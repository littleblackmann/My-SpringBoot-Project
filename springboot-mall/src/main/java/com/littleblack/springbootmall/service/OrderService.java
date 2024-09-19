package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.dto.CreateOrderRequest;
import com.littleblack.springbootmall.model.Order;

public interface OrderService {

    Order getOrderById(Integer orderId);

    Integer createOrder(Integer userId, CreateOrderRequest createOrderRequest);

}
