package com.bcp.training.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Table("bank_account")
public class BankAccount {

    @Id
    private Long id;

    @NotNull(message = "El balance no puede ser null")
    @Positive(message = "El balance debe ser un número positivo")
    private Long balance;

    private String type;

    public BankAccount() {
    }

    public BankAccount(Long balance, String type) {
        this.balance = balance;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
