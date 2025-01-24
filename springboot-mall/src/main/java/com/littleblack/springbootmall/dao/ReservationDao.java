package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Date;

public interface ReservationDao {
    String createReservation(Reservation reservation);

    Reservation getReservationById(String reservationId);

    List<Reservation> getReservationsByUserId(Integer userId);

    void updateReservationStatus(String reservationId, Integer status);

    List<Reservation> getReservationsByDateAndTimeSlot(Date date, Integer timeSlotId);

    boolean cancelReservation(String reservationId, Integer userId);

    boolean cancelReservation(String reservationId, Integer userId, String cancelReason);

    boolean cancelReservationByCustomerInfo(String reservationId, String customerName,
                                            String contactPhone, String cancelReason);

    List<Reservation> queryReservations(
            LocalDate startDate,
            LocalDate endDate,
            Integer userId,
            String customerName,
            String contactPhone,
            String email,
            Integer status);

    // 新增這個方法
    Map<Integer, Integer> getReservedSeatsByDate(LocalDate date);
}
