package com.agrofarm.api.repository.impl;
import com.agrofarm.api.dto.user.UserData; // or UserDto, adjust import as needed
import com.agrofarm.api.repository.UserManagementRepository;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserManagementRepositoryImpl implements UserManagementRepository {

    private final JdbcClient jdbcClient;

    @Override
    public List<UserData> getAll() {
        String sql = """
            SELECT 
                u.id, 
                u.user_name, 
                u.created_at, 
                u.email, 
                u.phone_number, 
                u.role, 
                u.enabled, 
                p.permission_key
            FROM users u
            LEFT JOIN user_permissions p ON u.id = p.user_id
            ORDER BY u.id
            """;

        return jdbcClient.sql(sql)
                .query(rs -> {
                    Map<Long, UserData> userMap = new LinkedHashMap<>();

                    while (rs.next()) {
                        Long userId = rs.getLong("id");

                        // Retrieve or instantiate the UserData object for this ID
                        UserData user = userMap.computeIfAbsent(userId, id -> {
                            try {
                                String roleStr = rs.getString("role");
                                Role role = roleStr != null ? Role.valueOf(roleStr) : null;
                                Timestamp timestamp = rs.getTimestamp("created_at");
                                LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

                                return new UserData(
                                        userId,
                                        rs.getString("user_name"),
                                        createdAt,
                                        rs.getString("email"),
                                        rs.getString("phone_number"),
                                        role,
                                        rs.getBoolean("enabled"),
                                        new HashSet<>()
                                );
                            } catch (Exception e) {
                                throw new RuntimeException("Error mapping UserData", e);
                            }
                        });

                        // Add permission if present in the joined row
                        String permission = rs.getString("permission_key");
                        if (permission != null) {
                            user.permissions().add(permission);
                        }
                    }

                    return new ArrayList<>(userMap.values());
                });
    }
}