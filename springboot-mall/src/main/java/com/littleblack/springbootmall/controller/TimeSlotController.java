package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.TimeSlotRequest;
import com.littleblack.springbootmall.dto.TimeSlotAvailabilityResponse;
import com.littleblack.springbootmall.model.TimeSlot;
import com.littleblack.springbootmall.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/time-slots")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlotAvailabilityResponse>> getAvailableTimeSlots(
            @RequestParam LocalDate date) {
        List<TimeSlot> availableSlots = timeSlotService.getAvailableTimeSlots(date);
        List<TimeSlotAvailabilityResponse> response = availableSlots.stream()
                .map(this::convertToAvailabilityResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{slotId}/capacity")
    public ResponseEntity<TimeSlotAvailabilityResponse> updateSlotCapacity(
            @PathVariable Integer slotId,
            @Valid @RequestBody TimeSlotRequest request) {
        TimeSlot updatedSlot = timeSlotService.updateSlotCapacity(slotId, request.getCapacity());
        return ResponseEntity.ok(convertToAvailabilityResponse(updatedSlot));
    }

    private TimeSlotAvailabilityResponse convertToAvailabilityResponse(TimeSlot slot) {
        TimeSlotAvailabilityResponse response = new TimeSlotAvailabilityResponse();
        response.setSlotId(slot.getSlotId());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setCapacity(slot.getCapacity());
        return response;
    }
}