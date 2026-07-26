package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    List<Attendance> findByDate(LocalDate date);
    Optional<Attendance> findByUserAndDate(UserEntity user, LocalDate date);
    Attendance save(Attendance attendance);
}