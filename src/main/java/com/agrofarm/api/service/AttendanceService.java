package com.agrofarm.api.service;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.util.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {

    Attendance save(Attendance attendance);

    Optional<Attendance> findById(Long id);

    List<Attendance> findAll();

    Attendance update(Attendance attendance);

    void deleteById(Long id);

    List<Attendance> findByStatus(AttendanceStatus status);

    List<Attendance> findByUserId(Long userId);

    List<Attendance> findByDate(LocalDate attendanceDate);
}