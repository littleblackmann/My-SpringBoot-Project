package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.model.BusinessCalendar;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface BusinessCalendarService {
    boolean isValidReservationDate(LocalDate date);
    void updateBusinessDay(BusinessCalendar businessCalendar);
    BusinessCalendar getBusinessDayStatus(LocalDate date);
    List<BusinessCalendar> getHolidaysBetween(LocalDate startDate, LocalDate endDate);
    BusinessCalendar updateBusinessDayStatus(BusinessCalendar businessCalendar);
    List<BusinessCalendar> batchUpdateBusinessDays(List<BusinessCalendar> calendars);
}