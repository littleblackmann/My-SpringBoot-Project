package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.model.Product;
import com.littleblack.springbootmall.service.AIService;
import com.littleblack.springbootmall.service.MenuContextService;
import com.littleblack.springbootmall.service.RAGService;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final OpenAiChatClient chatClient;
    private final MenuContextService menuContextService;
    private final RAGService ragService;

    public AIServiceImpl(OpenAiChatClient chatClient,
                         MenuContextService menuContextService,
                         RAGService ragService) {
        this.chatClient = chatClient;
        this.menuContextService = menuContextService;
        this.ragService = ragService;
    }

    private String getSystemPrompt(String userQuery) {
        String menuInfo = menuContextService.getMenuContext();
        String relevantContext = ragService.generateEnhancedPrompt(userQuery);

        return """
系統指令：你是 ZERO Supper 餐廳的 AI 客服。請遵守以下指令：

【菜單資訊】
%s

【推薦餐點】
%s

【回應指南】
1. 回答風格：
   - 使用親切、專業的口吻
   - 主動推薦適合的餐點
   - 提供簡潔且有用的資訊

2. 價格表示：
   - 統一使用「$XXX」格式
   - 確保價格準確無誤

3. 特殊情況：
   - 如果客人詢問的餐點不在菜單中，請禮貌的推薦相關餐點
   - 優先推薦上述「推薦餐點」中的選項

4. 飲食需求處理：
   - 若客人提到他是吃素食，請回應：
                      1. 表達理解客人的需求
                      2. 說明目前餐廳正在積極研發素食餐點
                      3. 建議客人關注我們的最新消息，未來會推出素食相關餐點
                      4. 不需要給出替代建議，如建議他們可以參考附近的素食餐廳
   - 若客人表明不吃牛肉，優先推薦豬肉跟雞肉就好
   - 若客人提到過敏，請注意堅果類的食物
   - 遇到特殊飲食需求時，主動詢問是否需要客製化調整

5. 食材說明：
   - 主動說明餐點的主要食材
   - 提供食材的特色說明
   - 若客人詢問特定食材，優先推薦相關餐點

請記住：你的任務是協助顧客找到最適合的餐點選擇，並提供專業且友善的服務。""".formatted(menuInfo, relevantContext);
    }

    @Override
    public String generateResponse(String message) {
        try {
            log.info("開始生成回應，用戶訊息: {}", message);
            String fullPrompt = getSystemPrompt(message) + "\n\n客人問題：" + message;
            Prompt prompt = new Prompt(fullPrompt);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                log.warn("AI回應包含菜單外項目，重新生成回應");
                return "非常抱歉，讓我重新確認我們的菜單。請問您想了解我們目前提供的哪些餐點呢？";
            }

            log.info("生成回應: {}", content);
            return content;
        } catch (Exception e) {
            log.error("生成回應時發生錯誤。用戶訊息: {}", message, e);
            return "非常抱歉，系統暫時無法處理您的請求。請稍後再試或聯繫我們的服務人員。";
        }
    }

    @Override
    public String generateProductRecommendation(String userPreference) {
        try {
            log.info("開始生成產品推薦，用戶偏好: {}", userPreference);
            String fullPrompt = getSystemPrompt(userPreference) +
                    "\n\n客人偏好：" + userPreference +
                    "\n請根據相關餐點資訊進行推薦，優先推薦最符合客人需求的項目。";

            Prompt prompt = new Prompt(fullPrompt);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                return "抱歉，讓我重新為您推薦我們實際提供的餐點。請告訴我您的喜好，我會從我們的菜單中為您挑選合適的選擇。";
            }

            log.info("生成推薦: {}", content);
            return content;
        } catch (Exception e) {
            log.error("生成產品推薦時發生錯誤。用戶偏好: {}", userPreference, e);
            return "抱歉，目前無法提供個人化推薦。請參考我們的菜單選項。";
        }
    }

    @Override
    public String generateMenuResponse(String category) {
        try {
            String promptText = getSystemPrompt("類別：" + category) +
                    "\n\n請介紹" + category + "類別的菜單項目。請確保只介紹實際存在於菜單中的餐點。";

            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                return "抱歉，讓我重新介紹我們的菜單。請問您想了解哪類餐點？";
            }

            log.info("生成菜單回應: {}", content);
            return content;
        } catch (Exception e) {
            log.error("生成菜單回應時發生錯誤。類別: {}", category, e);
            return "抱歉，目前無法顯示該類別的菜單資訊。";
        }
    }

    @Override
    public String generatePriceQuery(String productName) {
        try {
            Product product = menuContextService.getProduct(productName);
            String promptText;

            if (product != null) {
                log.info("找到產品: {}, 價格: {}", productName, product.getPrice());
                promptText = getSystemPrompt("價格查詢：" + productName) +
                        String.format("\n\n客人詢問「%s」的價格。這道餐點的價格是 $%d。",
                                productName, product.getPrice());
            } else {
                log.warn("未找到產品: {}", productName);
                promptText = getSystemPrompt("價格查詢：" + productName) +
                        String.format("\n\n客人詢問「%s」的價格，但此餐點不在我們的菜單中。",
                                productName);
            }

            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            if (!validateResponse(content)) {
                return "抱歉，讓我重新確認價格資訊。請問您想詢問我們菜單上哪道餐點的價格？";
            }

            log.info("生成價格查詢回應: {}", content);
            return content;
        } catch (Exception e) {
            log.error("查詢價格時發生錯誤。商品名稱: {}", productName, e);
            return "抱歉，目前無法查詢該商品的價格資訊。";
        }
    }

    private boolean validateResponse(String response) {
        String lowerResponse = response.toLowerCase();
        List<Product> allProducts = menuContextService.getAllProducts();

        // 檢查價格標示
        String[] parts = response.split("\\$");
        for (int i = 1; i < parts.length; i++) {
            String priceStr = parts[i].split("[^0-9]")[0];
            if (!priceStr.isEmpty()) {
                int price = Integer.parseInt(priceStr);
                boolean validPrice = allProducts.stream()
                        .anyMatch(product -> product.getPrice() == price);
                if (!validPrice) {
                    log.warn("發現無效價格: ${}", price);
                    return false;
                }
            }
        }

        // 檢查產品名稱和價格配對
        for (Product product : allProducts) {
            if (lowerResponse.contains(product.getProductName().toLowerCase())) {
                String priceStr = "$" + product.getPrice();
                if (!response.contains(priceStr)) {
                    log.warn("產品 {} 的價格不正確", product.getProductName());
                    return false;
                }
            }
        }

        return true;
    }
}