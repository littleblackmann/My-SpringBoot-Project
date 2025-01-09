package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.ai.ChatRequest;
import com.littleblack.springbootmall.service.AIService;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public String chat(@Validated @RequestBody ChatRequest request) {
        try {
            log.info("接收到聊天請求: {}", request.getMessage());
            String response = aiService.generateResponse(request.getMessage());
            log.debug("生成回應成功");
            return response;
        } catch (Exception e) {
            log.error("處理聊天請求時發生錯誤: {}", e.getMessage(), e);
            return "很抱歉，系統暫時無法處理您的請求，請稍後再試。";
        }
    }

    @PostMapping("/recommend")
    public String recommendProducts(@Validated @RequestBody ChatRequest request) {
        try {
            log.info("接收到產品推薦請求: {}", request.getMessage());
            String recommendation = aiService.generateProductRecommendation(request.getMessage());
            log.debug("產品推薦生成成功");
            return recommendation;
        } catch (Exception e) {
            log.error("產生產品推薦時發生錯誤: {}", e.getMessage(), e);
            return "很抱歉，系統暫時無法提供產品推薦服務，請稍後再試。";
        }
    }
}