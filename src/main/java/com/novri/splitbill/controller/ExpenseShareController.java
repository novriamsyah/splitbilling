package com.novri.splitbill.controller;

import com.novri.splitbill.dto.share.ExpenseShareResponse;
import com.novri.splitbill.dto.share.ReplaceExpenseSharesRequest;
import com.novri.splitbill.service.ExpenseShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses/{expenseId}/shares")
@RequiredArgsConstructor
@Tag(
        name = "Expense Share Management",
        description = "API untuk menghitung dan mengelola pembagian pengeluaran"
)
public class ExpenseShareController {
    private final ExpenseShareService expenseShareService;

    @PutMapping
    @Operation(
            summary = "Calculate Expense Shares",
            description = "Menghitung dan mengganti pembagian nominal expense"
    )
    public ResponseEntity<List<ExpenseShareResponse>> replaceExpenseShares(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody ReplaceExpenseSharesRequest request
    ) {

        List<ExpenseShareResponse> response =
                expenseShareService.replaceExpenseShares(
                        groupId,
                        expenseId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get Expense Shares",
            description = "Mengambil hasil pembagian expense"
    )
    public ResponseEntity<List<ExpenseShareResponse>> getExpenseShares(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId
    ) {

        List<ExpenseShareResponse> response =
                expenseShareService.getExpenseShares(groupId, expenseId);

        return ResponseEntity.ok(response);
    }
}
