package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.ReservationDao;
import com.littleblack.springbootmall.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReservationDaoImpl implements ReservationDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public String createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservation_id, user_id, customer_name, " +
                "reservation_date, time_slot_id, guest_count, contact_phone, email, status, " +
                "cancel_deadline, source, special_requests, created_at, updated_at) " +
                "VALUES (:reservationId, :userId, :customerName, :reservationDate, :timeSlotId, " +
                ":guestCount, :contactPhone, :email, :status, :cancelDeadline, :source, " +
                ":specialRequests, :createdAt, :updatedAt)";

        String reservationId = generateReservationId(reservation.getReservationDate());

        Map<String, Object> params = new HashMap<>();
        params.put("reservationId", reservationId);
        params.put("userId", reservation.getUserId());
        params.put("customerName", reservation.getCustomerName());
        params.put("reservationDate", reservation.getReservationDate());
        params.put("timeSlotId", reservation.getTimeSlotId());
        params.put("guestCount", reservation.getGuestCount());
        params.put("contactPhone", reservation.getContactPhone());
        params.put("email", reservation.getEmail());
        params.put("status", reservation.getStatus());
        params.put("cancelDeadline", Timestamp.valueOf(reservation.getCancelDeadline()));
        params.put("source", reservation.getSource());
        params.put("specialRequests", reservation.getSpecialRequests());
        params.put("createdAt", Timestamp.valueOf(LocalDateTime.now()));
        params.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

        namedParameterJdbcTemplate.update(sql, params);

        return reservationId;
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = :reservationId";

        Map<String, Object> params = new HashMap<>();
        params.put("reservationId", reservationId);

        List<Reservation> reservations = namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> {
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

        return reservations.isEmpty() ? null : reservations.get(0);
    }

    @Override
    public List<Reservation> getReservationsByUserId(Integer userId) {
        String sql = "SELECT * FROM reservations WHERE user_id = :userId ORDER BY reservation_date DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
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
    }

    @Override
    public void updateReservationStatus(String reservationId, Integer status) {
        String sql = "UPDATE reservations SET status = :status, updated_at = :updatedAt " +
                "WHERE reservation_id = :reservationId";

        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));
        params.put("reservationId", reservationId);

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<Reservation> getReservationsByDateAndTimeSlot(java.util.Date date, Integer timeSlotId) {
        String sql = "SELECT * FROM reservations WHERE reservation_date = :date " +
                "AND time_slot_id = :timeSlotId AND status != 2";

        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        params.put("timeSlotId", timeSlotId);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Reservation reservation = new Reservation();
            // ... (同上的設置方法)
            return reservation;
        });
    }

    @Override
    public boolean cancelReservation(String reservationId, Integer userId) {
        return cancelReservation(reservationId, userId, null);
    }

    @Override
    public boolean cancelReservation(String reservationId, Integer userId, String cancelReason) {
        String sql = "UPDATE reservations SET status = 2, cancel_reason = :cancelReason, " +
                "updated_at = :updatedAt WHERE reservation_id = :reservationId " +
                "AND user_id = :userId AND status != 2 " +
                "AND cancel_deadline > :now";

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", cancelReason);
        params.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));
        params.put("reservationId", reservationId);
        params.put("userId", userId);
        params.put("now", Timestamp.valueOf(LocalDateTime.now()));

        return namedParameterJdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                                   String contactPhone, String cancelReason) {
        String sql = "UPDATE reservations SET status = 2, cancel_reason = :cancelReason, " +
                "updated_at = :updatedAt WHERE reservation_id = :reservationId " +
                "AND customer_name = :customerName AND contact_phone = :contactPhone " +
                "AND status != 2 AND cancel_deadline > :now";

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", cancelReason);
        params.put("updatedAt", Timestamp.valueOf(LocalDateTime.now()));
        params.put("reservationId", reservationId);
        params.put("customerName", customerName);
        params.put("contactPhone", contactPhone);
        params.put("now", Timestamp.valueOf(LocalDateTime.now()));

        return namedParameterJdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public Map<Integer, Integer> getReservedSeatsByDate(LocalDate date) {
        String sql = "SELECT time_slot_id, SUM(guest_count) as total_guests " +
                "FROM reservations WHERE reservation_date = :date " +
                "AND status != 2 GROUP BY time_slot_id";

        Map<String, Object> params = new HashMap<>();
        params.put("date", Date.valueOf(date));

        Map<Integer, Integer> reservedSeats = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, params, (rs) -> {
            int timeSlotId = rs.getInt("time_slot_id");
            int totalGuests = rs.getInt("total_guests");
            reservedSeats.put(timeSlotId, totalGuests);
        });

        return reservedSeats;
    }

    @Override
    public List<Reservation> queryReservations(LocalDate startDate, LocalDate endDate,
                                               Integer userId, String customerName,
                                               String contactPhone, String email, Integer status) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("SELECT * FROM reservations WHERE 1=1");

        if (startDate != null) {
            sql.append(" AND reservation_date >= :startDate");
            params.addValue("startDate", Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND reservation_date <= :endDate");
            params.addValue("endDate", Date.valueOf(endDate));
        }
        if (userId != null) {
            sql.append(" AND user_id = :userId");
            params.addValue("userId", userId);
        }
        if (customerName != null) {
            sql.append(" AND customer_name LIKE :customerName");
            params.addValue("customerName", "%" + customerName + "%");
        }
        if (contactPhone != null) {
            sql.append(" AND contact_phone = :contactPhone");
            params.addValue("contactPhone", contactPhone);
        }
        if (email != null) {
            sql.append(" AND email = :email");
            params.addValue("email", email);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.addValue("status", status);
        }

        sql.append(" ORDER BY reservation_date DESC, time_slot_id ASC");

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
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
    }

    private String generateReservationId(java.util.Date reservationDate) {
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(reservationDate);
        String sql = "SELECT COUNT(*) FROM reservations WHERE reservation_id LIKE :prefix";

        Map<String, Object> params = new HashMap<>();
        params.put("prefix", datePart + "%");

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        count = (count == null) ? 0 : count;

        return String.format("%s%04d", datePart, count + 1);
    }
}
