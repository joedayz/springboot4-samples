package com.bcp.training.service;

import com.bcp.training.model.Expense;
import com.bcp.training.repository.AssociateRepository;
import com.bcp.training.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AssociateRepository associateRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          AssociateRepository associateRepository) {
        this.expenseRepository = expenseRepository;
        this.associateRepository = associateRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> list(int pageSize, int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize,
                Sort.by("amount").and(Sort.by("associateId")));
        Page<Expense> page = expenseRepository.findAll(pageRequest);
        return page.getContent();
    }

    public Expense create(Expense expense) {
        associateRepository.findById(expense.getAssociateId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Associate not found"));

        expense.setUuid(UUID.randomUUID());
        expense.setCreationDate(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public void delete(UUID uuid) {
        long numDeleted = expenseRepository.deleteByUuid(uuid);
        if (numDeleted == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Expense not found");
        }
    }

    public void update(Expense expense) {
        Expense existing = expenseRepository.findById(expense.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));

        existing.setUuid(expense.getUuid());
        existing.setName(expense.getName());
        existing.setAmount(expense.getAmount());
        existing.setPaymentMethod(expense.getPaymentMethod());
        expenseRepository.save(existing);
    }
}
