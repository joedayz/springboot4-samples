package com.bcp.training.expenses;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Expense {

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD
    }

    @Id
    private UUID uuid;
    private String name;
    @Enumerated(EnumType.ORDINAL)
    private PaymentMethod paymentMethod;
    private BigDecimal amount;

    public Expense() {
    }

    public Expense(UUID uuid, String name, PaymentMethod paymentMethod, BigDecimal amount) {
        this.uuid = uuid;
        this.name = name;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public Expense(String name, PaymentMethod paymentMethod, String amount) {
        this(UUID.randomUUID(), name, paymentMethod, new BigDecimal(amount));
    }

    public static Expense of(String name, PaymentMethod paymentMethod, String amount) {
        return new Expense(name, paymentMethod, amount);
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
