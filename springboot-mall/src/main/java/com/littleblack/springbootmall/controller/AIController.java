package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.ai.ChatRequest;
import com.littleblack.springbootmall.dto.ai.ChatResponse;
import com.littleblack.springbootmall.model.User;
import com.littleblack.springbootmall.service.AIService;
import com.littleblack.springbootmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final static Logger log = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;
    private final UserService userService;

    @Autowired
    public AIController(AIService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            String response = aiService.generateResponse(request.getMessage(), userId);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("處理聊天請求時發生錯誤，用戶訊息：{}", request.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("抱歉，系統暫時無法處理您的請求。請稍後再試，或是讓我為您介紹其他服務。"));
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<ChatResponse> recommend(@RequestBody ChatRequest request, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            String response = aiService.generateProductRecommendation(request.getMessage(), userId);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("處理推薦請求時發生錯誤，用戶偏好：{}", request.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("抱歉，目前無法提供個人化推薦。讓我為您介紹我們的熱門餐點。"));
        }
    }

    @GetMapping("/menu/{category}")
    public ResponseEntity<ChatResponse> getMenuInfo(@PathVariable String category) {
        try {
            String response = aiService.generateMenuResponse(category);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("獲取菜單資訊時發生錯誤，類別：{}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("抱歉，目前無法顯示菜單資訊。讓我為您介紹其他餐點類別。"));
        }
    }

    @GetMapping("/price/{productName}")
    public ResponseEntity<ChatResponse> getPriceInfo(@PathVariable String productName) {
        try {
            String response = aiService.generatePriceQuery(productName);
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            log.error("查詢價格時發生錯誤，產品名稱：{}", productName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("抱歉，目前無法查詢此餐點的價格。您可以詢問其他餐點的資訊。"));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleAllExceptions(Exception e) {
        log.error("控制器發生未預期的錯誤", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatResponse("系統發生未預期的錯誤，請稍後再試。很抱歉造成您的不便。"));
    }

    private ResponseEntity<ChatResponse> createStandardErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatResponse(message));
    }
}