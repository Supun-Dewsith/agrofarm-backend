package com.agrofarm.api.repository.impl;

import com.agrofarm.api.entity.Attendance;
import com.agrofarm.api.repository.AttendanceRepository;
import com.agrofarm.api.util.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttendanceRepositoryImpl implements AttendanceRepository {

    private final JdbcTemplate template;

    @Override
    public Attendance save(Attendance attendance) {

        String sql = """
                INSERT INTO attendance
                (user_id, attendance_date, check_in_time, check_out_time, status, remarks)
                VALUES (?,?,?,?,?,?)
                """;

        template.update(sql,
                attendance.getUserId(),
                attendance.getAttendanceDate(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getStatus().name(),
                attendance.getRemarks());

        return attendance;
    }

    @Override
    public Optional<Attendance> findById(Long id) {

        try {
            String sql = "SELECT * FROM attendance WHERE id=?";

            Attendance attendance = template.queryForObject(sql, (rs, rowNum) -> {

                Attendance a = new Attendance();

                a.setId(rs.getLong("id"));
                a.setUserId(rs.getLong("user_id"));
                a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());

                Time in = rs.getTime("check_in_time");
                if (in != null) {
                    a.setCheckInTime(in.toLocalTime());
                }

                Time out = rs.getTime("check_out_time");
                if (out != null) {
                    a.setCheckOutTime(out.toLocalTime());
                }

                a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
                a.setRemarks(rs.getString("remarks"));

                return a;

            }, id);

            return Optional.ofNullable(attendance);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Attendance> findAll() {

        String sql = "SELECT * FROM attendance";

        return template.query(sql, (rs, rowNum) -> {

            Attendance a = new Attendance();

            a.setId(rs.getLong("id"));
            a.setUserId(rs.getLong("user_id"));
            a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());

            Time in = rs.getTime("check_in_time");
            if (in != null) {
                a.setCheckInTime(in.toLocalTime());
            }

            Time out = rs.getTime("check_out_time");
            if (out != null) {
                a.setCheckOutTime(out.toLocalTime());
            }

            a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
            a.setRemarks(rs.getString("remarks"));

            return a;
        });
    }

    @Override
    public Attendance update(Attendance attendance) {

        String sql = """
                UPDATE attendance
                SET user_id=?,
                    attendance_date=?,
                    check_in_time=?,
                    check_out_time=?,
                    status=?,
                    remarks=?
                WHERE id=?
                """;

        template.update(sql,
                attendance.getUserId(),
                attendance.getAttendanceDate(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getStatus().name(),
                attendance.getRemarks(),
                attendance.getId());

        return attendance;
    }

    @Override
    public void deleteById(Long id) {

        String sql = "DELETE FROM attendance WHERE id=?";

        template.update(sql, id);
    }

    @Override
    public List<Attendance> findByStatus(AttendanceStatus status) {

        String sql = "SELECT * FROM attendance WHERE status=?";

        return template.query(sql, (rs, rowNum) -> {

            Attendance a = new Attendance();

            a.setId(rs.getLong("id"));
            a.setUserId(rs.getLong("user_id"));
            a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());

            Time in = rs.getTime("check_in_time");
            if (in != null) {
                a.setCheckInTime(in.toLocalTime());
            }

            Time out = rs.getTime("check_out_time");
            if (out != null) {
                a.setCheckOutTime(out.toLocalTime());
            }

            a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
            a.setRemarks(rs.getString("remarks"));

            return a;

        }, status.name());
    }

    @Override
    public List<Attendance> findByUserId(Long userId) {

        String sql = "SELECT * FROM attendance WHERE user_id=?";

        return template.query(sql, (rs, rowNum) -> {

            Attendance a = new Attendance();

            a.setId(rs.getLong("id"));
            a.setUserId(rs.getLong("user_id"));
            a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());

            Time in = rs.getTime("check_in_time");
            if (in != null) {
                a.setCheckInTime(in.toLocalTime());
            }

            Time out = rs.getTime("check_out_time");
            if (out != null) {
                a.setCheckOutTime(out.toLocalTime());
            }

            a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
            a.setRemarks(rs.getString("remarks"));

            return a;

        }, userId);
    }
    @Override
    public List<Attendance> findByDate(LocalDate attendanceDate) {

        String sql = "SELECT * FROM attendance WHERE attendance_date=?";

        return template.query(sql, (rs, rowNum) -> {

            Attendance a = new Attendance();

            a.setId(rs.getLong("id"));
            a.setUserId(rs.getLong("user_id"));
            a.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());

            Time in = rs.getTime("check_in_time");
            if (in != null) {
                a.setCheckInTime(in.toLocalTime());
            }

            Time out = rs.getTime("check_out_time");
            if (out != null) {
                a.setCheckOutTime(out.toLocalTime());
            }

            a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
            a.setRemarks(rs.getString("remarks"));

            return a;

        }, attendanceDate);
    }
}