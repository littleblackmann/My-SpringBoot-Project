package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.BusinessCalendarDao;
import com.littleblack.springbootmall.model.BusinessCalendar;
import com.littleblack.springbootmall.service.BusinessCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BusinessCalendarServiceImpl implements BusinessCalendarService {

    @Autowired
    private BusinessCalendarDao businessCalendarDao;

    @Override
    public boolean isValidReservationDate(LocalDate date) {
        return businessCalendarDao.isValidReservationDate(date);
    }

    @Override
    public void updateBusinessDay(BusinessCalendar businessCalendar) {
        businessCalendarDao.updateBusinessDay(businessCalendar);
    }

    @Override
    public BusinessCalendar getBusinessDayStatus(LocalDate date) {
        return businessCalendarDao.getBusinessDayByDate(date);
    }

    @Override
    public List<BusinessCalendar> getHolidaysBetween(LocalDate startDate, LocalDate endDate) {
        return businessCalendarDao.getHolidaysBetween(startDate, endDate);
    }

    @Override
    @Transactional
    public BusinessCalendar updateBusinessDayStatus(BusinessCalendar businessCalendar) {
        businessCalendarDao.updateBusinessDayStatus(businessCalendar);
        return businessCalendarDao.getBusinessDayByDate(businessCalendar.getCalendarDate());
    }

    @Override
    @Transactional
    public List<BusinessCalendar> batchUpdateBusinessDays(List<BusinessCalendar> calendars) {
        calendars.forEach(businessCalendarDao::updateBusinessDayStatus);
        return calendars.stream()
                .map(calendar -> businessCalendarDao.getBusinessDayByDate(calendar.getCalendarDate()))
                .toList();
    }
}