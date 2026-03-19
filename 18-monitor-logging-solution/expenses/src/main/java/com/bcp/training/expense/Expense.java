package com.bcp.training.expense;

import java.time.LocalDateTime;
import java.util.UUID;

public class Expense {

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD,
    }

    private UUID uuid;
    private String name;
    private LocalDateTime creationDate;
    private PaymentMethod paymentMethod;
    private double amount;
    private String username;

    public Expense() {
    }

    public Expense(UUID uuid, String name, LocalDateTime creationDate, PaymentMethod paymentMethod, double amount, String username) {
        this.uuid = uuid;
        this.name = name;
        this.creationDate = creationDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.username = username;
    }

    public Expense(String name, double amount, String username) {
        this(UUID.randomUUID(), name, LocalDateTime.now(), PaymentMethod.CASH, amount, username);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

