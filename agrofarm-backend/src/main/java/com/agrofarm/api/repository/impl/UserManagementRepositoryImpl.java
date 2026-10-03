package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.auth.AvailableFarmsResponse.FarmSummary;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserManagementRepository;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserManagementRepositoryImpl implements UserManagementRepository {

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AvailableFarmsResponse.FarmSummary> farmSummaryRowMapper = (rs, rowNum) -> new AvailableFarmsResponse.FarmSummary(
            rs.getLong("id"),
            rs.getLong("owner_id"),
            rs.getString("farm_name"),
            rs.getString("farm_address"),
            rs.getBigDecimal("latitude"),
            rs.getBigDecimal("longtitude")
    );

    @Override
    public List<UserData> getAll() {
        return getAll(null, null);
    }

    @Override
    public List<UserData> getAll(Role callerRole) {
        return getAll(callerRole, null);
    }

    @Override
    public List<UserData> getAll(Role callerRole, Long callerId) {
        String roleFilter;
        if (callerRole == Role.SUPERVISOR) {
            if (callerId == null) {
                return Collections.emptyList();
            }
            // Supervisor sees only farmers allocated to their farms
            roleFilter = """
                WHERE u.role = 'FARMER'
                  AND u.id IN (
                      SELECT fa_farmer.user_id 
                      FROM farm_assignments fa_farmer
                      WHERE fa_farmer.farm_id IN (
                          SELECT fa_sup.farm_id FROM farm_assignments fa_sup WHERE fa_sup.user_id = :callerId
                          UNION
                          SELECT f_sup.id FROM farms f_sup WHERE f_sup.owner_id = :callerId
                      )
                  )
            """;
        } else {
            // Admin sees all supervisors and farmers
            roleFilter = "WHERE u.role IN ('SUPERVISOR', 'FARMER') ";
        }

        String sql = """
            SELECT 
                u.id, 
                u.user_name, 
                u.created_at, 
                u.email, 
                u.phone_number, 
                u.role, 
                u.enabled, 
                u.image_url, 
                p.permission_key,
                fa.farm_id
            FROM users u
            LEFT JOIN user_permissions p ON u.id = p.user_id
            LEFT JOIN farm_assignments fa ON u.id = fa.user_id
            """ + roleFilter + """
            ORDER BY u.id
            """;

        var client = jdbcClient.sql(sql);
        if (callerRole == Role.SUPERVISOR && callerId != null) {
            client = client.param("callerId", callerId);
        }

        return client.query(rs -> {
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
                                rs.getString("image_url"),
                                new HashSet<>(),
                                new ArrayList<>()
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

                // Add farm assignment if present
                long farmId = rs.getLong("farm_id");
                if (!rs.wasNull() && !user.farmIds().contains(farmId)) {
                    user.farmIds().add(farmId);
                }
            }

            return new ArrayList<>(userMap.values());
        });
    }

    @Override
    public void updateUser(UserEntity user) {
        String sqlUser = """
            UPDATE users 
            SET user_name = ?, email = ?, password = ?, role = ?, image_url = ?
            WHERE id = ?
        """;
        jdbcTemplate.update(sqlUser,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getImageUrl(),
                user.getId()
        );

        // Update permissions
        jdbcTemplate.update("DELETE FROM user_permissions WHERE user_id = ?", user.getId());
        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            String sqlPerm = "INSERT INTO user_permissions (user_id, permission_key) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sqlPerm, user.getPermissions(), user.getPermissions().size(),
                    (ps, permissionKey) -> {
                        ps.setLong(1, user.getId());
                        ps.setString(2, permissionKey);
                    });
        }

        // Update farm assignments
        jdbcTemplate.update("DELETE FROM farm_assignments WHERE user_id = ?", user.getId());
        if (user.getFarmIds() != null && !user.getFarmIds().isEmpty()) {
            String sqlFarms = "INSERT INTO farm_assignments (user_id, farm_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sqlFarms, user.getFarmIds(), user.getFarmIds().size(),
                    (ps, farmId) -> {
                        ps.setLong(1, user.getId());
                        ps.setLong(2, farmId);
                    });
        }
    }

    private List<FarmSummary> populateStaff(List<FarmSummary> farms) {
        if (farms == null || farms.isEmpty()) {
            return farms != null ? farms : Collections.emptyList();
        }

        List<Long> farmIds = farms.stream().map(FarmSummary::id).toList();
        String inSql = String.join(",", Collections.nCopies(farmIds.size(), "?"));

        String sql = "SELECT fa.farm_id, u.user_name, u.role " +
                     "FROM farm_assignments fa " +
                     "JOIN users u ON fa.user_id = u.id " +
                     "WHERE fa.farm_id IN (" + inSql + ") " +
                     "ORDER BY u.user_name ASC";

        Map<Long, List<String>> supervisorMap = new HashMap<>();
        Map<Long, List<String>> farmerMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long farmId = rs.getLong("farm_id");
            String username = rs.getString("user_name");
            String role = rs.getString("role");

            if ("SUPERVISOR".equalsIgnoreCase(role)) {
                supervisorMap.computeIfAbsent(farmId, k -> new ArrayList<>()).add(username);
            } else if ("FARMER".equalsIgnoreCase(role)) {
                farmerMap.computeIfAbsent(farmId, k -> new ArrayList<>()).add(username);
            }
        }, farmIds.toArray());

        return farms.stream().map(f -> new FarmSummary(
                f.id(),
                f.ownerId(),
                f.farmName(),
                f.farmAddress(),
                f.latitude(),
                f.longitude(),
                supervisorMap.getOrDefault(f.id(), Collections.emptyList()),
                farmerMap.getOrDefault(f.id(), Collections.emptyList())
        )).toList();
    }

    public AvailableFarmsResponse getAvailableFarms() {
        String sql = """
            SELECT id, owner_id, farm_name, farm_address, latitude, longtitude
            FROM farms
            ORDER BY farm_name ASC
        """;

        List<FarmSummary> farms = jdbcClient.sql(sql)
                .query(farmSummaryRowMapper)
                .list();

        List<FarmSummary> populatedFarms = populateStaff(farms);
        return new AvailableFarmsResponse(populatedFarms, populatedFarms.size());
    }

    public AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId) {
        String sql = """
            SELECT DISTINCT f.id, f.owner_id, f.farm_name, f.farm_address, f.latitude, f.longtitude
            FROM farms f
            LEFT JOIN farm_assignments fa ON f.id = fa.farm_id
            WHERE f.owner_id = :ownerId OR fa.user_id = :ownerId
            ORDER BY f.farm_name ASC
        """;

        List<FarmSummary> farms = jdbcClient.sql(sql)
                .param("ownerId", ownerId)
                .query(farmSummaryRowMapper)
                .list();

        List<FarmSummary> populatedFarms = populateStaff(farms);
        return new AvailableFarmsResponse(populatedFarms, populatedFarms.size());
    }
}