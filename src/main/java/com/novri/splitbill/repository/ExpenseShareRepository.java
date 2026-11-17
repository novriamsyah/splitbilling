package com.novri.splitbill.repository;

import com.novri.splitbill.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, UUID> {
    List<ExpenseShare> findAllByGroupIdAndExpenseIdOrderByCreatedAtAsc(
            UUID groupId,
            UUID expenseId
    );

    List<ExpenseShare> findAllByGroupIdAndExpenseId(
            UUID groupId,
            UUID expenseId
    );

    List<ExpenseShare> findAllByGroupId(UUID groupId);
}
