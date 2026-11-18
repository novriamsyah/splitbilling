package com.novri.splitbill.service;

import com.novri.splitbill.dto.settlement.SettlementResponse;
import com.novri.splitbill.entity.BillGroup;
import com.novri.splitbill.entity.Expense;
import com.novri.splitbill.entity.ExpenseShare;
import com.novri.splitbill.entity.Participant;
import com.novri.splitbill.enums.SplitType;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.repository.ExpenseRepository;
import com.novri.splitbill.repository.ExpenseShareRepository;
import com.novri.splitbill.repository.ParticipantRepository;
import com.novri.splitbill.service.impl.SettlementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseShareRepository expenseShareRepository;

    private SettlementServiceImpl settlementService;

    private final UUID groupId = UUID.fromString(
            "11111111-1111-1111-1111-111111111111"
    );
    private final UUID novriId = UUID.fromString(
            "22222222-2222-2222-2222-222222222221"
    );
    private final UUID andiId = UUID.fromString(
            "22222222-2222-2222-2222-222222222222"
    );
    private final UUID budiId = UUID.fromString(
            "22222222-2222-2222-2222-222222222223"
    );

    @BeforeEach
    void setUp() {
        settlementService = new SettlementServiceImpl(
                billGroupRepository,
                participantRepository,
                expenseRepository,
                expenseShareRepository,
                new ServiceChargeCalculator("novriamsyah")
        );
    }

    @Test
    void calculatesBalancesTransfersAndPersonalizedServiceCharge() {
        BillGroup group = new BillGroup("Makan Bersama", "IDR");
        ReflectionTestUtils.setField(group, "id", groupId);

        Participant novri = new Participant(groupId, "Novri");
        Participant andi = new Participant(groupId, "Andi");
        Participant budi = new Participant(groupId, "Budi");
        ReflectionTestUtils.setField(novri, "id", novriId);
        ReflectionTestUtils.setField(andi, "id", andiId);
        ReflectionTestUtils.setField(budi, "id", budiId);

        UUID lunchId = UUID.randomUUID();
        UUID drinksId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Expense lunch = new Expense(
                groupId, novriId, "Makan Siang",
                new BigDecimal("150000.00"), SplitType.EQUAL, LocalDate.now()
        );
        Expense drinks = new Expense(
                groupId, andiId, "Minuman",
                new BigDecimal("90000.00"), SplitType.EQUAL, LocalDate.now()
        );
        ReflectionTestUtils.setField(lunch, "id", lunchId);
        ReflectionTestUtils.setField(drinks, "id", drinksId);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));
        when(participantRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId))
                .thenReturn(List.of(novri, andi, budi));
        when(expenseRepository.findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId))
                .thenReturn(List.of(lunch, drinks));
        when(expenseShareRepository.findAllByGroupId(groupId))
                .thenReturn(List.of(
                        share(groupId, lunchId, novriId, "50000.00"),
                        share(groupId, lunchId, andiId, "50000.00"),
                        share(groupId, lunchId, budiId, "50000.00"),
                        share(groupId, drinksId, novriId, "30000.00"),
                        share(groupId, drinksId, andiId, "30000.00"),
                        share(groupId, drinksId, budiId, "30000.00")
                ));

        SettlementResponse response =
                settlementService.calculateSettlement(groupId);

        assertEquals(new BigDecimal("240000.00"), response.totalExpense());
        assertEquals(new BigDecimal("240000.00"), response.totalPaid());
        assertEquals(new BigDecimal("240000.00"), response.totalAllocated());
        assertEquals(1, response.serviceChargePct());
        assertEquals(new BigDecimal("2400.00"), response.serviceChargeAmount());

        assertEquals(3, response.balances().size());
        assertEquals(new BigDecimal("70000.00"), response.balances().get(0).netBalance());
        assertEquals(new BigDecimal("10000.00"), response.balances().get(1).netBalance());
        assertEquals(new BigDecimal("-80000.00"), response.balances().get(2).netBalance());

        assertEquals(2, response.transfers().size());
        assertEquals(budiId, response.transfers().get(0).fromParticipantId());
        assertEquals(novriId, response.transfers().get(0).toParticipantId());
        assertEquals(new BigDecimal("70000.00"), response.transfers().get(0).amount());
        assertEquals(budiId, response.transfers().get(1).fromParticipantId());
        assertEquals(andiId, response.transfers().get(1).toParticipantId());
        assertEquals(new BigDecimal("10000.00"), response.transfers().get(1).amount());
    }

    @Test
    void rejectsSettlementWhenAnExpenseHasNoCompleteAllocation() {
        BillGroup group = new BillGroup("Makan Bersama", "IDR");
        ReflectionTestUtils.setField(group, "id", groupId);

        Participant novri = new Participant(groupId, "Novri");
        ReflectionTestUtils.setField(novri, "id", novriId);

        Expense expense = new Expense(
                groupId, novriId, "Makan Siang",
                new BigDecimal("100000.00"), SplitType.EQUAL,
                LocalDate.now()
        );
        ReflectionTestUtils.setField(expense, "id", UUID.randomUUID());

        when(billGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(participantRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId))
                .thenReturn(List.of(novri));
        when(expenseRepository.findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId))
                .thenReturn(List.of(expense));
        when(expenseShareRepository.findAllByGroupId(groupId))
                .thenReturn(List.of());

        assertThrows(
                ResponseStatusException.class,
                () -> settlementService.calculateSettlement(groupId)
        );
    }

    @Test
    void returnsEmptyTransfersWhenAllParticipantsAreSettled() {
        BillGroup group = new BillGroup("Solo", "IDR");
        ReflectionTestUtils.setField(group, "id", groupId);

        Participant novri = new Participant(groupId, "Novri");
        ReflectionTestUtils.setField(novri, "id", novriId);

        UUID expenseId = UUID.randomUUID();
        Expense expense = new Expense(
                groupId, novriId, "Makan",
                new BigDecimal("10000.00"), SplitType.EQUAL,
                LocalDate.now()
        );
        ReflectionTestUtils.setField(expense, "id", expenseId);

        when(billGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(participantRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId))
                .thenReturn(List.of(novri));
        when(expenseRepository.findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId))
                .thenReturn(List.of(expense));
        when(expenseShareRepository.findAllByGroupId(groupId))
                .thenReturn(List.of(share(groupId, expenseId, novriId, "10000.00")));

        SettlementResponse response = settlementService.calculateSettlement(groupId);

        assertEquals("SETTLED", response.balances().getFirst().status());
        assertTrue(response.transfers().isEmpty());
    }

    private ExpenseShare share(
            UUID groupId,
            UUID expenseId,
            UUID participantId,
            String amount
    ) {
        return new ExpenseShare(
                groupId,
                expenseId,
                participantId,
                new BigDecimal(amount),
                null
        );
    }
}
