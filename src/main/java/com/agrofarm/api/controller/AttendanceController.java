package com.agrofarm.api.controller;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.service.AttendanceService;
import com.agrofarm.api.util.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Create Attendance
    @PostMapping
    public ResponseEntity<Attendance> saveAttendance(@RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.save(attendance));
    }

    // Get All Attendance
    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.findAll());
    }

    // Get Attendance By ID
    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        return attendanceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update Attendance
    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance attendance) {

        attendance.setId(id);
        return ResponseEntity.ok(attendanceService.update(attendance));
    }

    // Delete Attendance
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Get Attendance By Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Attendance>> getByStatus(
            @PathVariable AttendanceStatus status) {

        return ResponseEntity.ok(attendanceService.findByStatus(status));
    }

    // Get Attendance By User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Attendance>> getByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(attendanceService.findByUserId(userId));
    }

    // Get Attendance By Date
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Attendance>> getByDate(
            @PathVariable LocalDate date) {

        return ResponseEntity.ok(attendanceService.findByDate(date));
    }
}