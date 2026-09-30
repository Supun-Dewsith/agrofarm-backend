package com.agrofarm.api.repository.impl;


import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate template;

    private void populatePermissions(UserEntity user) {
        if (user == null) return;

        String sql = "SELECT permission_key FROM user_permissions WHERE user_id = ?";
        List<String> permissionList = template.query(sql, (rs, rowNum) -> rs.getString("permission_key"), user.getId());

        user.setPermissions(new HashSet<>(permissionList));
    }

    private void populateFarms(UserEntity user) {
        String sql = "SELECT farm_id FROM farm_assignments WHERE user_id = ?";
        List<Long> farmIds = template.query(sql, (rs, rowNum) -> rs.getLong("farm_id"), user.getId());
        user.setFarmIds(farmIds);
    }

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
                            rs.getBoolean("enabled"),
                            rs.getString("image_url")
                    ),username);
            populatePermissions(user);
            populateFarms(user);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEntity> findByUsernameOrEmail(String identifier) {
        try {
            String sql = "SELECT * FROM users WHERE user_name = ? OR email = ?";
            UserEntity user = template.queryForObject(sql,
                    (rs, rowNum) -> new UserEntity(
                            rs.getLong("id"),
                            rs.getString("user_name"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("phone_number"),
                            Role.valueOf(rs.getString("role")),
                            rs.getBoolean("enabled"),
                            rs.getString("image_url")
                    ),
                    identifier, identifier
            );
            populatePermissions(user);
            populateFarms(user);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public UserEntity save(UserEntity entity) {
        String sql = "INSERT INTO users (user_name, created_at, email, password, phone_number, role, enabled, image_url) VALUES (?,?,?,?,?,?,?,?)";

        template.update(sql,
                entity.getUsername(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getPhoneNumber(),
                entity.getRole().name(),
                entity.getEnabled(),
                entity.getImageUrl()
        );

        UserEntity savedUser = findByUsername(entity.getUsername())
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve created user."));

        if (savedUser != null && entity.getPermissions() != null && !entity.getPermissions().isEmpty()) {
            String sqlPerm = "INSERT INTO user_permissions (user_id, permission_key) VALUES (?, ?)";

            template.batchUpdate(sqlPerm, entity.getPermissions(), entity.getPermissions().size(),
                    (ps, permissionKey) -> {
                        ps.setLong(1, savedUser.getId());
                        ps.setString(2, permissionKey);
                    });
            populatePermissions(savedUser);
        }

        List<Long> farmIds = entity.getFarmIds();
        if (farmIds == null || farmIds.isEmpty()) {
            if (savedUser.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Farm allocation is required. Please assign at least one farm.");
            }
        } else {
            if ("ADMIN".equalsIgnoreCase(savedUser.getRole().name())) {
                throw new IllegalArgumentException("ADMIN role cannot be assigned to farms.");
            }

            if ("FARMER".equalsIgnoreCase(savedUser.getRole().name()) && farmIds.size() > 1) {
                throw new IllegalArgumentException("A FARMER can only be assigned to a single farm.");
            }

            String sqlFarms = "INSERT INTO farm_assignments (user_id, farm_id) VALUES (?, ?)";

            template.batchUpdate(sqlFarms, farmIds, farmIds.size(),
                    (ps, farmId) -> {
                        ps.setLong(1, savedUser.getId());
                        ps.setLong(2, farmId);
                    });
            populateFarms(savedUser);
        }

        return savedUser;
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
                entity.setImageUrl(rs.getString("image_url"));
                return entity;
            }, id);
            populatePermissions(user);
            populateFarms(user);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}