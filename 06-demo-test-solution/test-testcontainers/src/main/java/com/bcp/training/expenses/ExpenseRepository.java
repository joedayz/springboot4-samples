package com.bcp.training.expenses;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    long deleteByUuid(UUID uuid);
}
