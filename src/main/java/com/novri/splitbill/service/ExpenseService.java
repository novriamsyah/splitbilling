package com.novri.splitbill.service;

import com.novri.splitbill.dto.expense.CreateExpenseRequest;
import com.novri.splitbill.dto.expense.ExpenseResponse;
import com.novri.splitbill.dto.expense.UpdateExpenseRequest;

import java.util.List;
import java.util.UUID;

public interface ExpenseService {
    ExpenseResponse createExpense(
            UUID groupId,
            CreateExpenseRequest request
    );

    List<ExpenseResponse> getAllExpenses(UUID groupId);

    ExpenseResponse getExpenseById(
            UUID groupId,
            UUID expenseId
    );

    ExpenseResponse updateExpense(
            UUID groupId,
            UUID expenseId,
            UpdateExpenseRequest request
    );

    void deleteExpense(
            UUID groupId,
            UUID expenseId
    );
}
