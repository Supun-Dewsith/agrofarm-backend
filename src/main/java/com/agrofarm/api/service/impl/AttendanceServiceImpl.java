package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.AttendanceDashboardResponse;
import com.agrofarm.api.dto.AttendanceResponse;
import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.AttendanceRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository; // පරණ එකමයි, කිසිම වෙනසක් කරන්නේ නැහැ

    private static final LocalTime LATE_TIME_THRESHOLD = LocalTime.of(8, 0);

    @Override
    public AttendanceDashboardResponse getDashboardData(LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findByDate(date);

        int present = 0;
        int late = 0;

        List<AttendanceResponse> responseList = new ArrayList<>();

        for (Attendance att : attendances) {
            if (att.getStatus() == Attendance.AttendanceStatus.PRESENT) present++;
            if (att.getStatus() == Attendance.AttendanceStatus.LATE) late++;

            responseList.add(AttendanceResponse.builder()
                    .employeeName(att.getUser().getUsername())
                    .role(att.getUser().getRole().name())
                    .checkIn(att.getCheckInTime())
                    .checkOut(att.getCheckOutTime())
                    .status(att.getStatus().name().toLowerCase())
                    .build());
        }

        // UserRepository එකේ findAll() නැති නිසා, මෙතැනදී Total සහ Absent ගණනය කිරීම
        // දත්ත සමුදායේ ඇති Attendance වාර්තා මත පදනම්ව හෝ ආරක්ෂිතව සකසා ඇත.
        int total = attendances.size(); // අවශ්‍ය නම් මෙයට වෙනත් logic එකක් දෙන්න පුළුවන්
        int absent = 0;

        return AttendanceDashboardResponse.builder()
                .total(total)
                .present(present)
                .late(late)
                .absent(absent)
                .todayAttendance(responseList)
                .build();
    }

    @Override
    public String markAttendance(Long userId) {
        // UserRepository එකේ මුලින් තිබුණු findById පමණක් පාවිච්චි කරයි (findAll පාවිච්චි කරන්නේ නැත)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(user, today);

        if (existingAttendance.isPresent()) {
            Attendance att = existingAttendance.get();
            if (att.getCheckOutTime() == null) {
                att.setCheckOutTime(now);
                attendanceRepository.save(att);
                return "Check-out successful!";
            } else {
                return "Attendance already marked for today!";
            }
        } else {
            Attendance newAttendance = new Attendance();
            newAttendance.setUser(user);
            newAttendance.setDate(today);
            newAttendance.setCheckInTime(now);

            if (now.isAfter(LATE_TIME_THRESHOLD)) {
                newAttendance.setStatus(Attendance.AttendanceStatus.LATE);
            } else {
                newAttendance.setStatus(Attendance.AttendanceStatus.PRESENT);
            }

            attendanceRepository.save(newAttendance);
            return "Check-in successful!";
        }
    }
}