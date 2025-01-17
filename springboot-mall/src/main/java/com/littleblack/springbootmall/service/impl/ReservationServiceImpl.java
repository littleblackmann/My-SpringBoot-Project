package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.ReservationDao;
import com.littleblack.springbootmall.model.Reservation;
import com.littleblack.springbootmall.service.ReservationService;
import com.littleblack.springbootmall.service.TimeSlotService;
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

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private JavaMailSender emailSender;

    @Override
    @Transactional
    public String createReservation(Reservation reservation) {
        String dateStr = DateTimeFormatter.ofPattern("yyyyMMdd")
                .format(LocalDateTime.now());
        String reservationId = dateStr + String.format("%04d",
                (int) (Math.random() * 10000));

        reservation.setReservationId(reservationId);
        reservation.setStatus(1); // 設定初始狀態為已確認，0=待確認，1=已確認，2=已取消

        LocalDateTime cancelDeadline = LocalDateTime.now().plusDays(1);
        reservation.setCancelDeadline(cancelDeadline);

        reservationDao.createReservation(reservation);
        sendReservationConfirmation(reservation);

        return reservationId;
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        return reservationDao.getReservationById(reservationId);
    }

    @Override
    public List<Reservation> getUserReservations(Integer userId) {
        return reservationDao.getReservationsByUserId(userId);
    }

    @Override
    @Transactional
    public boolean cancelReservation(String reservationId, Integer userId) {
        return cancelReservation(reservationId, userId, null);
    }

    @Override
    @Transactional
    public boolean cancelReservation(String reservationId, Integer userId, String cancelReason) {
        boolean cancelled = reservationDao.cancelReservation(reservationId, userId, cancelReason);
        if (cancelled) {
            Reservation reservation = getReservationById(reservationId);
            sendCancellationConfirmation(reservation);
        }
        return cancelled;
    }

    @Override
    @Transactional
    public boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                                   String contactPhone, String cancelReason) {
        boolean cancelled = reservationDao.cancelReservationByCustomerInfo(
                reservationId, customerName, contactPhone, cancelReason);
        if (cancelled) {
            Reservation reservation = getReservationById(reservationId);
            sendCancellationConfirmation(reservation);
        }
        return cancelled;
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

        if (customerName == null || customerName.trim().isEmpty() ||
                contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("訂位姓名和聯絡電話為必填項目");
        }

        return reservationDao.queryReservations(
                startDate, endDate, userId, customerName, contactPhone, email, status);
    }

    @Override
    public void sendReservationConfirmation(Reservation reservation) {
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
    }

    private void sendCancellationConfirmation(Reservation reservation) {
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
    }
}