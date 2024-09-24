package com.littleblack.springbootmall.dto;

import jakarta.validation.constraints.NotNull;

public class BuyItem {

    @NotNull
    private Integer productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private String productName;

    public @NotNull String getProductName() {
        return productName;
    }

    public void setProductName(@NotNull String productName) {
        this.productName = productName;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
