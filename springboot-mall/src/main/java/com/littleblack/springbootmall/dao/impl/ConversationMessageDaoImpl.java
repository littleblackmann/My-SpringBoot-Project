package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.ConversationMessageDao;
import com.littleblack.springbootmall.model.ConversationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConversationMessageDaoImpl implements ConversationMessageDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void createMessage(ConversationMessage message) {
        String sql = "INSERT INTO conversation_messages (conversation_id, user_id, content, role, " +
                "conversation_type, reservation_stage, related_reservation_id, created_at, updated_at) " +
                "VALUES (:conversationId, :userId, :content, :role, :conversationType, " +
                ":reservationStage, :relatedReservationId, :createdAt, :updatedAt)";

        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", message.getConversationId());
        map.put("userId", message.getUserId());
        map.put("content", message.getContent());
        map.put("role", message.getRole());
        map.put("conversationType", message.getConversationType());
        map.put("reservationStage", message.getReservationStage());
        map.put("relatedReservationId", message.getRelatedReservationId());
        map.put("createdAt", Timestamp.valueOf(LocalDateTime.now()));
        map.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        message.setMessageId(keyHolder.getKey().longValue());
    }

    @Override
    public List<ConversationMessage> getMessagesByConversationId(String conversationId) {
        String sql = "SELECT * FROM conversation_messages WHERE conversation_id = :conversationId " +
                "ORDER BY created_at ASC";

        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", conversationId);

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            ConversationMessage message = new ConversationMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setConversationId(rs.getString("conversation_id"));
            message.setUserId(rs.getInt("user_id"));
            message.setContent(rs.getString("content"));
            message.setRole(rs.getString("role"));
            message.setConversationType(rs.getString("conversation_type"));
            message.setReservationStage(rs.getString("reservation_stage"));
            message.setRelatedReservationId(rs.getString("related_reservation_id"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return message;
        });
    }

    @Override
    public ConversationMessage getLatestMessageByUserId(Integer userId) {
        String sql = "SELECT * FROM conversation_messages WHERE user_id = :userId " +
                "ORDER BY created_at DESC LIMIT 1";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        List<ConversationMessage> messages = namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            ConversationMessage message = new ConversationMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setConversationId(rs.getString("conversation_id"));
            message.setUserId(rs.getInt("user_id"));
            message.setContent(rs.getString("content"));
            message.setRole(rs.getString("role"));
            message.setConversationType(rs.getString("conversation_type"));
            message.setReservationStage(rs.getString("reservation_stage"));
            message.setRelatedReservationId(rs.getString("related_reservation_id"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return message;
        });

        return messages.isEmpty() ? null : messages.get(0);
    }

    @Override
    public List<ConversationMessage> getMessagesByUserIdAndType(Integer userId, String conversationType) {
        String sql = "SELECT * FROM conversation_messages WHERE user_id = :userId " +
                "AND conversation_type = :conversationType ORDER BY created_at ASC";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("conversationType", conversationType);

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            ConversationMessage message = new ConversationMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setConversationId(rs.getString("conversation_id"));
            message.setUserId(rs.getInt("user_id"));
            message.setContent(rs.getString("content"));
            message.setRole(rs.getString("role"));
            message.setConversationType(rs.getString("conversation_type"));
            message.setReservationStage(rs.getString("reservation_stage"));
            message.setRelatedReservationId(rs.getString("related_reservation_id"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return message;
        });
    }

    @Override
    public void updateReservationStage(Long messageId, String reservationStage) {
        String sql = "UPDATE conversation_messages SET reservation_stage = :reservationStage, " +
                "updated_at = :updatedAt WHERE message_id = :messageId";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationStage", reservationStage);
        map.put("messageId", messageId);
        map.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public void updateRelatedReservationId(Long messageId, String reservationId) {
        String sql = "UPDATE conversation_messages SET related_reservation_id = :reservationId, " +
                "updated_at = :updatedAt WHERE message_id = :messageId";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservationId);
        map.put("messageId", messageId);
        map.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public List<ConversationMessage> getReservationConversation(String reservationId) {
        String sql = "SELECT * FROM conversation_messages WHERE related_reservation_id = :reservationId " +
                "ORDER BY created_at ASC";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservationId);

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            ConversationMessage message = new ConversationMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setConversationId(rs.getString("conversation_id"));
            message.setUserId(rs.getInt("user_id"));
            message.setContent(rs.getString("content"));
            message.setRole(rs.getString("role"));
            message.setConversationType(rs.getString("conversation_type"));
            message.setReservationStage(rs.getString("reservation_stage"));
            message.setRelatedReservationId(rs.getString("related_reservation_id"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return message;
        });
    }

    @Override
    public ConversationMessage getLatestMessageByConversationId(String conversationId) {
        String sql = "SELECT * FROM conversation_messages WHERE conversation_id = :conversationId " +
                "ORDER BY created_at DESC LIMIT 1";

        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", conversationId);

        List<ConversationMessage> messages = namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            ConversationMessage message = new ConversationMessage();
            message.setMessageId(rs.getLong("message_id"));
            message.setConversationId(rs.getString("conversation_id"));
            message.setUserId(rs.getInt("user_id"));
            message.setContent(rs.getString("content"));
            message.setRole(rs.getString("role"));
            message.setConversationType(rs.getString("conversation_type"));
            message.setReservationStage(rs.getString("reservation_stage"));
            message.setRelatedReservationId(rs.getString("related_reservation_id"));
            message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return message;
        });

        return messages.isEmpty() ? null : messages.get(0);
    }

}