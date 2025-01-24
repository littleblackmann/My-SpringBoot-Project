package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.AIService;
import com.littleblack.springbootmall.service.MenuContextService;
import com.littleblack.springbootmall.service.RAGService;
import com.littleblack.springbootmall.service.AIReservationService;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.littleblack.springbootmall.config.SystemPrompt;
import com.littleblack.springbootmall.model.ConversationMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final OpenAiChatClient chatClient;
    private final MenuContextService menuContextService;
    private final RAGService ragService;
    private final AIReservationService aiReservationService;

    private final Set<String> reservationKeywords = new HashSet<>(Arrays.asList(
            "訂位", "預約", "訂餐", "訂桌", "預訂", "位子",
            "幾點", "什麼時候", "時段", "用餐時間", "預定"
    ));

    public AIServiceImpl(OpenAiChatClient chatClient,
                         MenuContextService menuContextService,
                         RAGService ragService,
                         AIReservationService aiReservationService) {
        this.chatClient = chatClient;
        this.menuContextService = menuContextService;
        this.ragService = ragService;
        this.aiReservationService = aiReservationService;
    }

    private String getSystemPrompt(String userQuery) {
        String menuInfo = menuContextService.getMenuContext();
        String relevantContext = ragService.generateEnhancedPrompt(userQuery);
        String basePrompt = String.format(SystemPrompt.getSystemPrompt(), menuInfo, relevantContext);

        // 添加用戶查詢相關的上下文
        return basePrompt + "\n\n當前用戶查詢：" + userQuery;
    }

    @Override
    public boolean containsReservationKeywords(String message) {
        String lowerMessage = message.toLowerCase();
        return reservationKeywords.stream()
                .anyMatch(lowerMessage::contains);
    }

    @Override
    public String generateResponse(String message, Integer userId) {
        try {
            log.info("開始生成回應，用戶訊息: {}, 用戶ID: {}", message, userId);

            // 處理未登入用戶的訂位意圖
            if (userId == null && containsReservationKeywords(message)) {
                String promptText = getSystemPrompt(message) +
                        "\n\n情境：未登入用戶詢問訂位相關服務。請根據【權限說明】和【身份驗證處理】的指引回應。";

                Prompt prompt = new Prompt(promptText);
                ChatResponse response = chatClient.call(prompt);
                return response.getResult().getOutput().getContent();
            }

            // 檢查訂位相關意圖
            if (userId != null) {
                if (aiReservationService.isReservationCancel(message)) {
                    return aiReservationService.handleReservationCancel(userId, message);
                }
                else if (aiReservationService.isReservationQuery(message)) {
                    return aiReservationService.handleReservationQuery(userId, message);
                }
                else if (aiReservationService.isReservationIntent(message)) {
                    return aiReservationService.handleReservationIntent(userId, null, message);
                }
            }

            // 檢查是否在訂位流程中
            if (userId != null) {
                ConversationMessage latestState = aiReservationService.getLatestConversationState(userId);
                if (latestState != null && "RESERVATION".equals(latestState.getConversationType())) {
                    return aiReservationService.processReservationInfo(userId, latestState.getConversationId(), message);
                }
            }

            // 生成一般回應
            String promptText = getSystemPrompt(message);
            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                String errorPrompt = getSystemPrompt(message) +
                        "\n\n情境：需要重新確認菜單資訊。請根據【回應指南】友善地請求用戶重新詢問。";

                prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            }

            log.info("生成回應: {}", content);
            return content;

        } catch (Exception e) {
            log.error("生成回應時發生錯誤。用戶訊息: {}", message, e);

            String errorPrompt = getSystemPrompt(message) +
                    "\n\n情境：系統發生錯誤。請根據【回應指南】委婉地告知用戶稍後重試。";

            try {
                Prompt prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            } catch (Exception ex) {
                return "非常抱歉，系統暫時無法處理您的請求。請稍後再試或聯繫我們的服務人員。";
            }
        }
    }

    @Override
    public String generateProductRecommendation(String userPreference, Integer userId) {
        try {
            log.info("開始生成產品推薦，用戶偏好: {}, 用戶ID: {}", userPreference, userId);

            // 處理未登入用戶的訂位意圖
            if (userId == null && containsReservationKeywords(userPreference)) {
                String promptText = getSystemPrompt(userPreference) +
                        "\n\n情境：未登入用戶在詢問推薦時提到訂位。請根據【權限說明】和【推薦策略】回應。";

                Prompt prompt = new Prompt(promptText);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            }

            String promptText = getSystemPrompt(userPreference) +
                    "\n\n情境：用戶詢問餐點推薦。請根據【推薦策略】進行回應。";

            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                String errorPrompt = getSystemPrompt(userPreference) +
                        "\n\n情境：需要重新推薦菜單中的餐點。請根據【推薦策略】重新推薦。";

                prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            }

            log.info("生成推薦: {}", content);
            return content;

        } catch (Exception e) {
            log.error("生成產品推薦時發生錯誤。用戶偏好: {}", userPreference, e);

            String errorPrompt = getSystemPrompt(userPreference) +
                    "\n\n情境：推薦系統發生錯誤。請根據【回應指南】友善地告知用戶參考菜單。";

            try {
                Prompt prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            } catch (Exception ex) {
                return "抱歉，目前無法提供個人化推薦。請參考我們的菜單選項。";
            }
        }
    }

    @Override
    public String generateMenuResponse(String category) {
        try {
            log.info("開始生成菜單回應，類別: {}", category);

            String promptText = getSystemPrompt("類別：" + category) +
                    "\n\n情境：用戶詢問特定類別的菜單。請根據【菜單資訊】介紹相關餐點。";

            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                String errorPrompt = getSystemPrompt(category) +
                        "\n\n情境：需要重新介紹菜單類別。請根據【回應指南】重新引導用戶。";

                prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            }

            log.info("生成菜單回應: {}", content);
            return content;

        } catch (Exception e) {
            log.error("生成菜單回應時發生錯誤。類別: {}", category, e);

            String errorPrompt = getSystemPrompt(category) +
                    "\n\n情境：菜單查詢系統發生錯誤。請根據【回應指南】友善地告知用戶稍後再試。";

            try {
                Prompt prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            } catch (Exception ex) {
                return "抱歉，目前無法顯示該類別的菜單資訊。";
            }
        }
    }

    @Override
    public String generatePriceQuery(String productName) {
        try {
            log.info("開始生成價格查詢回應，商品名稱: {}", productName);
            Product product = menuContextService.getProduct(productName);

            String promptText;
            if (product != null) {
                log.info("找到產品: {}, 價格: {}", productName, product.getPrice());
                promptText = getSystemPrompt("價格查詢：" + productName) +
                        String.format("\n\n情境：用戶詢問「%s」的價格（$%d）。請根據【價格表示】和【回應指南】回應。",
                                productName, product.getPrice());
            } else {
                log.warn("未找到產品: {}", productName);
                promptText = getSystemPrompt("價格查詢：" + productName) +
                        String.format("\n\n情境：用戶詢問不存在的餐點「%s」的價格。請根據【特殊情況】的指引回應。",
                                productName);
            }

            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                String errorPrompt = getSystemPrompt(productName) +
                        "\n\n情境：需要重新確認價格資訊。請根據【回應指南】重新引導用戶。";

                prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            }

            log.info("生成價格查詢回應: {}", content);
            return content;

        } catch (Exception e) {
            log.error("查詢價格時發生錯誤。商品名稱: {}", productName, e);

            String errorPrompt = getSystemPrompt(productName) +
                    "\n\n情境：價格查詢系統發生錯誤。請根據【回應指南】友善地告知用戶稍後再試。";

            try {
                Prompt prompt = new Prompt(errorPrompt);
                return chatClient.call(prompt).getResult().getOutput().getContent();
            } catch (Exception ex) {
                return "抱歉，目前無法查詢該商品的價格資訊。";
            }
        }
    }

    private boolean validateResponse(String response) {
        String lowerResponse = response.toLowerCase();
        List<Product> allProducts = menuContextService.getAllProducts();

        // 檢查價格格式和範圍
        if (response.contains("$")) {
            String[] parts = response.split("\\$");
            for (int i = 1; i < parts.length; i++) {
                try {
                    String priceStr = parts[i].split("[^0-9,]")[0].replace(",", "");
                    if (!priceStr.isEmpty()) {
                        int price = Integer.parseInt(priceStr);
                        boolean inRange = price >= 0 && price <= 10000;
                        if (!inRange) {
                            log.warn("價格超出合理範圍: ${}", price);
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        // 驗證提到的產品價格是否準確
        if (response.contains("價格是") || response.contains("售價")) {
            for (Product product : allProducts) {
                String productName = product.getProductName().toLowerCase();
                if (lowerResponse.contains(productName)) {
                    String priceStr = "$" + product.getPrice();
                    if (!response.contains(priceStr)) {
                        log.warn("產品價格資訊不準確: {}", product.getProductName());
                        return false;
                    }
                }
            }
        }

        return true;
    }



}