package com.novri.splitbill.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense_shares", schema = "tugas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseShare {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(
            name = "share_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal shareAmount;

    @Column(
            name = "share_percentage",
            precision = 7,
            scale = 4
    )
    private BigDecimal sharePercentage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ExpenseShare(
            UUID groupId,
            UUID expenseId,
            UUID participantId,
            BigDecimal shareAmount,
            BigDecimal sharePercentage
    ) {
        this.groupId = groupId;
        this.expenseId = expenseId;
        this.participantId = participantId;
        this.shareAmount = shareAmount;
        this.sharePercentage = sharePercentage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
