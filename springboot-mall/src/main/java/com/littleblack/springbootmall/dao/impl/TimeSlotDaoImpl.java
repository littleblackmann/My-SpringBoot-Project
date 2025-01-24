package com.littleblack.springbootmall.dao.impl;

import com.littleblack.springbootmall.dao.TimeSlotDao;
import com.littleblack.springbootmall.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.util.Optional;
import java.sql.Time;


@Component
public class TimeSlotDaoImpl implements TimeSlotDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<TimeSlot> getAllTimeSlots() {
        String sql = "SELECT slot_id, start_time, end_time, capacity FROM time_slots";

        Map<String, Object> map = new HashMap<>();

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSlotId(rs.getInt("slot_id"));
            timeSlot.setStartTime(rs.getTime("start_time").toLocalTime());
            timeSlot.setEndTime(rs.getTime("end_time").toLocalTime());
            timeSlot.setCapacity(rs.getInt("capacity"));
            return timeSlot;
        });
    }

    @Override
    public TimeSlot getTimeSlotById(Integer slotId) {
        String sql = "SELECT slot_id, start_time, end_time, capacity FROM time_slots WHERE slot_id = :slotId";

        Map<String, Object> map = new HashMap<>();
        map.put("slotId", slotId);

        List<TimeSlot> timeSlotList = namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSlotId(rs.getInt("slot_id"));
            timeSlot.setStartTime(rs.getTime("start_time").toLocalTime());
            timeSlot.setEndTime(rs.getTime("end_time").toLocalTime());
            timeSlot.setCapacity(rs.getInt("capacity"));
            return timeSlot;
        });

        if (timeSlotList.size() > 0) {
            return timeSlotList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<TimeSlot> getAvailableTimeSlots(LocalDate date) {
        String sql = "SELECT ts.slot_id, ts.start_time, ts.end_time, ts.capacity, " +
                "ts.capacity - COALESCE(SUM(r.guest_count), 0) as available_seats " +
                "FROM time_slots ts " +
                "LEFT JOIN reservations r ON ts.slot_id = r.time_slot_id " +
                "AND r.reservation_date = :date " +
                "AND r.status != 2 " +
                "GROUP BY ts.slot_id, ts.capacity " +
                "HAVING available_seats > 0";

        Map<String, Object> map = new HashMap<>();
        map.put("date", Date.valueOf(date));

        return namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSlotId(rs.getInt("slot_id"));
            timeSlot.setStartTime(rs.getTime("start_time").toLocalTime());
            timeSlot.setEndTime(rs.getTime("end_time").toLocalTime());
            timeSlot.setCapacity(rs.getInt("capacity"));
            return timeSlot;
        });
    }

    @Override
    public int getAvailableSeats(LocalDate date, Integer timeSlotId) {
        String sql = "SELECT ts.capacity - COALESCE(SUM(r.guest_count), 0) as available_seats " +
                "FROM time_slots ts " +
                "LEFT JOIN reservations r ON ts.slot_id = r.time_slot_id " +
                "AND r.reservation_date = :date " +
                "AND r.status != 2 " +
                "WHERE ts.slot_id = :timeSlotId " +
                "GROUP BY ts.slot_id, ts.capacity";

        Map<String, Object> map = new HashMap<>();
        map.put("date", Date.valueOf(date));
        map.put("timeSlotId", timeSlotId);

        return namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);
    }

    @Override
    public TimeSlot updateSlotCapacity(Integer slotId, Integer capacity) {
        String sql = "UPDATE time_slots SET capacity = :capacity WHERE slot_id = :slotId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("capacity", capacity);
        params.addValue("slotId", slotId);

        namedParameterJdbcTemplate.update(sql, params);

        return getTimeSlotById(slotId);
    }

    @Override
    public Optional<TimeSlot> getTimeSlotByStartTime(LocalTime startTime) {
        String sql = "SELECT slot_id, start_time, end_time, capacity FROM time_slots WHERE start_time = :startTime";

        Map<String, Object> map = new HashMap<>();
        map.put("startTime", Time.valueOf(startTime));

        List<TimeSlot> timeSlotList = namedParameterJdbcTemplate.query(sql, map, (rs, rowNum) -> {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSlotId(rs.getInt("slot_id"));
            timeSlot.setStartTime(rs.getTime("start_time").toLocalTime());
            timeSlot.setEndTime(rs.getTime("end_time").toLocalTime());
            timeSlot.setCapacity(rs.getInt("capacity"));
            return timeSlot;
        });

        return timeSlotList.isEmpty() ? Optional.empty() : Optional.of(timeSlotList.get(0));
    }


}