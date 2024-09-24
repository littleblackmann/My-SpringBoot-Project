package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.BuyItem;
import com.littleblack.springbootmall.dto.CreateOrderRequest;
import com.littleblack.springbootmall.dto.OrderQueryParams;
import com.littleblack.springbootmall.model.Order;
import com.littleblack.springbootmall.service.OrderService;
import com.littleblack.springbootmall.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LogManager.getLogger(OrderController.class);

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<Page<Order>> getOrders(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") @Max(1000) @Min(0) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        OrderQueryParams orderQueryParams = new OrderQueryParams();
        orderQueryParams.setUserId(userId);
        orderQueryParams.setLimit(limit);
        orderQueryParams.setOffset(offset);

        List<Order> orderList = orderService.getOrders(orderQueryParams);
        Integer count = orderService.countOrder(orderQueryParams);

        Page<Order> page = new Page<>();
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal(count);
        page.setResults(orderList);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @PostMapping("/users/{userId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable Integer userId,
                                         @RequestBody @Valid CreateOrderRequest createOrderRequest) {

        Integer orderId = orderService.createOrder(userId, createOrderRequest);
        Order order = orderService.getOrderById(orderId);

        String userEmail = createOrderRequest.getUserEmail();
        Integer totalAmount = createOrderRequest.getTotalAmount();
        String arrivalDate = createOrderRequest.getArrivalDate();
        String arrivalTime = createOrderRequest.getArrivalTime();
        String phoneNumber = createOrderRequest.getPhoneNumber();

        String userEmailSubject = "您的訂單確認";
        String userEmailText = buildOrderEmailContent(createOrderRequest, totalAmount, order.getCreatedDate(), arrivalDate, arrivalTime, phoneNumber);

        String restaurantEmailSubject = "新訂單通知";
        String restaurantEmailText = buildOrderEmailContent(createOrderRequest, totalAmount, order.getCreatedDate(), arrivalDate, arrivalTime, phoneNumber);

        try {
            sendEmail(userEmail, userEmailSubject, userEmailText);
            sendEmail("littleblack0830@gmail.com", restaurantEmailSubject, restaurantEmailText);
        } catch (Exception e) {
            logger.error("郵件發送失敗: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("訂單已建立，但郵件發送失敗");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    private String buildOrderEmailContent(CreateOrderRequest createOrderRequest, Integer totalAmount, Date orderTime, String arrivalDate, String arrivalTime, String phoneNumber) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder content = new StringBuilder();
        content.append("感謝您的訂購！您的訂單詳情如下：\n\n");
        content.append("訂單時間：").append(dateFormat.format(orderTime)).append("\n");
        content.append("到達日期：").append(arrivalDate).append("\n");
        content.append("到達時間：").append(arrivalTime).append("\n");
        content.append("聯絡電話：").append(phoneNumber).append("\n\n");

        content.append("訂購商品：\n");
        for (BuyItem buyItem : createOrderRequest.getBuyItemList()) {
            content.append("- ").append(buyItem.getProductName())
                    .append("，數量：").append(buyItem.getQuantity())
                    .append("\n");
        }

        content.append("\n總計：").append(totalAmount).append(" 元");
        content.append("\n\nZERO Supper已收到您的訂單，請保留此郵件作為紀錄。");
        content.append("\n我們期待您在 ").append(arrivalDate).append(" 的到來。");

        return content.toString();
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("littleblack0830@gmail.com"); // 替換為您的發件人郵箱地址
        mailSender.send(message);
    }
}