package com.littleblack.springbootmall.dto;

import com.littleblack.springbootmall.model.Order;

public class OrderResponse {
    private final Order order;
    private final String message;

    public OrderResponse(Order order, String message) {
        this.order = order;
        this.message = message;
    }

    public Order getOrder() {
        return order;
    }

    public String getMessage() {
        return message;
    }
}