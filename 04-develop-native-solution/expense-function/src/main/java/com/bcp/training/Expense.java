package com.bcp.training;

import java.math.BigDecimal;
import java.util.UUID;

public class Expense {

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD
    }

    private UUID uuid;
    private String name;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;

    public Expense() {
        this.uuid = UUID.randomUUID();
    }

    Expense(String name, PaymentMethod paymentMethod, String amount) {
        this();
        this.name = name;
        this.paymentMethod = paymentMethod;
        this.amount = new BigDecimal(amount);
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
