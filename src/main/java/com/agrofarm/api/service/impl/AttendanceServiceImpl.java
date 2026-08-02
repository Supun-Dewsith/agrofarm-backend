package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.AttendanceRequest;
import com.agrofarm.api.dto.AttendanceResponse;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.entity.UserEntity;

import com.agrofarm.api.repository.AttendanceRepository;
import com.agrofarm.api.repository.UserRepository;

import com.agrofarm.api.service.AttendanceService;
import com.agrofarm.api.util.AttendanceStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {


    private final AttendanceRepository attendanceRepository;

    private final UserRepository userRepository;



    private AttendanceResponse mapToResponse(Attendance attendance) {

        AttendanceResponse response = new AttendanceResponse();

        response.setId(attendance.getId());
        response.setUserId(attendance.getUserId());
        response.setUserName(attendance.getUserName());
        response.setDate(attendance.getDate());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setStatus(attendance.getStatus());
        response.setLocation(attendance.getLocation());

        return response;
    }



    @Override
    public AttendanceResponse checkIn(AttendanceRequest request) {


        LocalDate today = LocalDate.now();


        if (attendanceRepository.existsByUserIdAndDate(
                request.getUserId(),
                today
        )) {

            throw new RuntimeException(
                    "Employee already checked in today."
            );
        }



        UserEntity user =
                userRepository.findById(request.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found."
                                )
                        );



        Attendance attendance = new Attendance();


        attendance.setUserId(user.getId());

        attendance.setUserName(user.getUsername());

        attendance.setDate(today);

        attendance.setCheckInTime(
                LocalDateTime.now()
        );

        attendance.setLocation(
                request.getLocation()
        );



        LocalTime lateTime =
                LocalTime.of(8,30);



        if(LocalTime.now().isAfter(lateTime)) {

            attendance.setStatus(
                    AttendanceStatus.LATE
            );

        } else {

            attendance.setStatus(
                    AttendanceStatus.PRESENT
            );
        }



        return mapToResponse(
                attendanceRepository.save(attendance)
        );
    }




    @Override
    public AttendanceResponse checkOut(Long userId) {


        LocalDate today =
                LocalDate.now();



        Attendance attendance =
                attendanceRepository
                        .findByUserIdAndDate(userId,today)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance not found."
                                )
                        );



        if(attendance.getCheckOutTime()!=null){

            throw new RuntimeException(
                    "Already checked out."
            );
        }



        attendance.setCheckOutTime(
                LocalDateTime.now()
        );



        return mapToResponse(
                attendanceRepository.save(attendance)
        );

    }





    @Override
    public QrAttendanceResponse processQrAttendance(
            QrAttendanceRequest request) {


        if(request.getQrToken()==null ||
                request.getQrToken().isBlank()){

            throw new RuntimeException(
                    "Invalid QR Token."
            );
        }



        UserEntity user =
                userRepository.findById(request.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found."
                                )
                        );



        AttendanceRequest attendanceRequest =
                new AttendanceRequest();


        attendanceRequest.setUserId(
                user.getId()
        );


        attendanceRequest.setUserName(
                user.getUsername()
        );


        attendanceRequest.setLocation(
                request.getLocation()
        );


        attendanceRequest.setStatus(
                AttendanceStatus.PRESENT
        );



        AttendanceResponse response =
                checkIn(attendanceRequest);



        return new QrAttendanceResponse(
                true,
                "Attendance marked successfully.",
                response.getCheckInTime()
        );
    }





    @Override
    public AttendanceDashboardResponse getDashboardSummary(
            LocalDate date) {



        long present =
                attendanceRepository.countByDateAndStatus(
                        date,
                        AttendanceStatus.PRESENT
                );


        long late =
                attendanceRepository.countByDateAndStatus(
                        date,
                        AttendanceStatus.LATE
                );


        long absent =
                attendanceRepository.countByDateAndStatus(
                        date,
                        AttendanceStatus.ABSENT
                );



        List<AttendanceResponse> list =
                getAttendanceByDate(date);



        long totalWorkers =
                list.size();



        return new AttendanceDashboardResponse(
                totalWorkers,
                present,
                late,
                absent,
                list
        );

    }





    @Override
    public List<AttendanceResponse> getAttendanceByDate(
            LocalDate date) {


        return attendanceRepository.findByDate(date)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }





    @Override
    public List<AttendanceResponse> getAttendanceHistory(
            Long userId) {


        return attendanceRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

}