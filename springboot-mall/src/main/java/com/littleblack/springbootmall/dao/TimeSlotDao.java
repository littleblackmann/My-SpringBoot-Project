package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.model.TimeSlot;
import java.time.LocalDate;
import java.util.List;

public interface TimeSlotDao {
    List<TimeSlot> getAllTimeSlots();

    TimeSlot getTimeSlotById(Integer slotId);

    List<TimeSlot> getAvailableTimeSlots(LocalDate date);

    int getAvailableSeats(LocalDate date, Integer timeSlotId);

    TimeSlot updateSlotCapacity(Integer slotId, Integer capacity);
}