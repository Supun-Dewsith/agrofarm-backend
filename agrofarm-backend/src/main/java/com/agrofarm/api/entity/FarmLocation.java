package com.agrofarm.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class FarmLocation {
    private double latitude;
    private double longitude;


    public FarmLocation(double latitude, double longiitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
