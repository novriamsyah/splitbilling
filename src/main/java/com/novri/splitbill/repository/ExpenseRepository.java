package com.novri.splitbill.repository;

import com.novri.splitbill.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(
            UUID groupId
    );

    Optional<Expense> findByIdAndGroupId(
            UUID id,
            UUID groupId
    );
}
