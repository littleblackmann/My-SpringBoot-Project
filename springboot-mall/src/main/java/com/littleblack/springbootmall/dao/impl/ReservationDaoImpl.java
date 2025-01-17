package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.ReservationDao;
import com.littleblack.springbootmall.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ReservationDaoImpl implements ReservationDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public String createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservation_id, user_id, customer_name, reservation_date, " +
                "time_slot_id, guest_count, contact_phone, email, status, cancel_deadline, " +
                "source, special_requests, created_at, updated_at) " +
                "VALUES (:reservationId, :userId, :customerName, :reservationDate, :timeSlotId, " +
                ":guestCount, :contactPhone, :email, :status, :cancelDeadline, " +
                ":source, :specialRequests, :createdAt, :updatedAt)";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservation.getReservationId());
        map.put("userId", reservation.getUserId());
        map.put("customerName", reservation.getCustomerName());
        map.put("reservationDate", reservation.getReservationDate());
        map.put("timeSlotId", reservation.getTimeSlotId());
        map.put("guestCount", reservation.getGuestCount());
        map.put("contactPhone", reservation.getContactPhone());
        map.put("email", reservation.getEmail());
        map.put("status", reservation.getStatus());
        map.put("cancelDeadline", reservation.getCancelDeadline());
        map.put("source", reservation.getSource());
        map.put("specialRequests", reservation.getSpecialRequests());
        map.put("createdAt", LocalDateTime.now());
        map.put("updatedAt", LocalDateTime.now());

        namedParameterJdbcTemplate.update(sql, map);
        return reservation.getReservationId();
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = :reservationId";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservationId);

        List<Reservation> reservationList = namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            Reservation reservation = new Reservation();
            reservation.setReservationId(rs.getString("reservation_id"));
            reservation.setUserId(rs.getInt("user_id"));
            reservation.setCustomerName(rs.getString("customer_name"));
            reservation.setReservationDate(rs.getDate("reservation_date"));
            reservation.setTimeSlotId(rs.getInt("time_slot_id"));
            reservation.setGuestCount(rs.getInt("guest_count"));
            reservation.setContactPhone(rs.getString("contact_phone"));
            reservation.setEmail(rs.getString("email"));
            reservation.setStatus(rs.getInt("status"));
            reservation.setCancelDeadline(rs.getTimestamp("cancel_deadline").toLocalDateTime());
            reservation.setSource(rs.getString("source"));
            reservation.setSpecialRequests(rs.getString("special_requests"));
            reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return reservation;
        });

        if (reservationList.size() > 0) {
            return reservationList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Reservation> getReservationsByUserId(Integer userId) {
        String sql = "SELECT * FROM reservations WHERE user_id = :userId ORDER BY reservation_date DESC";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        return namedParameterJdbcTemplate.query(sql, map, this::mapRowToReservation);
    }

    @Override
    public void updateReservationStatus(String reservationId, Integer status) {
        String sql = "UPDATE reservations SET status = :status, updated_at = NOW() " +
                "WHERE reservation_id = :reservationId";

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("reservationId", reservationId);

        namedParameterJdbcTemplate.update(sql, map);
    }

    @Override
    public List<Reservation> getReservationsByDateAndTimeSlot(Date date, Integer timeSlotId) {
        String sql = "SELECT * FROM reservations WHERE reservation_date = :date " +
                "AND time_slot_id = :timeSlotId AND status != 2";

        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("timeSlotId", timeSlotId);

        return namedParameterJdbcTemplate.query(sql, map, this::mapRowToReservation);
    }

    @Override
    public boolean cancelReservation(String reservationId, Integer userId) {
        return cancelReservation(reservationId, userId, null);
    }

    @Override
    public boolean cancelReservation(String reservationId, Integer userId, String cancelReason) {
        String sql = "UPDATE reservations SET status = 2, cancel_reason = :cancelReason, " +
                "updated_at = NOW() " +
                "WHERE reservation_id = :reservationId " +
                "AND user_id = :userId " +
                "AND status != 2 " +
                "AND cancel_deadline > NOW()";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservationId);
        map.put("userId", userId);
        map.put("cancelReason", cancelReason);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, map);
        return rowsAffected > 0;
    }

    @Override
    public boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                                   String contactPhone, String cancelReason) {
        String sql = "UPDATE reservations SET status = 2, cancel_reason = :cancelReason, " +
                "updated_at = NOW() " +
                "WHERE reservation_id = :reservationId " +
                "AND customer_name = :customerName " +
                "AND contact_phone = :contactPhone " +
                "AND status != 2 " +
                "AND cancel_deadline > NOW()";

        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservationId);
        map.put("customerName", customerName);
        map.put("contactPhone", contactPhone);
        map.put("cancelReason", cancelReason);

        int rowsAffected = namedParameterJdbcTemplate.update(sql, map);
        return rowsAffected > 0;
    }

    @Override
    public List<Reservation> queryReservations(
            LocalDate startDate,
            LocalDate endDate,
            Integer userId,
            String customerName,
            String contactPhone,
            String email,
            Integer status) {

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT * FROM reservations WHERE 1=1");

        // 修改：確保姓名和電話條件一定會被加入
        sql.append(" AND customer_name = :customerName");
        parameters.addValue("customerName", customerName);

        sql.append(" AND contact_phone = :contactPhone");
        parameters.addValue("contactPhone", contactPhone);

        if (startDate != null) {
            sql.append(" AND reservation_date >= :startDate");
            parameters.addValue("startDate", java.sql.Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND reservation_date <= :endDate");
            parameters.addValue("endDate", java.sql.Date.valueOf(endDate));
        }

        if (userId != null) {
            sql.append(" AND user_id = :userId");
            parameters.addValue("userId", userId);
        }

        if (customerName != null && !customerName.trim().isEmpty()) {
            sql.append(" AND customer_name = :customerName");
            parameters.addValue("customerName", customerName);
        }

        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            sql.append(" AND contact_phone = :contactPhone");
            parameters.addValue("contactPhone", contactPhone);
        }

        if (email != null && !email.trim().isEmpty()) {
            sql.append(" AND email = :email");
            parameters.addValue("email", email);
        }

        if (status != null) {
            sql.append(" AND status = :status");
            parameters.addValue("status", status);
        }

        sql.append(" ORDER BY reservation_date DESC, created_at DESC");

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, this::mapRowToReservation);
    }

    private Reservation mapRowToReservation(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getString("reservation_id"));
        reservation.setUserId(rs.getInt("user_id"));
        reservation.setCustomerName(rs.getString("customer_name"));
        reservation.setReservationDate(rs.getDate("reservation_date"));
        reservation.setTimeSlotId(rs.getInt("time_slot_id"));
        reservation.setGuestCount(rs.getInt("guest_count"));
        reservation.setContactPhone(rs.getString("contact_phone"));
        reservation.setEmail(rs.getString("email"));
        reservation.setStatus(rs.getInt("status"));
        reservation.setCancelDeadline(rs.getTimestamp("cancel_deadline").toLocalDateTime());
        reservation.setSource(rs.getString("source"));
        reservation.setSpecialRequests(rs.getString("special_requests"));
        reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return reservation;
    }
}