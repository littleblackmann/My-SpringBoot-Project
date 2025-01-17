package com.littleblack.springbootmall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ReservationRequest {

    @NotNull(message = "用戶ID不能為空")
    private Integer userId;

    @NotBlank(message = "訂位者姓名不能為空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z\\s]+$", message = "姓名只能包含中文、英文字母和空格")
    private String customerName;

    @NotNull(message = "預約日期不能為空")
    @Future(message = "預約日期必須是未來日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @NotNull(message = "時段ID不能為空")
    private Integer timeSlotId;

    @NotNull(message = "用餐人數不能為空")
    @Min(value = 1, message = "用餐人數最少為1人")
    @Max(value = 30, message = "用餐人數最多為30人")
    private Integer guestCount;

    @NotBlank(message = "聯絡電話不能為空")
    @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼")
    private String contactPhone;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件地址")
    private String email;

    @Size(max = 500, message = "特殊要求不能超過500字")
    private String specialRequests;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // 原有的 getters 和 setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Integer getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Integer timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
}