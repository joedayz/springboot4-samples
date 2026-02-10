package com.bcp.training.repository;

import com.bcp.training.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByUuid(UUID uuid);

    long deleteByUuid(UUID uuid);
}
