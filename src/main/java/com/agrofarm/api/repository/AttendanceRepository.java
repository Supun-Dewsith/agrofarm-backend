package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.util.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Find attendance by user and date
     */
    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Find all attendance records for a given date
     */
    List<Attendance> findByDate(LocalDate date);

    /**
     * Count attendance by date and status
     */
    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

    /**
     * Find all attendance records of a user
     */
    List<Attendance> findByUserId(Long userId);

    /**
     * Check whether a user has already checked in today
     */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

}