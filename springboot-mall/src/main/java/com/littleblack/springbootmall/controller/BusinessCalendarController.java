package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.BusinessCalendarRequest;
import com.littleblack.springbootmall.model.BusinessCalendar;
import com.littleblack.springbootmall.service.BusinessCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/business-calendar")
public class BusinessCalendarController {

    @Autowired
    private BusinessCalendarService businessCalendarService;

    @GetMapping("/status")
    public ResponseEntity<BusinessCalendar> getBusinessDayStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        BusinessCalendar status = businessCalendarService.getBusinessDayStatus(date);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<BusinessCalendar>> getHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BusinessCalendar> holidays = businessCalendarService.getHolidaysBetween(startDate, endDate);
        return ResponseEntity.ok(holidays);
    }

    @PutMapping("/status")
    public ResponseEntity<BusinessCalendar> updateBusinessDayStatus(
            @Valid @RequestBody BusinessCalendarRequest request) {
        BusinessCalendar calendar = new BusinessCalendar();
        calendar.setCalendarDate(request.getCalendarDate());
        calendar.setIsBusinessDay(request.getIsBusinessDay());
        calendar.setHolidayName(request.getHolidayName());

        BusinessCalendar updatedCalendar = businessCalendarService.updateBusinessDayStatus(calendar);
        return ResponseEntity.ok(updatedCalendar);
    }

    @PostMapping("/batch-update")
    public ResponseEntity<List<BusinessCalendar>> batchUpdateBusinessDays(
            @Valid @RequestBody List<BusinessCalendarRequest> requests) {
        List<BusinessCalendar> calendars = requests.stream()
                .map(request -> {
                    BusinessCalendar calendar = new BusinessCalendar();
                    calendar.setCalendarDate(request.getCalendarDate());
                    calendar.setIsBusinessDay(request.getIsBusinessDay());
                    calendar.setHolidayName(request.getHolidayName());
                    return calendar;
                })
                .toList();

        List<BusinessCalendar> updatedCalendars = businessCalendarService.batchUpdateBusinessDays(calendars);
        return ResponseEntity.ok(updatedCalendars);
    }
}