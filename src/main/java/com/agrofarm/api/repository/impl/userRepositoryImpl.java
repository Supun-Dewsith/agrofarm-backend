package com.agrofarm.api.repository.impl;


import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate template;

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try{
            String sql = "SELECT * FROM users WHERE user_name=?";
            UserEntity user = template.queryForObject(sql,
                    (rs, rowNum) -> new UserEntity(
                            rs.getLong("id"),
                            rs.getString("user_name"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("phone_number"),
                            Role.valueOf(rs.getString("role")),
                            rs.getBoolean("enabled")
                    ),username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public UserEntity save(UserEntity entity) {
        String sql = "INSERT INTO users (user_name, created_at, email, password, phone_number, role, enabled) VALUES (?,?,?,?,?,?,?)";
        template.update(sql,
                entity.getUsername(),
                entity.getCreatedAt(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getPhoneNumber(),
                entity.getRole().name(),
                entity.getEnabled());
        Optional<UserEntity> user = findByUsername(entity.getUsername());
        return user.orElse(null);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            UserEntity user = template.queryForObject(sql, (rs, rowNum) -> {
                UserEntity entity = new UserEntity();
                entity.setId(rs.getLong("id"));
                entity.setUsername(rs.getString("user_name"));
                entity.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                entity.setEmail(rs.getString("email"));
                entity.setPassword(rs.getString("password"));
                entity.setPhoneNumber(rs.getString("phone_number"));
                entity.setRole(Role.valueOf(rs.getString("role")));
                entity.setEnabled(rs.getBoolean("enabled"));
                return entity;
            }, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
