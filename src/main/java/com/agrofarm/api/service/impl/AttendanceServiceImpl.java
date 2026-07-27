package com.agrofarm.api.service.impl;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.repository.AttendanceRepository;
import com.agrofarm.api.service.AttendanceService;
import com.agrofarm.api.util.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Override
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public Optional<Attendance> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    @Override
    public Attendance update(Attendance attendance) {
        return attendanceRepository.update(attendance);
    }

    @Override
    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
    }

    @Override
    public List<Attendance> findByStatus(AttendanceStatus status) {
        return attendanceRepository.findByStatus(status);
    }

    @Override
    public List<Attendance> findByUserId(Long userId) {
        return attendanceRepository.findByUserId(userId);
    }

    @Override
    public List<Attendance> findByDate(LocalDate attendanceDate) {
        return attendanceRepository.findByDate(attendanceDate);
    }
}