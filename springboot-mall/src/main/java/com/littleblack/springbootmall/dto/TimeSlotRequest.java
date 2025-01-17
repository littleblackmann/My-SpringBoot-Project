package com.littleblack.springbootmall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TimeSlotRequest {

    @NotNull(message = "容量不能為空")
    @Min(value = 0, message = "容量不能小於0")
    @Max(value = 30, message = "容量不能超過30人")
    private Integer capacity;

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}