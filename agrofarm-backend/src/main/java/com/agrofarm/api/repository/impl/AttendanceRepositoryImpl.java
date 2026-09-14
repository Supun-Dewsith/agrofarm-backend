package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;
import com.agrofarm.api.dto.attendance.AttendanceFarmResponse;
import com.agrofarm.api.dto.attendance.AttendanceReportSummary;
import com.agrofarm.api.dto.attendance.AttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceWorkerResponse;
import com.agrofarm.api.dto.attendance.AttendanceQrSession;
import com.agrofarm.api.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttendanceRepositoryImpl implements AttendanceRepository {

    private final JdbcTemplate jdbcTemplate;

    private AttendanceResponse mapAttendance(ResultSet rs, int rowNum) throws SQLException {
        Timestamp checkIn = rs.getTimestamp("check_in_time");
        Timestamp checkOut = rs.getTimestamp("check_out_time");
        Date attendanceDate = rs.getDate("date");

        return new AttendanceResponse(
                rs.getObject("attendance_id", Long.class),
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("role"),
                rs.getObject("farm_id", Long.class),
                rs.getString("farm_name"),
                attendanceDate != null ? attendanceDate.toLocalDate() : null,
                checkIn != null ? checkIn.toLocalDateTime() : null,
                checkOut != null ? checkOut.toLocalDateTime() : null,
                rs.getString("status"),
                rs.getString("location"),
                rs.getObject("latitude", Double.class),
                rs.getObject("longitude", Double.class),
                rs.getObject("accuracy_meters", Double.class),
                rs.getObject("check_out_latitude", Double.class),
                rs.getObject("check_out_longitude", Double.class),
                rs.getObject("check_out_accuracy_meters", Double.class)
        );
    }

    @Override
    public List<AttendanceFarmResponse> findFarmsForUser(Long viewerUserId, String viewerRole) {
        if ("ADMIN".equals(viewerRole)) {
            return jdbcTemplate.query(
                    "SELECT id, farm_name, farm_address, boundary FROM farms ORDER BY farm_name",
                    (rs, rowNum) -> new AttendanceFarmResponse(
                            rs.getLong("id"),
                            rs.getString("farm_name"),
                            rs.getString("farm_address"),
                            rs.getString("boundary")
                    )
            );
        }

        String sql;
        if ("SUPERVISOR".equals(viewerRole)) {
            sql = """
                    SELECT DISTINCT f.id, f.farm_name, f.farm_address, f.boundary
                    FROM farms f
                    LEFT JOIN farm_assignments fa ON fa.farm_id = f.id
                    WHERE f.owner_id = ? OR fa.user_id = ?
                    ORDER BY f.farm_name
                    """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new AttendanceFarmResponse(
                    rs.getLong("id"),
                    rs.getString("farm_name"),
                    rs.getString("farm_address"),
                    rs.getString("boundary")
            ), viewerUserId, viewerUserId);
        }

        sql = """
                SELECT DISTINCT f.id, f.farm_name, f.farm_address, f.boundary
                FROM farms f
                JOIN farm_assignments fa ON fa.farm_id = f.id
                WHERE fa.user_id = ?
                ORDER BY f.farm_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AttendanceFarmResponse(
                rs.getLong("id"),
                rs.getString("farm_name"),
                rs.getString("farm_address"),
                rs.getString("boundary")
        ), viewerUserId);
    }

    @Override
    public boolean canAccessFarm(Long viewerUserId, String viewerRole, Long farmId) {
        if (farmId == null) {
            return true;
        }

        Integer count;
        if ("ADMIN".equals(viewerRole)) {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM farms WHERE id = ?",
                    Integer.class,
                    farmId
            );
        } else if ("SUPERVISOR".equals(viewerRole)) {
            count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(DISTINCT f.id)
                    FROM farms f
                    LEFT JOIN farm_assignments fa ON fa.farm_id = f.id AND fa.user_id = ?
                    WHERE f.id = ?
                      AND (f.owner_id = ? OR fa.user_id = ?)
                    """, Integer.class, viewerUserId, farmId, viewerUserId, viewerUserId);
        } else {
            count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM farm_assignments
                    WHERE user_id = ? AND farm_id = ?
                    """, Integer.class, viewerUserId, farmId);
        }

        return count != null && count > 0;
    }

    @Override
    public boolean canAccessUser(Long viewerUserId, String viewerRole, Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }

        if ("ADMIN".equals(viewerRole)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE id = ?",
                    Integer.class,
                    targetUserId
            );
            return count != null && count > 0;
        }

        if ("FARMER".equals(viewerRole)) {
            return viewerUserId.equals(targetUserId);
        }

        if ("SUPERVISOR".equals(viewerRole)) {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(DISTINCT target_user.id)
                    FROM users target_user
                    JOIN farm_assignments target_fa
                      ON target_fa.user_id = target_user.id
                    JOIN farms f
                      ON f.id = target_fa.farm_id
                    LEFT JOIN farm_assignments supervisor_fa
                      ON supervisor_fa.farm_id = f.id
                     AND supervisor_fa.user_id = ?
                    WHERE target_user.id = ?
                      AND target_user.role = 'FARMER'
                      AND target_user.enabled = 1
                      AND (f.owner_id = ? OR supervisor_fa.user_id = ?)
                    """, Integer.class, viewerUserId, targetUserId, viewerUserId, viewerUserId);
            return count != null && count > 0;
        }

        return false;
    }

    @Override
    public List<AttendanceWorkerResponse> findWorkers(
            Long viewerUserId,
            String viewerRole,
            Long farmId,
            String search
    ) {
        String normalized = search == null ? "" : search.trim();
        String like = "%" + normalized + "%";

        String sql = """
                SELECT DISTINCT
                       u.id,
                       u.user_name,
                       u.email,
                       u.phone_number,
                       u.role,
                       f.id AS farm_id,
                       f.farm_name
                FROM users u
                JOIN farm_assignments worker_fa ON worker_fa.user_id = u.id
                JOIN farms f ON f.id = worker_fa.farm_id
                WHERE u.enabled = 1
                  AND u.role = 'FARMER'
                  AND (? IS NULL OR f.id = ?)
                  AND (? = '' OR LOWER(u.user_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?))
                  AND (
                        ? = 'ADMIN'
                        OR (
                            ? = 'SUPERVISOR'
                            AND (
                                f.owner_id = ?
                                OR EXISTS (
                                    SELECT 1
                                    FROM farm_assignments supervisor_fa
                                    WHERE supervisor_fa.user_id = ?
                                      AND supervisor_fa.farm_id = f.id
                                )
                            )
                        )
                        OR (
                            ? = 'FARMER'
                            AND u.id = ?
                        )
                  )
                ORDER BY u.user_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AttendanceWorkerResponse(
                        rs.getLong("id"),
                        rs.getString("user_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("role"),
                        rs.getLong("farm_id"),
                        rs.getString("farm_name")
                ),
                farmId, farmId,
                normalized, like, like,
                viewerRole,
                viewerRole, viewerUserId, viewerUserId,
                viewerRole, viewerUserId
        );
    }

    @Override
    public List<AttendanceResponse> findDailyAttendance(
            Long viewerUserId,
            String viewerRole,
            LocalDate date,
            Long farmId,
            String search
    ) {
        String normalized = search == null ? "" : search.trim();
        String like = "%" + normalized + "%";

        String sql = """
                SELECT
                       a.id AS attendance_id,
                       u.id AS user_id,
                       u.user_name,
                       u.role,
                       f.id AS farm_id,
                       f.farm_name,
                       ? AS date,
                       a.check_in_time,
                       a.check_out_time,
                       COALESCE(a.status, 'ABSENT') AS status,
                       a.location,
                       a.check_in_latitude AS latitude,
                       a.check_in_longitude AS longitude,
                       a.check_in_accuracy_meters AS accuracy_meters,
                       a.check_out_latitude,
                       a.check_out_longitude,
                       a.check_out_accuracy_meters
                FROM users u
                JOIN farm_assignments worker_fa ON worker_fa.user_id = u.id
                JOIN farms f ON f.id = worker_fa.farm_id
                LEFT JOIN attendance a
                  ON a.user_id = u.id
                 AND a.farm_id = f.id
                 AND a.date = ?
                WHERE u.enabled = 1
                  AND u.role = 'FARMER'
                  AND (? IS NULL OR f.id = ?)
                  AND (? = '' OR LOWER(u.user_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?))
                  AND (
                        ? = 'ADMIN'
                        OR (
                            ? = 'SUPERVISOR'
                            AND (
                                f.owner_id = ?
                                OR EXISTS (
                                    SELECT 1
                                    FROM farm_assignments supervisor_fa
                                    WHERE supervisor_fa.user_id = ?
                                      AND supervisor_fa.farm_id = f.id
                                )
                            )
                        )
                        OR (
                            ? = 'FARMER'
                            AND u.id = ?
                        )
                  )
                ORDER BY u.user_name
                """;

        return jdbcTemplate.query(sql, this::mapAttendance,
                Date.valueOf(date), Date.valueOf(date),
                farmId, farmId,
                normalized, like, like,
                viewerRole,
                viewerRole, viewerUserId, viewerUserId,
                viewerRole, viewerUserId
        );
    }

    @Override
    public AttendanceDashboardResponse dashboard(
            Long viewerUserId,
            String viewerRole,
            LocalDate date,
            Long farmId
    ) {
        List<AttendanceResponse> list = findDailyAttendance(viewerUserId, viewerRole, date, farmId, "");
        long present = list.stream().filter(a -> "PRESENT".equals(a.status())).count();
        long late = list.stream().filter(a -> "LATE".equals(a.status())).count();
        long absent = list.stream().filter(a -> "ABSENT".equals(a.status())).count();

        return new AttendanceDashboardResponse(
                list.size(),
                present,
                late,
                absent,
                list.stream().limit(8).toList()
        );
    }

    @Override
    public Optional<AttendanceResponse> findAttendance(Long userId, Long farmId, LocalDate date) {
        String sql = """
                SELECT
                       a.id AS attendance_id,
                       u.id AS user_id,
                       u.user_name,
                       u.role,
                       a.farm_id,
                       f.farm_name,
                       a.date,
                       a.check_in_time,
                       a.check_out_time,
                       a.status,
                       a.location,
                       a.check_in_latitude AS latitude,
                       a.check_in_longitude AS longitude,
                       a.check_in_accuracy_meters AS accuracy_meters,
                       a.check_out_latitude,
                       a.check_out_longitude,
                       a.check_out_accuracy_meters
                FROM attendance a
                JOIN users u ON u.id = a.user_id
                LEFT JOIN farms f ON f.id = a.farm_id
                WHERE a.user_id = ?
                  AND a.farm_id = ?
                  AND a.date = ?
                LIMIT 1
                """;

        List<AttendanceResponse> rows = jdbcTemplate.query(
                sql,
                this::mapAttendance,
                userId,
                farmId,
                Date.valueOf(date)
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<AttendanceResponse> findAttendanceById(Long attendanceId) {
        String sql = """
                SELECT
                       a.id AS attendance_id,
                       u.id AS user_id,
                       u.user_name,
                       u.role,
                       a.farm_id,
                       f.farm_name,
                       a.date,
                       a.check_in_time,
                       a.check_out_time,
                       a.status,
                       a.location,
                       a.check_in_latitude AS latitude,
                       a.check_in_longitude AS longitude,
                       a.check_in_accuracy_meters AS accuracy_meters,
                       a.check_out_latitude,
                       a.check_out_longitude,
                       a.check_out_accuracy_meters
                FROM attendance a
                JOIN users u ON u.id = a.user_id
                LEFT JOIN farms f ON f.id = a.farm_id
                WHERE a.id = ?
                LIMIT 1
                """;

        List<AttendanceResponse> rows = jdbcTemplate.query(sql, this::mapAttendance, attendanceId);
        return rows.stream().findFirst();
    }

    @Override
    public AttendanceResponse insertCheckIn(
            Long userId,
            Long farmId,
            LocalDateTime checkInTime,
            String status,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters
    ) {
        String userName = jdbcTemplate.queryForObject(
                "SELECT user_name FROM users WHERE id = ?",
                String.class,
                userId
        );

        jdbcTemplate.update("""
                INSERT INTO attendance(
                    user_id,
                    farm_id,
                    user_name,
                    date,
                    check_in_time,
                    check_in_latitude,
                    check_in_longitude,
                    check_in_accuracy_meters,
                    status,
                    location
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userId,
                farmId,
                userName,
                Date.valueOf(checkInTime.toLocalDate()),
                Timestamp.valueOf(checkInTime),
                latitude,
                longitude,
                accuracyMeters,
                status,
                location
        );

        return findAttendance(userId, farmId, checkInTime.toLocalDate()).orElseThrow();
    }

    @Override
    public AttendanceResponse updateCheckOut(
            Long attendanceId,
            LocalDateTime checkOutTime,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters
    ) {
        jdbcTemplate.update("""
                UPDATE attendance
                SET check_out_time = ?,
                    check_out_latitude = ?,
                    check_out_longitude = ?,
                    check_out_accuracy_meters = ?,
                    location = COALESCE(location, ?)
                WHERE id = ?
                """,
                Timestamp.valueOf(checkOutTime),
                latitude,
                longitude,
                accuracyMeters,
                location,
                attendanceId
        );

        return findAttendanceById(attendanceId).orElseThrow();
    }

    @Override
    public void createQrToken(
            String token,
            String manualCode,
            Long farmId,
            Long createdBy,
            LocalDateTime expiresAt
    ) {
        jdbcTemplate.update(
                "UPDATE attendance_qr_tokens SET active = 0 WHERE farm_id = ? AND active = 1",
                farmId
        );

        jdbcTemplate.update("""
                INSERT INTO attendance_qr_tokens(
                    token, manual_code, farm_id, created_by, expires_at, active
                )
                VALUES (?, ?, ?, ?, ?, 1)
                """,
                token,
                manualCode,
                farmId,
                createdBy,
                Timestamp.valueOf(expiresAt)
        );
    }

    @Override
    public boolean activeManualCodeExists(String manualCode, LocalDateTime now) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM attendance_qr_tokens
                WHERE manual_code = ?
                  AND active = 1
                  AND expires_at >= ?
                """, Integer.class, manualCode, Timestamp.valueOf(now));

        return count != null && count > 0;
    }

    private AttendanceQrSession mapQrSession(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp expiresAt = rs.getTimestamp("expires_at");

        return new AttendanceQrSession(
                rs.getLong("id"),
                rs.getString("token"),
                rs.getString("manual_code"),
                rs.getLong("farm_id"),
                rs.getLong("created_by"),
                rs.getString("created_by_name"),
                rs.getString("created_by_role"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                expiresAt != null ? expiresAt.toLocalDateTime() : null,
                rs.getBoolean("active")
        );
    }

    @Override
    public Optional<AttendanceQrSession> findValidQrSessionByToken(String token, LocalDateTime now) {
        String sql = """
                SELECT q.id, q.token, q.manual_code, q.farm_id, q.created_by,
                       u.user_name AS created_by_name, u.role AS created_by_role,
                       q.created_at, q.expires_at, q.active
                FROM attendance_qr_tokens q
                JOIN users u ON u.id = q.created_by
                WHERE q.token = ?
                  AND q.active = 1
                  AND q.expires_at >= ?
                LIMIT 1
                """;

        List<AttendanceQrSession> rows = jdbcTemplate.query(
                sql,
                this::mapQrSession,
                token,
                Timestamp.valueOf(now)
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<AttendanceQrSession> findValidQrSessionByManualCode(
            String manualCode,
            LocalDateTime now
    ) {
        String sql = """
                SELECT q.id, q.token, q.manual_code, q.farm_id, q.created_by,
                       u.user_name AS created_by_name, u.role AS created_by_role,
                       q.created_at, q.expires_at, q.active
                FROM attendance_qr_tokens q
                JOIN users u ON u.id = q.created_by
                WHERE q.manual_code = ?
                  AND q.active = 1
                  AND q.expires_at >= ?
                ORDER BY q.created_at DESC
                LIMIT 1
                """;

        List<AttendanceQrSession> rows = jdbcTemplate.query(
                sql,
                this::mapQrSession,
                manualCode,
                Timestamp.valueOf(now)
        );
        return rows.stream().findFirst();
    }

    @Override
    public boolean isWorkerAssignedToFarm(Long userId, Long farmId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM farm_assignments fa
                JOIN users u ON u.id = fa.user_id
                WHERE fa.user_id = ?
                  AND fa.farm_id = ?
                  AND u.enabled = 1
                  AND u.role = 'FARMER'
                """, Integer.class, userId, farmId);

        return count != null && count > 0;
    }


    @Override
    public Optional<String> findFarmBoundary(Long farmId) {
        if (farmId == null) {
            return Optional.empty();
        }

        List<String> rows = jdbcTemplate.query(
                "SELECT boundary FROM farms WHERE id = ? LIMIT 1",
                (rs, rowNum) -> rs.getString("boundary"),
                farmId
        );

        return rows.stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    @Override
    public List<AttendanceResponse> findReport(
            Long viewerUserId,
            String viewerRole,
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    ) {
        String normalized = search == null ? "" : search.trim();
        String like = "%" + normalized + "%";

        String sql = """
                SELECT
                       a.id AS attendance_id,
                       u.id AS user_id,
                       u.user_name,
                       u.role,
                       a.farm_id,
                       f.farm_name,
                       a.date,
                       a.check_in_time,
                       a.check_out_time,
                       a.status,
                       a.location,
                       a.check_in_latitude AS latitude,
                       a.check_in_longitude AS longitude,
                       a.check_in_accuracy_meters AS accuracy_meters,
                       a.check_out_latitude,
                       a.check_out_longitude,
                       a.check_out_accuracy_meters
                FROM attendance a
                JOIN users u ON u.id = a.user_id
                LEFT JOIN farms f ON f.id = a.farm_id
                WHERE a.date BETWEEN ? AND ?
                  AND (? IS NULL OR a.farm_id = ?)
                  AND (? IS NULL OR a.user_id = ?)
                  AND (? = '' OR LOWER(u.user_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?))
                  AND (
                        ? = 'ADMIN'
                        OR (
                            ? = 'SUPERVISOR'
                            AND (
                                f.owner_id = ?
                                OR EXISTS (
                                    SELECT 1
                                    FROM farm_assignments supervisor_fa
                                    WHERE supervisor_fa.user_id = ?
                                      AND supervisor_fa.farm_id = a.farm_id
                                )
                            )
                        )
                        OR (
                            ? = 'FARMER'
                            AND a.user_id = ?
                        )
                  )
                ORDER BY a.date DESC, u.user_name ASC
                """;

        return jdbcTemplate.query(sql, this::mapAttendance,
                Date.valueOf(from), Date.valueOf(to),
                farmId, farmId,
                userId, userId,
                normalized, like, like,
                viewerRole,
                viewerRole, viewerUserId, viewerUserId,
                viewerRole, viewerUserId
        );
    }

    @Override
    public List<AttendanceReportSummary> findReportSummary(
            Long viewerUserId,
            String viewerRole,
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    ) {
        List<AttendanceWorkerResponse> workers = findWorkers(
                viewerUserId,
                viewerRole,
                farmId,
                search
        );

        if (userId != null) {
            workers = workers.stream()
                    .filter(worker -> worker.id().equals(userId))
                    .toList();
        }

        long scheduledDays = ChronoUnit.DAYS.between(from, to) + 1;

        List<AttendanceResponse> rows = findReport(
                viewerUserId,
                viewerRole,
                from,
                to,
                farmId,
                userId,
                search
        );

        return workers.stream().map(worker -> {
            List<AttendanceResponse> workerRows = rows.stream()
                    .filter(row -> row.userId().equals(worker.id()))
                    .filter(row -> worker.farmId().equals(row.farmId()))
                    .toList();

            long present = workerRows.stream().filter(row -> "PRESENT".equals(row.status())).count();
            long late = workerRows.stream().filter(row -> "LATE".equals(row.status())).count();
            long halfDay = workerRows.stream().filter(row -> "HALF_DAY".equals(row.status())).count();
            long leave = workerRows.stream().filter(row -> "ON_LEAVE".equals(row.status())).count();
            long recordedDays = workerRows.stream().map(AttendanceResponse::date).distinct().count();
            long absent = Math.max(0, scheduledDays - recordedDays);

            double workedHours = workerRows.stream()
                    .filter(row -> row.checkInTime() != null && row.checkOutTime() != null)
                    .mapToLong(row -> Duration.between(row.checkInTime(), row.checkOutTime()).toMinutes())
                    .sum() / 60.0;

            return new AttendanceReportSummary(
                    worker.id(),
                    worker.userName(),
                    worker.farmId(),
                    worker.farmName(),
                    scheduledDays,
                    present,
                    late,
                    absent,
                    halfDay,
                    leave,
                    Math.round(workedHours * 100.0) / 100.0
            );
        }).toList();
    }
}
