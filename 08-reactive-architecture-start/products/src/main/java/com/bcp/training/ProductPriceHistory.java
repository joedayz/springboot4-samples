package com.bcp.training;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProductPriceHistory {

    @JsonProperty("product_id")
    public Long productId;

    public List<Price> prices;

    public ProductPriceHistory() {
    }

    public ProductPriceHistory(Long productId, List<Price> prices) {
        this.productId = productId;
        this.prices = prices;
    }
}
