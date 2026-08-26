package com.agrofarm.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private Instant expiryDate;

    public RefreshToken(Long userId, String token, Instant expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
