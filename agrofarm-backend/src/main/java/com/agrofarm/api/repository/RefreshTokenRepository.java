package com.agrofarm.api.repository;


import com.agrofarm.api.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String token, Long userId, Instant expiryDate);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
    void deleteByToken(String token);
}
