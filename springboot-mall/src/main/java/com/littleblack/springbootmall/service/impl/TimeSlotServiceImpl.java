package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.ReservationDao;
import com.littleblack.springbootmall.dao.TimeSlotDao;
import com.littleblack.springbootmall.model.TimeSlot;
import com.littleblack.springbootmall.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TimeSlotServiceImpl implements TimeSlotService {

    @Autowired
    private TimeSlotDao timeSlotDao;

    @Autowired
    private ReservationDao reservationDao;

    @Override
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotDao.getAllTimeSlots();
    }

    @Override
    public List<TimeSlot> getAvailableTimeSlots(LocalDate date) {
        List<TimeSlot> allSlots = timeSlotDao.getAllTimeSlots();
        Map<Integer, Integer> reservedSeats = reservationDao.getReservedSeatsByDate(date);

        return allSlots.stream()
                .filter(slot -> {
                    Integer reserved = reservedSeats.getOrDefault(slot.getSlotId(), 0);
                    return reserved < slot.getCapacity();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Integer> getAvailableSeats(LocalDate date) {
        List<TimeSlot> allSlots = timeSlotDao.getAllTimeSlots();
        Map<Integer, Integer> reservedSeats = reservationDao.getReservedSeatsByDate(date);
        Map<Integer, Integer> availableSeats = new HashMap<>();

        allSlots.forEach(slot -> {
            Integer reserved = reservedSeats.getOrDefault(slot.getSlotId(), 0);
            availableSeats.put(slot.getSlotId(), slot.getCapacity() - reserved);
        });

        return availableSeats;
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, Integer timeSlotId, Integer guestCount) {
        TimeSlot slot = timeSlotDao.getTimeSlotById(timeSlotId);
        if (slot == null) {
            return false;
        }

        // 檢查是否在營業時間內
        LocalTime now = LocalTime.now();
        if (date.equals(LocalDate.now()) && now.isAfter(slot.getStartTime())) {
            return false;
        }

        Map<Integer, Integer> availableSeats = getAvailableSeats(date);
        Integer available = availableSeats.getOrDefault(timeSlotId, 0);

        return available >= guestCount;
    }

    @Override
    public TimeSlot updateSlotCapacity(Integer slotId, Integer capacity) {
        return timeSlotDao.updateSlotCapacity(slotId, capacity);
    }

    @Override
    public Integer getTimeSlotIdByTime(String timeRange) {
        String[] times = timeRange.split("-");
        if (times.length != 2) {
            return null;
        }

        LocalTime startTime = LocalTime.parse(times[0].trim() + ":00");
        return timeSlotDao.getTimeSlotByStartTime(startTime)
                .map(TimeSlot::getSlotId)
                .orElse(null);
    }

    @Override
    public List<String> getAvailableTimeRanges(LocalDate date) {
        return getAvailableTimeSlots(date).stream()
                .map(slot -> String.format("%s-%s",
                        slot.getStartTime().toString().substring(0, 5),
                        slot.getEndTime().toString().substring(0, 5)))
                .collect(Collectors.toList());
    }
}
