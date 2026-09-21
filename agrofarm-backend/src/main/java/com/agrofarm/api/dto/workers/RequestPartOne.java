package com.agrofarm.api.dto.workers;

import lombok.Data;

@Data
public class RequestPartOne {

    String name;
    String phone;
    String email;
    String farm;
    String joinDate;
    String profile_picture;
    String role;
}
