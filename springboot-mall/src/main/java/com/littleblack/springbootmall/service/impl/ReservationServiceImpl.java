package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.ReservationDao;
import com.littleblack.springbootmall.model.Reservation;
import com.littleblack.springbootmall.service.ReservationService;
import com.littleblack.springbootmall.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private JavaMailSender emailSender;

    @Override
    @Transactional
    public String createReservation(Reservation reservation) {
        try {
            validateReservation(reservation);

            String dateStr = DateTimeFormatter.ofPattern("yyyyMMdd")
                    .format(LocalDateTime.now());
            String reservationId = dateStr + String.format("%04d",
                    (int) (Math.random() * 10000));

            setupReservation(reservation, reservationId);

            log.info("Creating reservation for customer: {}, Date: {}",
                    reservation.getCustomerName(),
                    reservation.getReservationDate());

            reservationDao.createReservation(reservation);

            try {
                sendReservationConfirmation(reservation);
                log.info("Confirmation email sent for reservation: {}", reservationId);
            } catch (Exception e) {
                log.error("Failed to send confirmation email for reservation: {}", reservationId, e);
            }

            return reservationId;

        } catch (Exception e) {
            log.error("Failed to create reservation", e);
            throw new RuntimeException("建立訂位時發生錯誤: " + e.getMessage());
        }
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        try {
            if (reservationId == null || reservationId.trim().isEmpty()) {
                throw new IllegalArgumentException("訂位編號不能為空");
            }

            Reservation reservation = reservationDao.getReservationById(reservationId);

            if (reservation == null) {
                log.warn("No reservation found with ID: {}", reservationId);
            }

            return reservation;

        } catch (Exception e) {
            log.error("Error retrieving reservation with ID: {}", reservationId, e);
            throw new RuntimeException("查詢訂位資料時發生錯誤");
        }
    }

    @Override
    public List<Reservation> getUserReservations(Integer userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("用戶ID不能為空");
            }

            List<Reservation> reservations = reservationDao.getReservationsByUserId(userId);

            if (reservations == null || reservations.isEmpty()) {
                log.info("No reservations found for user ID: {}", userId);
            }

            return reservations;

        } catch (Exception e) {
            log.error("Error retrieving reservations for user: {}", userId, e);
            throw new RuntimeException("查詢用戶訂位資料時發生錯誤");
        }
    }

    @Override
    @Transactional
    public boolean cancelReservation(String reservationId, Integer userId) {
        return cancelReservation(reservationId, userId, null);
    }

    @Override
    @Transactional
    public boolean cancelReservation(String reservationId, Integer userId, String cancelReason) {
        try {
            if (reservationId == null || userId == null) {
                throw new IllegalArgumentException("訂位編號和用戶ID不能為空");
            }

            log.info("Attempting to cancel reservation: {} for user: {}", reservationId, userId);

            boolean cancelled = reservationDao.cancelReservation(reservationId, userId, cancelReason);

            if (cancelled) {
                Reservation reservation = getReservationById(reservationId);
                try {
                    sendCancellationConfirmation(reservation);
                    log.info("Cancellation confirmation email sent for reservation: {}", reservationId);
                } catch (Exception e) {
                    log.error("Failed to send cancellation email for reservation: {}", reservationId, e);
                }
            }

            return cancelled;

        } catch (Exception e) {
            log.error("Error cancelling reservation: {} for user: {}", reservationId, userId, e);
            throw new RuntimeException("取消訂位時發生錯誤");
        }
    }

    @Override
    @Transactional
    public boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                                   String contactPhone, String cancelReason) {
        try {
            validateCancellationInfo(reservationId, customerName, contactPhone);

            log.info("Attempting to cancel reservation: {} for customer: {}",
                    reservationId, customerName);

            boolean cancelled = reservationDao.cancelReservationByCustomerInfo(
                    reservationId, customerName, contactPhone, cancelReason);

            if (cancelled) {
                Reservation reservation = getReservationById(reservationId);
                try {
                    sendCancellationConfirmation(reservation);
                    log.info("Cancellation confirmation email sent for reservation: {}", reservationId);
                } catch (Exception e) {
                    log.error("Failed to send cancellation email for reservation: {}", reservationId, e);
                }
            }

            return cancelled;

        } catch (Exception e) {
            log.error("Error cancelling reservation: {} for customer: {}", reservationId, customerName, e);
            throw new RuntimeException("取消訂位時發生錯誤");
        }
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

        try {
            validateQueryParams(customerName, contactPhone);

            List<Reservation> reservations = reservationDao.queryReservations(
                    startDate, endDate, userId, customerName, contactPhone, email, status);

            if (reservations == null || reservations.isEmpty()) {
                log.info("No reservations found for the given criteria");
            }

            return reservations;

        } catch (Exception e) {
            log.error("Error querying reservations", e);
            throw new RuntimeException("查詢訂位資料時發生錯誤");
        }
    }

    @Override
    public void sendReservationConfirmation(Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reservation.getEmail());
            message.setSubject("訂位確認通知");
            message.setText(String.format(
                    "親愛的 %s 您好，\n\n" +
                            "您的訂位已確認，訂位詳情如下：\n" +
                            "訂位編號：%s\n" +
                            "預約日期：%s\n" +
                            "用餐人數：%d人\n\n" +
                            "如需取消訂位，請於用餐時間24小時前取消。\n" +
                            "期待您的光臨！",
                    reservation.getCustomerName(),
                    reservation.getReservationId(),
                    reservation.getReservationDate(),
                    reservation.getGuestCount()
            ));

            emailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
            throw new RuntimeException("發送確認郵件時發生錯誤");
        }
    }

    private void sendCancellationConfirmation(Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reservation.getEmail());
            message.setSubject("訂位取消確認通知");
            message.setText(String.format(
                    "親愛的 %s 您好，\n\n" +
                            "您的訂位已成功取消，訂位詳情如下：\n" +
                            "訂位編號：%s\n" +
                            "預約日期：%s\n" +
                            "用餐人數：%d人\n\n" +
                            "歡迎您再次預約！",
                    reservation.getCustomerName(),
                    reservation.getReservationId(),
                    reservation.getReservationDate(),
                    reservation.getGuestCount()
            ));

            emailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send cancellation email", e);
            throw new RuntimeException("發送取消確認郵件時發生錯誤");
        }
    }

    private void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("預約資料不能為空");
        }
        if (reservation.getReservationDate() == null) {
            throw new IllegalArgumentException("預約日期不能為空");
        }
        if (reservation.getCustomerName() == null || reservation.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("顧客姓名不能為空");
        }
        if (reservation.getContactPhone() == null || reservation.getContactPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("聯絡電話不能為空");
        }
        if (reservation.getTimeSlotId() == null) {
            throw new IllegalArgumentException("時段編號不能為空");
        }
        if (reservation.getGuestCount() == null || reservation.getGuestCount() <= 0) {
            throw new IllegalArgumentException("用餐人數必須大於0");
        }
    }

    private void validateCancellationInfo(String reservationId, String customerName, String contactPhone) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("訂位編號不能為空");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("顧客姓名不能為空");
        }
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("聯絡電話不能為空");
        }
    }

    private void validateQueryParams(String customerName, String contactPhone) {
        if (customerName == null || customerName.trim().isEmpty() ||
                contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("訂位姓名和聯絡電話為必填項目");
        }
    }

    private void setupReservation(Reservation reservation, String reservationId) {
        reservation.setReservationId(reservationId);
        reservation.setStatus(1); // 設定初始狀態為已確認

        LocalDateTime now = LocalDateTime.now();
        if (reservation.getCreatedAt() == null) {
            reservation.setCreatedAt(now);
        }
        if (reservation.getUpdatedAt() == null) {
            reservation.setUpdatedAt(now);
        }

        LocalDateTime cancelDeadline = now.plusDays(1);
        reservation.setCancelDeadline(cancelDeadline);
    }
}