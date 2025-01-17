package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.model.BusinessCalendar;
import java.time.LocalDate;
import java.util.List;

public interface BusinessCalendarDao {
    List<BusinessCalendar> getBusinessDays(LocalDate startDate, LocalDate endDate);
    boolean isBusinessDay(LocalDate date);
    void updateBusinessDay(BusinessCalendar businessCalendar);

    // 新增以下方法
    boolean isValidReservationDate(LocalDate date);
    BusinessCalendar getBusinessDayByDate(LocalDate date);
    List<BusinessCalendar> getHolidaysBetween(LocalDate startDate, LocalDate endDate);
    void updateBusinessDayStatus(BusinessCalendar businessCalendar);
}