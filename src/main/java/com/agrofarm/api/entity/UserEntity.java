package com.agrofarm.api.entity;

import com.agrofarm.api.util.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
    private Boolean enabled;
}