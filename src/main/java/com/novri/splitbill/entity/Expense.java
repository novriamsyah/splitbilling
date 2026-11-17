package com.novri.splitbill.entity;

import com.novri.splitbill.enums.SplitType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "expenses", schema = "tugas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "payer_id", nullable = false)
    private UUID payerId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 20)
    private SplitType splitType;

    @Column(name = "expense_date", nullable = false)
    private OffsetDateTime expenseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Expense(
            UUID groupId,
            UUID payerId,
            String description,
            BigDecimal amount,
            SplitType splitType,
            OffsetDateTime  expenseDate
    ) {
        this.groupId = groupId;
        this.payerId = payerId;
        this.description = description;
        this.amount = amount;
        this.splitType = splitType;
        this.expenseDate = expenseDate;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(
            UUID payerId,
            String description,
            BigDecimal amount,
            SplitType splitType,
            OffsetDateTime  expenseDate
    ) {
        this.payerId = payerId;
        this.description = description;
        this.amount = amount;
        this.splitType = splitType;
        this.expenseDate = expenseDate;
    }
}
