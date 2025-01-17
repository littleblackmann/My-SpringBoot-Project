package com.littleblack.springbootmall.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BusinessCalendar {
    private Integer dateId;
    private LocalDate calendarDate;  // 將 Date 改為 LocalDate
    private Boolean isBusinessDay;
    private String holidayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getDateId() {
        return dateId;
    }

    public void setDateId(Integer dateId) {
        this.dateId = dateId;
    }

    public LocalDate getCalendarDate() {
        return calendarDate;
    }

    public void setCalendarDate(LocalDate calendarDate) {
        this.calendarDate = calendarDate;
    }

    public Boolean getIsBusinessDay() {
        return isBusinessDay;
    }

    public void setIsBusinessDay(Boolean isBusinessDay) {
        this.isBusinessDay = isBusinessDay;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}