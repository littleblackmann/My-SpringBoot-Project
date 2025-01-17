package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.TimeSlotDao;
import com.littleblack.springbootmall.model.TimeSlot;
import com.littleblack.springbootmall.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TimeSlotServiceImpl implements TimeSlotService {

    @Autowired
    private TimeSlotDao timeSlotDao;

    @Override
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotDao.getAllTimeSlots();
    }

    @Override
    public List<TimeSlot> getAvailableTimeSlots(LocalDate date) {
        return timeSlotDao.getAvailableTimeSlots(date);
    }

    @Override
    public Map<Integer, Integer> getAvailableSeats(LocalDate date) {
        List<TimeSlot> timeSlots = getAllTimeSlots();
        Map<Integer, Integer> availabilityMap = new HashMap<>();

        for (TimeSlot slot : timeSlots) {
            int availableSeats = timeSlotDao.getAvailableSeats(date, slot.getSlotId());
            availabilityMap.put(slot.getSlotId(), availableSeats);
        }

        return availabilityMap;
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, Integer timeSlotId, Integer guestCount) {
        int availableSeats = timeSlotDao.getAvailableSeats(date, timeSlotId);
        return availableSeats >= guestCount;
    }

    @Override
    public TimeSlot updateSlotCapacity(Integer slotId, Integer capacity) {
        return timeSlotDao.updateSlotCapacity(slotId, capacity);
    }
}