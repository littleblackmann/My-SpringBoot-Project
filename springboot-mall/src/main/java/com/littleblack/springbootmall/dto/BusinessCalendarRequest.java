package com.littleblack.springbootmall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;import java.time.LocalDate;

public class BusinessCalendarRequest {

    @NotNull(message = "日期不能為空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calendarDate;

    @NotNull(message = "營業狀態不能為空")
    private Boolean isBusinessDay;

    private String holidayName;

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
}