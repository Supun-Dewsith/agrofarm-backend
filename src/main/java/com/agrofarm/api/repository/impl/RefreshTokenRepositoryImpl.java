package com.agrofarm.api.repository.impl;


import com.agrofarm.api.entity.RefreshToken;
import com.agrofarm.api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public void save(String token, Long userId, Instant expiryDate) {
        String sql = "INSERT INTO refresh_tokens (token, user_id, expiry_date) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, token, userId, Timestamp.from(expiryDate));
    }

    public Optional<RefreshToken> findByToken(String token) {
        String sql = "SELECT user_id, token, expiry_date FROM refresh_tokens WHERE token = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RefreshToken(
                rs.getLong("user_id"),
                rs.getString("token"),
                rs.getObject("expiry_date", LocalDateTime.class)
        ), token).stream().findFirst();
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM refresh_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}
