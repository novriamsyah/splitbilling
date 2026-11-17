package com.novri.splitbill.service.impl;

import com.novri.splitbill.dto.settlement.ParticipantBalanceResponse;
import com.novri.splitbill.dto.settlement.SettlementResponse;
import com.novri.splitbill.dto.settlement.SettlementTransferResponse;
import com.novri.splitbill.entity.BillGroup;
import com.novri.splitbill.entity.Expense;
import com.novri.splitbill.entity.ExpenseShare;
import com.novri.splitbill.entity.Participant;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.repository.ExpenseRepository;
import com.novri.splitbill.repository.ExpenseShareRepository;
import com.novri.splitbill.repository.ParticipantRepository;
import com.novri.splitbill.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementServiceImpl implements SettlementService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    @Override
    public SettlementResponse calculateSettlement(UUID groupId) {

        // 1. Validasi group
        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Group tidak ditemukan"
                ));

        // 2. Ambil semua participant
        List<Participant> participants =
                participantRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId);

        // 3. Ambil semua expense
        List<Expense> expenses =
                expenseRepository.findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId);

        // 4. Ambil semua expense shares
        List<ExpenseShare> shares =
                expenseShareRepository.findAllByGroupId(groupId);

        // 5. Siapkan accumulator setiap participant
        Map<UUID, BalanceAccumulator> balances = new LinkedHashMap<>();

        for (Participant participant : participants) {

            balances.put(
                    participant.getId(),
                    new BalanceAccumulator(
                            participant.getId(),
                            participant.getName()
                    )
            );
        }

        // 6. Hitung total pembayaran setiap participant
        BigDecimal totalExpense = ZERO;

        for (Expense expense : expenses) {

            BalanceAccumulator payer = balances.get(expense.getPayerId());

            if (payer == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Payer expense tidak ditemukan dalam group"
                );
            }

            payer.totalPaid = payer.totalPaid.add(expense.getAmount());

            totalExpense = totalExpense.add(expense.getAmount());
        }

        // 7. Hitung total shares setiap expense
        Map<UUID, BigDecimal> sharesByExpense = new LinkedHashMap<>();

        // 8. Hitung total beban setiap participant
        BigDecimal totalAllocated = ZERO;

        for (ExpenseShare share : shares) {

            BalanceAccumulator participant =
                    balances.get(share.getParticipantId());

            if (participant == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Expense share memiliki participant yang tidak valid"
                );
            }

            participant.totalShare =
                    participant.totalShare.add(share.getShareAmount());

            totalAllocated =
                    totalAllocated.add(share.getShareAmount());

            sharesByExpense.merge(
                    share.getExpenseId(),
                    share.getShareAmount(),
                    BigDecimal::add
            );
        }

        // 9. Pastikan setiap expense sudah memiliki pembagian shares
        for (Expense expense : expenses) {

            BigDecimal allocated = sharesByExpense.getOrDefault(
                    expense.getId(),
                    ZERO
            );

            if (allocated.compareTo(expense.getAmount()) != 0) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Expense '" + expense.getDescription()
                                + "' belum memiliki pembagian shares yang valid. "
                                + "Nominal expense: " + expense.getAmount()
                                + ", total shares: " + allocated
                );
            }
        }

        // 10. Pastikan total pengeluaran dan total shares seimbang
        if (totalExpense.compareTo(totalAllocated) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Total expense dan total allocated shares tidak seimbang"
            );
        }

        // 11. Hitung net balance
        List<ParticipantBalanceResponse> balanceResponses =
                new ArrayList<>();

        for (BalanceAccumulator balance : balances.values()) {

            balance.netBalance =
                    balance.totalPaid.subtract(balance.totalShare);

            String status;

            if (balance.netBalance.compareTo(BigDecimal.ZERO) > 0) {
                status = "CREDITOR";
            } else if (balance.netBalance.compareTo(BigDecimal.ZERO) < 0) {
                status = "DEBTOR";
            } else {
                status = "SETTLED";
            }

            balanceResponses.add(
                    new ParticipantBalanceResponse(
                            balance.participantId,
                            balance.participantName,
                            balance.totalPaid,
                            balance.totalShare,
                            balance.netBalance,
                            status
                    )
            );
        }

        // 12. Generate instruksi transfer
        List<SettlementTransferResponse> transfers =
                generateTransfers(balances);

        // 13. Return settlement response
        return new SettlementResponse(
                group.getId(),
                group.getName(),
                totalExpense,
                totalExpense,
                totalAllocated,
                balanceResponses,
                transfers
        );
    }

    private List<SettlementTransferResponse> generateTransfers(
            Map<UUID, BalanceAccumulator> balances
    ) {

        List<BalanceAccumulator> debtors = new ArrayList<>();
        List<BalanceAccumulator> creditors = new ArrayList<>();

        // Pisahkan debtor dan creditor
        for (BalanceAccumulator balance : balances.values()) {

            if (balance.netBalance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(balance.copy());
            }

            if (balance.netBalance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(balance.copy());
            }
        }

        List<SettlementTransferResponse> transfers =
                new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        // Cocokkan debtor dengan creditor
        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            BalanceAccumulator debtor = debtors.get(debtorIndex);
            BalanceAccumulator creditor = creditors.get(creditorIndex);

            BigDecimal debtorAmount =
                    debtor.netBalance.abs();

            BigDecimal creditorAmount =
                    creditor.netBalance;

            BigDecimal transferAmount =
                    debtorAmount.min(creditorAmount);

            // Debtor membayar creditor
            transfers.add(
                    new SettlementTransferResponse(
                            debtor.participantId,
                            debtor.participantName,
                            creditor.participantId,
                            creditor.participantName,
                            transferAmount
                    )
            );

            // Kurangi saldo debtor
            debtor.netBalance =
                    debtor.netBalance.add(transferAmount);

            // Kurangi saldo creditor
            creditor.netBalance =
                    creditor.netBalance.subtract(transferAmount);

            // Pindah ke debtor berikutnya jika sudah lunas
            if (debtor.netBalance.compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }

            // Pindah ke creditor berikutnya jika sudah lunas
            if (creditor.netBalance.compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
        }

        return transfers;
    }

    private static class BalanceAccumulator {

        private final UUID participantId;
        private final String participantName;

        private BigDecimal totalPaid = ZERO;
        private BigDecimal totalShare = ZERO;
        private BigDecimal netBalance = ZERO;

        private BalanceAccumulator(
                UUID participantId,
                String participantName
        ) {
            this.participantId = participantId;
            this.participantName = participantName;
        }

        private BalanceAccumulator copy() {

            BalanceAccumulator copy =
                    new BalanceAccumulator(
                            participantId,
                            participantName
                    );

            copy.totalPaid = totalPaid;
            copy.totalShare = totalShare;
            copy.netBalance = netBalance;

            return copy;
        }
    }
}
