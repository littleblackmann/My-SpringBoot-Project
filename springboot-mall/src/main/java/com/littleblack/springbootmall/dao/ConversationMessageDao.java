package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.model.ConversationMessage;

import java.util.List;

public interface ConversationMessageDao {
    void createMessage(ConversationMessage message);

    List<ConversationMessage> getMessagesByConversationId(String conversationId);

    ConversationMessage getLatestMessageByUserId(Integer userId);

    List<ConversationMessage> getMessagesByUserIdAndType(Integer userId, String conversationType);

    void updateReservationStage(Long messageId, String reservationStage);

    void updateRelatedReservationId(Long messageId, String reservationId);

    List<ConversationMessage> getReservationConversation(String reservationId);

    ConversationMessage getLatestMessageByConversationId(String conversationId);


}