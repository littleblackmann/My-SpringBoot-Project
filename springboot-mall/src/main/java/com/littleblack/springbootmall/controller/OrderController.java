package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.BuyItem;
import com.littleblack.springbootmall.dto.CreateOrderRequest;
import com.littleblack.springbootmall.dto.OrderQueryParams;
import com.littleblack.springbootmall.dto.OrderResponse;
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
        logger.info("Received order request: {}", createOrderRequest);

        Integer orderId = orderService.createOrder(userId, createOrderRequest);
        Order order = orderService.getOrderById(orderId);

        String userEmail = createOrderRequest.getUserEmail();
        Integer totalAmount = createOrderRequest.getTotalAmount();
        String arrivalDate = createOrderRequest.getArrivalDate();
        String arrivalTime = createOrderRequest.getArrivalTime();
        String phoneNumber = createOrderRequest.getPhoneNumber();

        String userEmailSubject = "您的訂單確認";
        String userEmailText = buildOrderEmailContent(createOrderRequest, totalAmount, order.getCreatedDate(), arrivalDate, arrivalTime, phoneNumber);

        String businessEmailSubject = "新訂單通知";
        String businessEmailText = buildBusinessOrderEmailContent(createOrderRequest, totalAmount, order.getCreatedDate(), arrivalDate, arrivalTime, phoneNumber, userEmail);

        try {
            sendEmail(userEmail, userEmailSubject, userEmailText);
            sendEmail("littleblack0830@gmail.com", businessEmailSubject, businessEmailText);
            logger.info("Order confirmation emails sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OrderResponse(order, "Order created successfully, but email notification failed"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    private String buildBusinessOrderEmailContent(CreateOrderRequest createOrderRequest, Integer totalAmount, Date orderTime, String arrivalDate, String arrivalTime, String phoneNumber, String customerEmail) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder content = new StringBuilder();
        content.append("新訂單通知\n\n");
        content.append("訂單時間：").append(dateFormat.format(orderTime)).append("\n");
        content.append("客戶信息：\n");
        content.append("  電子郵件: ").append(customerEmail).append("\n");
        content.append("  電話號碼：").append(phoneNumber).append("\n");
        content.append("到達日期：").append(arrivalDate).append("\n");
        content.append("到達時間：").append(arrivalTime).append("\n\n");

        content.append("訂購商品：\n");
        for (BuyItem buyItem : createOrderRequest.getBuyItemList()) {
            content.append("- ").append(buyItem.getProductName())
                    .append("，數量：").append(buyItem.getQuantity())
                    .append("\n");
        }

        content.append("\n總計：").append(totalAmount).append(" 元\n\n");
        content.append("請及時準備訂單。");

        return content.toString();
    }

    private String buildOrderEmailContent(CreateOrderRequest createOrderRequest, Integer totalAmount, Date orderTime, String arrivalDate, String arrivalTime, String phoneNumber) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder content = new StringBuilder();
        content.append("親愛的顧客，您好！\n\n");
        content.append("感謝您在 ZeroSupper 的訂購。以下是您的訂單詳情：\n\n");
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

        content.append("\n總計：").append(totalAmount).append(" 元\n\n");
        content.append("ZeroSupper 已收到您的訂單，請保留此郵件作為記錄。\n");
        content.append("我們期待您在 ").append(arrivalDate).append(" ").append(arrivalTime).append(" 的到來。\n\n");
        content.append("如有任何疑問，請隨時與我們聯繫。\n\n");
        content.append("謝謝您的惠顧！\n");
        content.append("ZeroSupper 團隊");

        return content.toString();
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text.replace("\n", System.lineSeparator()));
        message.setFrom("littleblack0830@gmail.com");
        mailSender.send(message);
    }
}