package com.littleblack.springbootmall.model;

import java.time.LocalDateTime;

public class ConversationMessage {
    private Long messageId;
    private String conversationId;
    private Integer userId;
    private String content;
    private String role;
    private String conversationType;
    private String reservationStage;
    private String relatedReservationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters
    public Long getMessageId() {
        return messageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getContent() {
        return content;
    }

    public String getRole() {
        return role;
    }

    public String getConversationType() {
        return conversationType;
    }

    public String getReservationStage() {
        return reservationStage;
    }

    public String getRelatedReservationId() {
        return relatedReservationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public void setReservationStage(String reservationStage) {
        this.reservationStage = reservationStage;
    }

    public void setRelatedReservationId(String relatedReservationId) {
        this.relatedReservationId = relatedReservationId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ConversationMessage{" +
                "messageId=" + messageId +
                ", userId=" + userId +
                ", conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", role='" + role + '\'' +
                ", conversationType='" + conversationType + '\'' +
                ", reservationStage='" + reservationStage + '\'' +
                ", relatedReservationId='" + relatedReservationId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}