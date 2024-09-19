package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.dto.CreateOrderRequest;
import com.littleblack.springbootmall.dto.OrderQueryParams;
import com.littleblack.springbootmall.model.Order;

import java.util.List;

public interface OrderService {

    Integer countOrder(OrderQueryParams orderQueryParams);

    List<Order> getOrders(OrderQueryParams orderQueryParams);

    Order getOrderById(Integer orderId);

    Integer createOrder(Integer userId, CreateOrderRequest createOrderRequest);
}
