package com.agrofarm.api.entity;

import com.agrofarm.api.util.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private String imageUrl;
    private Set<String> permissions = new HashSet<>();

    public UserEntity(Long id, String username, LocalDateTime createdAt, String email,
                      String password, String phoneNumber, Role role, Boolean enabled) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.enabled = enabled;
        this.permissions = new HashSet<>();
    }
}