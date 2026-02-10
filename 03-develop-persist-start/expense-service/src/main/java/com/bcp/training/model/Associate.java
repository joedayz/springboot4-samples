package com.bcp.training.model;

import java.util.ArrayList;
import java.util.List;

// TODO: Add @Entity annotation
// TODO: Add necessary JPA imports (jakarta.persistence.*)
public class Associate {

    // TODO: Add @Id and @GeneratedValue(strategy = GenerationType.IDENTITY) annotations
    private Long id;

    private String name;

    // TODO: Add @JsonIgnore annotation to avoid circular reference
    // TODO: Add @OneToMany(mappedBy = "associate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    // TODO: Add a no-argument constructor (required by JPA)

    public Associate(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
