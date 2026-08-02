package com.agrofarm.api.dto;

import com.agrofarm.api.util.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    private Long userId;

    private String userName;

    private String location;

    private AttendanceStatus status;

}