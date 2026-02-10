package com.bcp.training.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// TODO: Add @Entity annotation
// TODO: Add necessary JPA imports (jakarta.persistence.*)
public class Expense {

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD
    }

    // TODO: Add @Id and @GeneratedValue(strategy = GenerationType.IDENTITY) annotations
    private Long id;

    // TODO: Add @NotNull annotation (from jakarta.validation.constraints)
    private UUID uuid;

    private String name;

    // TODO: Add @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") annotation
    private LocalDateTime creationDate;

    // TODO: Add @Enumerated(EnumType.STRING) annotation
    private PaymentMethod paymentMethod;

    private BigDecimal amount;

    // TODO: Add @JsonIgnore annotation to avoid circular reference
    // TODO: Add @ManyToOne(fetch = FetchType.LAZY) annotation
    // TODO: Add @JoinColumn(name = "associate_id", insertable = false, updatable = false) annotation
    private Associate associate;

    // TODO: Add @Column(name = "associate_id") annotation
    private Long associateId;

    // TODO: Add a no-argument constructor that initializes uuid and creationDate

    public Expense(String name, PaymentMethod paymentMethod, String amount) {
        this.uuid = UUID.randomUUID();
        this.creationDate = LocalDateTime.now();
        this.name = name;
        this.paymentMethod = paymentMethod;
        this.amount = new BigDecimal(amount);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Associate getAssociate() {
        return associate;
    }

    public void setAssociate(Associate associate) {
        this.associate = associate;
    }

    public Long getAssociateId() {
        return associateId;
    }

    public void setAssociateId(Long associateId) {
        this.associateId = associateId;
    }
}
