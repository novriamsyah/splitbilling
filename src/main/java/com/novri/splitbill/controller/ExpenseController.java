package com.novri.splitbill.controller;

import com.novri.splitbill.dto.expense.CreateExpenseRequest;
import com.novri.splitbill.dto.expense.ExpenseResponse;
import com.novri.splitbill.dto.expense.UpdateExpenseRequest;
import com.novri.splitbill.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
@Tag(
        name = "Expense Management",
        description = "Expense management new group Split bill "
)
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    @Operation(
            summary = "Create Expense",
            description = "Mencatat pengeluaran baru dalam grup"
    )
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {

        ExpenseResponse response =
                expenseService.createExpense(groupId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get All Expenses",
            description = "Mengambil seluruh pengeluaran dalam grup"
    )
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @PathVariable UUID groupId
    ) {

        List<ExpenseResponse> response =
                expenseService.getAllExpenses(groupId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{expenseId}")
    @Operation(
            summary = "Get Expense By ID",
            description = "Mengambil detail pengeluaran berdasarkan UUID"
    )
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId
    ) {

        ExpenseResponse response =
                expenseService.getExpenseById(groupId, expenseId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{expenseId}")
    @Operation(
            summary = "Update Expense",
            description = "Mengubah data pengeluaran"
    )
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateExpenseRequest request
    ) {

        ExpenseResponse response =
                expenseService.updateExpense(
                        groupId,
                        expenseId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{expenseId}")
    @Operation(
            summary = "Delete Expense",
            description = "Menghapus pengeluaran dari grup"
    )
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId
    ) {

        expenseService.deleteExpense(groupId, expenseId);

        return ResponseEntity.noContent().build();
    }
}
