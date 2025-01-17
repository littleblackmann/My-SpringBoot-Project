package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.TimeSlot;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TimeSlotService {
    List<TimeSlot> getAllTimeSlots();

    List<TimeSlot> getAvailableTimeSlots(LocalDate date);

    Map<Integer, Integer> getAvailableSeats(LocalDate date);

    boolean isSlotAvailable(LocalDate date, Integer timeSlotId, Integer guestCount);

    TimeSlot updateSlotCapacity(Integer slotId, Integer capacity);
}