package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.ReservationRequest;
import com.littleblack.springbootmall.dto.ReservationResponse;
import com.littleblack.springbootmall.dto.ReservationQueryRequest;
import com.littleblack.springbootmall.dto.ReservationCancelRequest;
import com.littleblack.springbootmall.model.Reservation;
import com.littleblack.springbootmall.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);


    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        log.info("Received reservation request for customer: {}", request.getCustomerName());

        try {
            validateReservationRequest(request);

            Reservation reservation = new Reservation();
            reservation.setUserId(request.getUserId());
            reservation.setCustomerName(request.getCustomerName());
            reservation.setReservationDate(Date.valueOf(request.getReservationDate()));
            reservation.setTimeSlotId(request.getTimeSlotId());
            reservation.setGuestCount(request.getGuestCount());
            reservation.setContactPhone(request.getContactPhone());
            reservation.setEmail(request.getEmail());
            reservation.setSpecialRequests(request.getSpecialRequests());

            String reservationId = reservationService.createReservation(reservation);

            if (reservationId == null) {
                throw new RuntimeException("建立訂位失敗");
            }

            Reservation createdReservation = reservationService.getReservationById(reservationId);
            ReservationResponse responseData = convertToReservationResponse(createdReservation);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "訂位成功！");
            response.put("data", responseData);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid reservation request: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating reservation", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "建立訂位時發生錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private void validateReservationRequest(ReservationRequest request) {
        if (request.getReservationDate() == null) {
            throw new IllegalArgumentException("預約日期不能為空");
        }
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("顧客姓名不能為空");
        }
        if (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("聯絡電話不能為空");
        }
        if (request.getTimeSlotId() == null) {
            throw new IllegalArgumentException("時段不能為空");
        }
        if (request.getGuestCount() == null || request.getGuestCount() <= 0) {
            throw new IllegalArgumentException("用餐人數必須大於0");
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> queryReservations(
            @Valid ReservationQueryRequest request) {

        if ((request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) ||
                (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty())) {
            throw new IllegalArgumentException("訂位姓名和聯絡電話為必填項目");
        }

        List<Reservation> reservations = reservationService.queryReservations(
                request.getStartDate(),
                request.getEndDate(),
                request.getUserId(),
                request.getCustomerName(),
                request.getContactPhone(),
                request.getEmail(),
                request.getStatus()
        );

        List<ReservationResponse> responseList = reservations.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable String reservationId) {

        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToReservationResponse(reservation));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @Valid @RequestBody ReservationCancelRequest request) {

        System.out.println("收到取消訂位請求: " + request.getReservationId() +
                ", userId: " + request.getUserId() +
                ", cancelReason: " + request.getCancelReason());

        if (request.getUserId() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "未提供用戶ID，無法取消訂位");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        boolean cancelled;

        if (request.getUserId() != null) {
            cancelled = reservationService.cancelReservation(
                    request.getReservationId(),
                    request.getUserId(),
                    request.getCancelReason()
            );
        } else {
            cancelled = reservationService.cancelReservationByCustomerInfo(
                    request.getReservationId(),
                    request.getCustomerName(),
                    request.getContactPhone(),
                    request.getCancelReason()
            );
        }

        if (!cancelled) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "取消訂位失敗，請確認訂位資訊是否正確或是否已超過取消期限");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "訂位已成功取消");

        return ResponseEntity.ok(response);
    }

    private ReservationResponse convertToReservationResponse(Reservation reservation) {
        if (reservation == null) {
            log.warn("Attempting to convert null reservation to response");
            return null;
        }

        ReservationResponse response = new ReservationResponse();

        try {
            response.setReservationId(reservation.getReservationId());
            response.setUserId(reservation.getUserId());
            response.setCustomerName(reservation.getCustomerName());

            if (reservation.getReservationDate() != null) {
                response.setReservationDate(Instant.ofEpochMilli(reservation.getReservationDate().getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
            }

            response.setTimeSlotId(reservation.getTimeSlotId());
            response.setGuestCount(reservation.getGuestCount());
            response.setContactPhone(reservation.getContactPhone());
            response.setEmail(reservation.getEmail());
            response.setStatus(reservation.getStatus());
            response.setCancelDeadline(reservation.getCancelDeadline());
            response.setSpecialRequests(reservation.getSpecialRequests());
            response.setCreatedAt(reservation.getCreatedAt());
            response.setUpdatedAt(reservation.getUpdatedAt());

        } catch (Exception e) {
            log.error("Error converting reservation to response", e);
            throw new RuntimeException("轉換訂位資料時發生錯誤");
        }

        return response;
    }
}