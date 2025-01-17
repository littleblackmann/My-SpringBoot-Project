package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    // 建立訂位
    String createReservation(Reservation reservation);

    // 根據訂位編號獲取訂位
    Reservation getReservationById(String reservationId);

    // 獲取用戶的所有訂位
    List<Reservation> getUserReservations(Integer userId);

    // 使用用戶ID取消訂位（基本方法）
    boolean cancelReservation(String reservationId, Integer userId);

    // 使用用戶ID取消訂位（帶取消原因）
    boolean cancelReservation(String reservationId, Integer userId, String cancelReason);

    // 使用客戶資訊取消訂位（新增方法）
    boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                            String contactPhone, String cancelReason);

    // 多條件查詢訂位
    List<Reservation> queryReservations(
            LocalDate startDate,
            LocalDate endDate,
            Integer userId,
            String customerName,
            String contactPhone,
            String email,
            Integer status);

    // 發送訂位確認郵件
    void sendReservationConfirmation(Reservation reservation);
}