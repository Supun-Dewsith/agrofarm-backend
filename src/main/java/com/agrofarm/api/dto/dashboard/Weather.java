package com.agrofarm.api.dto.dashboard;

public record Weather(
        Integer temp,
        String condition,
        String date,
        String humidity,
        String rainfall,
        String wind
) {}