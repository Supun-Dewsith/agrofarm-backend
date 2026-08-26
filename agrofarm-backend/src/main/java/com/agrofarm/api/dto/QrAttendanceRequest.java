package com.agrofarm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QrAttendanceRequest {

    private String qrToken;

    private Long userId;

    private String location;

    private Double latitude;

    private Double longitude;
}