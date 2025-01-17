package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.BusinessCalendarDao;
import com.littleblack.springbootmall.model.BusinessCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BusinessCalendarDaoImpl implements BusinessCalendarDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<BusinessCalendar> getBusinessDays(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT date_id, calendar_date, is_business_day, holiday_name, " +
                "created_at, updated_at FROM business_calendar " +
                "WHERE calendar_date BETWEEN :startDate AND :endDate";

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", Date.valueOf(startDate));
        map.put("endDate", Date.valueOf(endDate));

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            BusinessCalendar businessCalendar = new BusinessCalendar();
            businessCalendar.setDateId(rs.getInt("date_id"));
            businessCalendar.setCalendarDate(rs.getDate("calendar_date").toLocalDate());
            businessCalendar.setIsBusinessDay(rs.getBoolean("is_business_day"));
            businessCalendar.setHolidayName(rs.getString("holiday_name"));
            businessCalendar.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            businessCalendar.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return businessCalendar;
        });
    }

    @Override
    public boolean isBusinessDay(LocalDate date) {
        String sql = "SELECT is_business_day FROM business_calendar WHERE calendar_date = :date";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("date", Date.valueOf(date));

        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, param, Boolean.class));
    }

    @Override
    public void updateBusinessDay(BusinessCalendar businessCalendar) {
        String sql = "UPDATE business_calendar SET is_business_day = :isBusinessDay, " +
                "holiday_name = :holidayName, updated_at = NOW() " +
                "WHERE calendar_date = :calendarDate";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("isBusinessDay", businessCalendar.getIsBusinessDay());
        param.addValue("holidayName", businessCalendar.getHolidayName());
        param.addValue("calendarDate", Date.valueOf(businessCalendar.getCalendarDate()));

        namedParameterJdbcTemplate.update(sql, param);
    }

    @Override
    public boolean isValidReservationDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM business_calendar " +
                "WHERE calendar_date = :date AND is_business_day = true";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("date", Date.valueOf(date));

        int count = namedParameterJdbcTemplate.queryForObject(sql, param, Integer.class);
        return count > 0;
    }

    @Override
    public BusinessCalendar getBusinessDayByDate(LocalDate date) {
        String sql = "SELECT date_id, calendar_date, is_business_day, holiday_name, " +
                "created_at, updated_at FROM business_calendar " +
                "WHERE calendar_date = :date";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("date", Date.valueOf(date));

        List<BusinessCalendar> results = namedParameterJdbcTemplate.query(sql, param, (rs, rowNum) -> {
            BusinessCalendar businessCalendar = new BusinessCalendar();
            businessCalendar.setDateId(rs.getInt("date_id"));
            businessCalendar.setCalendarDate(rs.getDate("calendar_date").toLocalDate());
            businessCalendar.setIsBusinessDay(rs.getBoolean("is_business_day"));
            businessCalendar.setHolidayName(rs.getString("holiday_name"));
            businessCalendar.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            businessCalendar.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return businessCalendar;
        });

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<BusinessCalendar> getHolidaysBetween(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT date_id, calendar_date, is_business_day, holiday_name, " +
                "created_at, updated_at FROM business_calendar " +
                "WHERE calendar_date BETWEEN :startDate AND :endDate " +
                "AND is_business_day = false";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("startDate", Date.valueOf(startDate));
        param.addValue("endDate", Date.valueOf(endDate));

        return namedParameterJdbcTemplate.query(sql, param, (rs, rowNum) -> {
            BusinessCalendar businessCalendar = new BusinessCalendar();
            businessCalendar.setDateId(rs.getInt("date_id"));
            businessCalendar.setCalendarDate(rs.getDate("calendar_date").toLocalDate());
            businessCalendar.setIsBusinessDay(rs.getBoolean("is_business_day"));
            businessCalendar.setHolidayName(rs.getString("holiday_name"));
            businessCalendar.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            businessCalendar.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return businessCalendar;
        });
    }

    @Override
    public void updateBusinessDayStatus(BusinessCalendar businessCalendar) {
        String sql = "UPDATE business_calendar SET is_business_day = :isBusinessDay, " +
                "holiday_name = :holidayName, updated_at = NOW() " +
                "WHERE calendar_date = :calendarDate";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("isBusinessDay", businessCalendar.getIsBusinessDay());
        param.addValue("holidayName", businessCalendar.getHolidayName());
        param.addValue("calendarDate", Date.valueOf(businessCalendar.getCalendarDate()));

        namedParameterJdbcTemplate.update(sql, param);
    }
}