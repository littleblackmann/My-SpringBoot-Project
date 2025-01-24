package com.littleblack.springbootmall.service;

public interface AIService {

    /**
     * 生成對話回應
     * @param message 用戶訊息
     * @param userId 用戶ID（可為null，代表非會員）
     * @return 回應內容
     */
    String generateResponse(String message, Integer userId);

    /**
     * 生成產品推薦
     * @param userPreference 用戶偏好
     * @param userId 用戶ID（可為null，代表非會員）
     * @return 推薦內容
     */
    String generateProductRecommendation(String userPreference, Integer userId);

    /**
     * 生成菜單回應
     * @param category 菜單類別
     * @return 菜單資訊
     */
    String generateMenuResponse(String category);

    /**
     * 生成價格查詢回應
     * @param productName 產品名稱
     * @return 價格資訊
     */
    String generatePriceQuery(String productName);

    /**
     * 檢查訊息是否包含訂位相關關鍵字
     * @param message 用戶訊息
     * @return 是否包含訂位關鍵字
     */
    boolean containsReservationKeywords(String message);
}