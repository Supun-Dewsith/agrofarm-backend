package com.agrofarm.api.entity;

import com.agrofarm.api.util.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Employee Name
     */
    @Column(nullable = false, length = 100)
    private String userName;

    /**
     * Attendance Date
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Employee Check-In Time
     */
    private LocalDateTime checkInTime;

    /**
     * Employee Check-Out Time
     */
    private LocalDateTime checkOutTime;

    /**
     * Attendance Status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    /**
     * GPS / Farm Location
     */
    @Column(length = 200)
    private String location;

}