package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.ConversationMessage;

public interface AIReservationService {

    /**
     * 處理新的訂位意圖
     *
     * @param userId 用戶ID
     * @param conversationId 對話ID，可為空
     * @param userMessage 用戶輸入的訊息
     * @return 系統回應訊息
     */
    String handleReservationIntent(Integer userId, String conversationId, String userMessage);

    /**
     * 處理訂位過程中的資訊輸入
     *
     * @param userId 用戶ID
     * @param conversationId 對話ID
     * @param userMessage 用戶輸入的訊息
     * @return 系統回應訊息
     */
    String processReservationInfo(Integer userId, String conversationId, String userMessage);

    /**
     * 啟動訂位查詢流程
     *
     * @param userId 用戶ID
     * @param userMessage 用戶輸入的訊息
     * @return 系統回應訊息
     */
    String handleReservationQuery(Integer userId, String userMessage);

    /**
     * 處理訂位查詢過程中的資訊輸入
     *
     * @param userId 用戶ID
     * @param conversationId 對話ID
     * @param userMessage 用戶輸入的訊息
     * @return 系統回應訊息
     */
    String processReservationQuery(Integer userId, String conversationId, String userMessage);

    /**
     * 處理取消訂位的請求
     *
     * @param userId 用戶ID
     * @param userMessage 用戶輸入的訊息
     * @return 系統回應訊息
     */
    String handleReservationCancel(Integer userId, String userMessage);

    /**
     * 獲取用戶最新的對話狀態
     *
     * @param userId 用戶ID
     * @return 最新的對話狀態
     */
    ConversationMessage getLatestConversationState(Integer userId);

    /**
     * 判斷是否為訂位意圖
     *
     * @param message 用戶輸入的訊息
     * @return true 如果是訂位意圖，否則返回 false
     */
    boolean isReservationIntent(String message);

    /**
     * 判斷是否為查詢訂位意圖
     *
     * @param message 用戶輸入的訊息
     * @return true 如果是查詢意圖，否則返回 false
     */
    boolean isReservationQuery(String message);

    /**
     * 判斷是否為取消訂位意圖
     *
     * @param message 用戶輸入的訊息
     * @return true 如果是取消意圖，否則返回 false
     */
    boolean isReservationCancel(String message);
}