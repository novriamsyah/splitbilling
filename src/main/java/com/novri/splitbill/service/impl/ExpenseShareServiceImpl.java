package com.novri.splitbill.service.impl;

import com.novri.splitbill.dto.share.ExpenseShareAllocationRequest;
import com.novri.splitbill.dto.share.ExpenseShareResponse;
import com.novri.splitbill.dto.share.ReplaceExpenseSharesRequest;
import com.novri.splitbill.entity.Expense;
import com.novri.splitbill.entity.ExpenseShare;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.repository.ExpenseRepository;
import com.novri.splitbill.repository.ExpenseShareRepository;
import com.novri.splitbill.repository.ParticipantRepository;
import com.novri.splitbill.service.ExpenseShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseShareServiceImpl implements ExpenseShareService {
    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;
    private final BillGroupRepository groupRepository;

    @Override
    public List<ExpenseShareResponse> replaceExpenseShares(
            UUID groupId,
            UUID expenseId,
            ReplaceExpenseSharesRequest request
    ) {

        validateGroupExists(groupId);

        Expense expense = findExpense(groupId, expenseId);

        List<ExpenseShareAllocationRequest> allocations =
                request.allocations();

        validateParticipants(groupId, allocations);

        List<ExpenseShare> calculatedShares =
                calculateShares(groupId, expense, allocations);

        validateTotalShares(expense.getAmount(), calculatedShares);

        // Remove previous shares before inserting new shares.
        List<ExpenseShare> existingShares =
                expenseShareRepository.findAllByGroupIdAndExpenseId(
                        groupId,
                        expenseId
                );

        expenseShareRepository.deleteAll(existingShares);
        expenseShareRepository.flush();

        List<ExpenseShare> savedShares =
                expenseShareRepository.saveAll(calculatedShares);

        return savedShares.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseShareResponse> getExpenseShares(
            UUID groupId,
            UUID expenseId
    ) {

        validateGroupExists(groupId);
        findExpense(groupId, expenseId);

        return expenseShareRepository
                .findAllByGroupIdAndExpenseIdOrderByCreatedAtAsc(
                        groupId,
                        expenseId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private List<ExpenseShare> calculateShares(
            UUID groupId,
            Expense expense,
            List<ExpenseShareAllocationRequest> allocations
    ) {

        return switch (expense.getSplitType()) {

            case EQUAL ->
                    calculateEqualShares(groupId, expense, allocations);

            case EXACT ->
                    calculateExactShares(groupId, expense, allocations);

            case PERCENTAGE ->
                    calculatePercentageShares(groupId, expense, allocations);
        };
    }

    // =========================================
    // EQUAL SPLIT
    // =========================================

    private List<ExpenseShare> calculateEqualShares(
            UUID groupId,
            Expense expense,
            List<ExpenseShareAllocationRequest> allocations
    ) {

        validateFieldsForEqual(allocations);

        BigDecimal total = expense.getAmount()
                .setScale(2, RoundingMode.UNNECESSARY);

        int participantCount = allocations.size();

        BigDecimal baseShare = total.divide(
                BigDecimal.valueOf(participantCount),
                2,
                RoundingMode.DOWN
        );

        BigDecimal remainder = total.subtract(
                baseShare.multiply(BigDecimal.valueOf(participantCount))
        );

        int remainingCents = remainder
                .movePointRight(2)
                .intValueExact();

        List<ExpenseShare> shares = new ArrayList<>();

        for (int i = 0; i < participantCount; i++) {

            BigDecimal shareAmount = baseShare;

            // Distribute remaining cents one by one.
            if (i < remainingCents) {
                shareAmount = shareAmount.add(ONE_CENT);
            }

            shares.add(new ExpenseShare(
                    groupId,
                    expense.getId(),
                    allocations.get(i).participantId(),
                    shareAmount,
                    null
            ));
        }

        return shares;
    }

    // =========================================
    // EXACT SPLIT
    // =========================================

    private List<ExpenseShare> calculateExactShares(
            UUID groupId,
            Expense expense,
            List<ExpenseShareAllocationRequest> allocations
    ) {

        validateFieldsForExact(allocations);

        BigDecimal totalShares = allocations.stream()
                .map(ExpenseShareAllocationRequest::shareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShares.compareTo(expense.getAmount()) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total share amount harus sama dengan nominal expense"
            );
        }

        return allocations.stream()
                .map(allocation -> new ExpenseShare(
                        groupId,
                        expense.getId(),
                        allocation.participantId(),
                        allocation.shareAmount().setScale(2),
                        null
                ))
                .toList();
    }

    // =========================================
    // PERCENTAGE SPLIT
    // =========================================

    private List<ExpenseShare> calculatePercentageShares(
            UUID groupId,
            Expense expense,
            List<ExpenseShareAllocationRequest> allocations
    ) {

        validateFieldsForPercentage(allocations);

        BigDecimal totalPercentage = allocations.stream()
                .map(ExpenseShareAllocationRequest::sharePercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(ONE_HUNDRED) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total share percentage harus tepat 100%"
            );
        }

        // Calculate each exact share, then round down to cents.
        List<PercentageCalculation> calculations = new ArrayList<>();

        for (int i = 0; i < allocations.size(); i++) {

            ExpenseShareAllocationRequest allocation = allocations.get(i);

            BigDecimal exactAmount = expense.getAmount()
                    .multiply(allocation.sharePercentage())
                    .divide(ONE_HUNDRED);

            BigDecimal roundedDown = exactAmount.setScale(
                    2,
                    RoundingMode.DOWN
            );

            BigDecimal fractionalRemainder =
                    exactAmount.subtract(roundedDown);

            calculations.add(new PercentageCalculation(
                    i,
                    allocation,
                    roundedDown,
                    fractionalRemainder
            ));
        }

        BigDecimal roundedTotal = calculations.stream()
                .map(PercentageCalculation::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = expense.getAmount()
                .subtract(roundedTotal);

        int remainingCents = remaining
                .movePointRight(2)
                .intValueExact();

        // Give leftover cents to participants with the largest
        // fractional remainders. Stable order resolves ties.
        List<PercentageCalculation> ranked = calculations.stream()
                .sorted(
                        Comparator.comparing(
                                PercentageCalculation::fractionalRemainder
                        ).reversed().thenComparingInt(
                                PercentageCalculation::index
                        )
                )
                .toList();

        Map<Integer, BigDecimal> finalAmounts = new HashMap<>();

        for (PercentageCalculation calculation : calculations) {
            finalAmounts.put(
                    calculation.index(),
                    calculation.amount()
            );
        }

        for (int i = 0; i < remainingCents; i++) {

            PercentageCalculation calculation = ranked.get(i);

            finalAmounts.compute(
                    calculation.index(),
                    (key, value) -> value.add(ONE_CENT)
            );
        }

        List<ExpenseShare> shares = new ArrayList<>();

        for (PercentageCalculation calculation : calculations) {

            ExpenseShareAllocationRequest allocation =
                    calculation.allocation();

            shares.add(new ExpenseShare(
                    groupId,
                    expense.getId(),
                    allocation.participantId(),
                    finalAmounts.get(calculation.index()),
                    allocation.sharePercentage().setScale(4)
            ));
        }

        return shares;
    }

    // =========================================
    // VALIDATIONS
    // =========================================

    private void validateFieldsForEqual(
            List<ExpenseShareAllocationRequest> allocations
    ) {

        for (ExpenseShareAllocationRequest allocation : allocations) {

            if (allocation.shareAmount() != null
                    || allocation.sharePercentage() != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Metode EQUAL tidak menerima shareAmount atau sharePercentage"
                );
            }
        }
    }

    private void validateFieldsForExact(
            List<ExpenseShareAllocationRequest> allocations
    ) {

        for (ExpenseShareAllocationRequest allocation : allocations) {

            if (allocation.shareAmount() == null
                    || allocation.sharePercentage() != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Metode EXACT membutuhkan shareAmount dan tidak menerima sharePercentage"
                );
            }
        }
    }

    private void validateFieldsForPercentage(
            List<ExpenseShareAllocationRequest> allocations
    ) {

        for (ExpenseShareAllocationRequest allocation : allocations) {

            if (allocation.sharePercentage() == null
                    || allocation.shareAmount() != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Metode PERCENTAGE membutuhkan sharePercentage dan tidak menerima shareAmount"
                );
            }
        }
    }

    private void validateParticipants(
            UUID groupId,
            List<ExpenseShareAllocationRequest> allocations
    ) {

        Set<UUID> participantIds = allocations.stream()
                .map(ExpenseShareAllocationRequest::participantId)
                .collect(Collectors.toSet());

        if (participantIds.size() != allocations.size()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant tidak boleh duplikat dalam satu expense"
            );
        }

        for (UUID participantId : participantIds) {

            if (!participantRepository.existsByIdAndGroupId(
                    participantId,
                    groupId
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Participant " + participantId
                                + " tidak ditemukan dalam group tersebut"
                );
            }
        }
    }

    private void validateTotalShares(
            BigDecimal expenseAmount,
            List<ExpenseShare> shares
    ) {

        BigDecimal total = shares.stream()
                .map(ExpenseShare::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(expenseAmount) != 0) {

            throw new IllegalStateException(
                    "Total expense shares tidak sama dengan nominal expense"
            );
        }
    }

    private void validateGroupExists(UUID groupId) {

        if (!groupRepository.existsById(groupId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Group tidak ditemukan dengan ID: " + groupId
            );
        }
    }

    private Expense findExpense(UUID groupId, UUID expenseId) {

        return expenseRepository
                .findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Expense tidak ditemukan dalam group tersebut"
                ));
    }

    private ExpenseShareResponse mapToResponse(ExpenseShare share) {

        return new ExpenseShareResponse(
                share.getId(),
                share.getGroupId(),
                share.getExpenseId(),
                share.getParticipantId(),
                share.getShareAmount(),
                share.getSharePercentage(),
                share.getCreatedAt()
        );
    }

    private record PercentageCalculation(
            int index,
            ExpenseShareAllocationRequest allocation,
            BigDecimal amount,
            BigDecimal fractionalRemainder
    ) {
    }
}
