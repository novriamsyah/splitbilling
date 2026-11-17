package com.novri.splitbill.service.impl;

import com.novri.splitbill.dto.expense.CreateExpenseRequest;
import com.novri.splitbill.dto.expense.ExpenseResponse;
import com.novri.splitbill.dto.expense.UpdateExpenseRequest;
import com.novri.splitbill.entity.Expense;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.repository.ExpenseRepository;
import com.novri.splitbill.repository.ParticipantRepository;
import com.novri.splitbill.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BillGroupRepository groupRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public ExpenseResponse createExpense(
            UUID groupId,
            CreateExpenseRequest request
    ) {

        validateGroupExists(groupId);
        validatePayer(groupId, request.payerId());

        Expense expense = new Expense(
                groupId,
                request.payerId(),
                request.description().trim(),
                request.amount(),
                request.splitType(),
                request.expenseDate()
        );

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses(UUID groupId) {

        validateGroupExists(groupId);

        return expenseRepository
                .findAllByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(
            UUID groupId,
            UUID expenseId
    ) {

        Expense expense = findExpense(groupId, expenseId);

        return mapToResponse(expense);
    }

    @Override
    public ExpenseResponse updateExpense(
            UUID groupId,
            UUID expenseId,
            UpdateExpenseRequest request
    ) {

        Expense expense = findExpense(groupId, expenseId);

        validatePayer(groupId, request.payerId());

        expense.update(
                request.payerId(),
                request.description().trim(),
                request.amount(),
                request.splitType(),
                request.expenseDate()
        );

        Expense updatedExpense = expenseRepository.save(expense);

        return mapToResponse(updatedExpense);
    }

    @Override
    public void deleteExpense(UUID groupId, UUID expenseId) {

        Expense expense = findExpense(groupId, expenseId);

        expenseRepository.delete(expense);
        expenseRepository.flush();
    }

    private void validateGroupExists(UUID groupId) {

        if (!groupRepository.existsById(groupId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Group tidak ditemukan dengan ID: " + groupId
            );
        }
    }

    private void validatePayer(UUID groupId, UUID payerId) {

        boolean participantExists =
                participantRepository.existsByIdAndGroupId(
                        payerId,
                        groupId
                );

        if (!participantExists) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payer tidak ditemukan dalam group tersebut"
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

    private ExpenseResponse mapToResponse(Expense expense) {

        return new ExpenseResponse(
                expense.getId(),
                expense.getGroupId(),
                expense.getPayerId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getSplitType(),
                expense.getExpenseDate(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
