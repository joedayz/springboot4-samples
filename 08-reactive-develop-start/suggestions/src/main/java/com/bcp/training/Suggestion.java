package com.bcp.training;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("suggestion")
public class Suggestion {

    @Id
    public Long id;
    public Long clientId;
    public Long itemId;

    public Suggestion() {
    }

    public Suggestion(Long clientId, Long itemId) {
        this.clientId = clientId;
        this.itemId = itemId;
    }
}
