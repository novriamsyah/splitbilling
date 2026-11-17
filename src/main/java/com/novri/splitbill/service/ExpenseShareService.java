package com.novri.splitbill.service;

import com.novri.splitbill.dto.share.ExpenseShareResponse;
import com.novri.splitbill.dto.share.ReplaceExpenseSharesRequest;

import java.util.List;
import java.util.UUID;

public interface ExpenseShareService {
    List<ExpenseShareResponse> replaceExpenseShares(
            UUID groupId,
            UUID expenseId,
            ReplaceExpenseSharesRequest request
    );

    List<ExpenseShareResponse> getExpenseShares(
            UUID groupId,
            UUID expenseId
    );
}
