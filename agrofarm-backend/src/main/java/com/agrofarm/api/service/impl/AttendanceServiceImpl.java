package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.AttendanceCodeRequest;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;
import com.agrofarm.api.dto.attendance.AttendanceFarmResponse;
import com.agrofarm.api.dto.attendance.AttendanceQrSession;
import com.agrofarm.api.dto.attendance.AttendanceReportSummary;
import com.agrofarm.api.dto.attendance.AttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceWorkerResponse;
import com.agrofarm.api.dto.attendance.QrGenerateResponse;
import com.agrofarm.api.dto.attendance.QrLiveCodeResponse;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.AttendanceRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.AttendanceService;
import com.agrofarm.api.util.Role;
import com.agrofarm.api.util.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime LATE_AFTER = LocalTime.of(8, 15);

    private static final int QR_VALID_MINUTES = 60;

    private static final int LIVE_CODE_ROTATION_SECONDS = 45;

    private static final double MAX_ACCEPTED_LOCATION_ACCURACY_METERS = 100.0;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private String roleName(UserEntity user) {
        return user.getRole().name();
    }

    private void requireFarmAccess(UserEntity viewer, Long farmId) {
        if (farmId == null) {
            return;
        }

        if (!attendanceRepository.canAccessFarm(viewer.getId(), roleName(viewer), farmId)) {
            throw new AccessDeniedException("You do not have access to this farm");
        }
    }

    private void requireUserAccess(UserEntity viewer, Long targetUserId) {
        if (!attendanceRepository.canAccessUser(viewer.getId(), roleName(viewer), targetUserId)) {
            throw new AccessDeniedException("You do not have access to this worker's attendance");
        }
    }

    private Long effectiveReportUserId(UserEntity viewer, Long requestedUserId) {
        if (viewer.getRole() == Role.FARMER) {
            if (requestedUserId != null && !viewer.getId().equals(requestedUserId)) {
                throw new AccessDeniedException("Farmers can only view their own attendance");
            }
            return viewer.getId();
        }

        if (requestedUserId != null) {
            requireUserAccess(viewer, requestedUserId);
        }

        return requestedUserId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceFarmResponse> getFarms() {
        UserEntity viewer = currentUser();
        return attendanceRepository.findFarmsForUser(viewer.getId(), roleName(viewer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceWorkerResponse> getWorkers(Long farmId, String search) {
        UserEntity viewer = currentUser();
        requireFarmAccess(viewer, farmId);

        return attendanceRepository.findWorkers(
                viewer.getId(),
                roleName(viewer),
                farmId,
                search
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getDailyAttendance(LocalDate date, Long farmId, String search) {
        UserEntity viewer = currentUser();
        requireFarmAccess(viewer, farmId);

        return attendanceRepository.findDailyAttendance(
                viewer.getId(),
                roleName(viewer),
                date,
                farmId,
                search
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDashboardResponse getDashboard(LocalDate date, Long farmId) {
        UserEntity viewer = currentUser();
        requireFarmAccess(viewer, farmId);

        return attendanceRepository.dashboard(
                viewer.getId(),
                roleName(viewer),
                date,
                farmId
        );
    }

    @Override
    public QrGenerateResponse generateQr(Long farmId) {
        if (farmId == null) {
            throw new IllegalArgumentException("Please select a farm before generating QR");
        }

        UserEntity viewer = currentUser();

        if (viewer.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Farmers cannot generate attendance QR codes");
        }

        requireFarmAccess(viewer, farmId);

        AttendanceFarmResponse farm = attendanceRepository.findFarmsForUser(
                        viewer.getId(),
                        roleName(viewer)
                )
                .stream()
                .filter(item -> item.id().equals(farmId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this farm"));

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(QR_VALID_MINUTES);
        String token = UUID.randomUUID().toString();
        String manualCode = generateUniqueManualCode(createdAt);

        attendanceRepository.createQrToken(
                token,
                manualCode,
                farmId,
                viewer.getId(),
                expiresAt
        );

        Instant instant = Instant.now();

        return new QrGenerateResponse(
                token,
                farmId,
                farm.farmName(),
                manualCode,
                rotatingCode(token, instant),
                LIVE_CODE_ROTATION_SECONDS,
                secondsRemainingInRotation(instant),
                rotatingCodeExpiresAt(instant),
                createdAt,
                expiresAt,
                viewer.getId(),
                viewer.getUsername(),
                viewer.getRole().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public QrLiveCodeResponse getQrLiveCode(String token) {
        UserEntity viewer = currentUser();

        if (viewer.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Farmers cannot view attendance session live codes");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("QR token is required");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceQrSession session = attendanceRepository.findValidQrSessionByToken(token, now)
                .orElseThrow(() -> new IllegalArgumentException(
                        "This attendance session is invalid or expired. Generate a new QR."
                ));

        requireFarmAccess(viewer, session.farmId());

        Instant instant = Instant.now();
        return new QrLiveCodeResponse(
                rotatingCode(session.token(), instant),
                LIVE_CODE_ROTATION_SECONDS,
                secondsRemainingInRotation(instant),
                now,
                rotatingCodeExpiresAt(instant),
                session.expiresAt()
        );
    }

    @Override
    public QrAttendanceResponse processQrAttendance(QrAttendanceRequest request) {
        UserEntity worker = requireFarmerForMarking();
        LocalDateTime now = LocalDateTime.now();

        AttendanceQrSession session = attendanceRepository.findValidQrSessionByToken(
                        request.getQrToken(),
                        now
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "This QR code is invalid or expired. Generate a new QR."
                ));

        return markAttendance(
                worker,
                session.farmId(),
                request.getLocation(),
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracyMeters(),
                request.getDeviceType(),
                now
        );
    }

    @Override
    public QrAttendanceResponse processCodeAttendance(AttendanceCodeRequest request) {
        UserEntity worker = requireFarmerForMarking();
        LocalDateTime now = LocalDateTime.now();

        String sessionCode = request.getSessionCode().trim();
        String liveNumber = request.getRotatingCode().trim();

        AttendanceQrSession session = attendanceRepository.findValidQrSessionByManualCode(
                        sessionCode,
                        now
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "The 4-digit attendance code is invalid or expired."
                ));

        String expected = rotatingCode(session.token(), Instant.now());
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                liveNumber.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IllegalArgumentException(
                    "The 2-digit live number has changed or is incorrect. Enter the current number shown under the QR."
            );
        }

        return markAttendance(
                worker,
                session.farmId(),
                request.getLocation(),
                request.getLatitude(),
                request.getLongitude(),
                request.getAccuracyMeters(),
                request.getDeviceType(),
                now
        );
    }

    private UserEntity requireFarmerForMarking() {
        UserEntity worker = currentUser();
        if (worker.getRole() != Role.FARMER) {
            throw new AccessDeniedException("Only FARMER users can mark attendance");
        }
        return worker;
    }

    private QrAttendanceResponse markAttendance(
            UserEntity worker,
            Long farmId,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters,
            String deviceType,
            LocalDateTime now
    ) {
        String normalizedDeviceType = normalizeDeviceType(deviceType);
        boolean mobile = "MOBILE".equals(normalizedDeviceType);

        if (!attendanceRepository.isWorkerAssignedToFarm(worker.getId(), farmId)) {
            throw new AccessDeniedException("You are not assigned to this farm");
        }

        if (mobile) {
            validateLocation(latitude, longitude, accuracyMeters);
            validateInsideFarmBoundary(farmId, latitude, longitude);
        } else {
            boolean anyLocationValue = latitude != null
                    || longitude != null
                    || accuracyMeters != null;

            if (anyLocationValue) {
                validateLocation(latitude, longitude, accuracyMeters);
            }

            if (location == null || location.isBlank()) {
                location = "Desktop/Laptop attendance - location not required";
            }
        }

        var existing = attendanceRepository.findAttendance(
                worker.getId(),
                farmId,
                now.toLocalDate()
        );

        if (existing.isEmpty()) {
            Status status = now.toLocalTime().isAfter(LATE_AFTER)
                    ? Status.LATE
                    : Status.PRESENT;

            AttendanceResponse saved = attendanceRepository.insertCheckIn(
                    worker.getId(),
                    farmId,
                    now,
                    status.name(),
                    location,
                    latitude,
                    longitude,
                    accuracyMeters
            );

            return new QrAttendanceResponse(
                    true,
                    "Attendance check-in marked successfully",
                    saved.checkInTime(),
                    null,
                    saved.status(),
                    saved.latitude(),
                    saved.longitude(),
                    saved.accuracyMeters()
            );
        }

        AttendanceResponse attendance = existing.get();

        if (attendance.checkOutTime() == null) {
            AttendanceResponse saved = attendanceRepository.updateCheckOut(
                    attendance.id(),
                    now,
                    location,
                    latitude,
                    longitude,
                    accuracyMeters
            );

            return new QrAttendanceResponse(
                    true,
                    "Attendance check-out marked successfully",
                    saved.checkInTime(),
                    saved.checkOutTime(),
                    saved.status(),
                    saved.checkOutLatitude(),
                    saved.checkOutLongitude(),
                    saved.checkOutAccuracyMeters()
            );
        }

        return new QrAttendanceResponse(
                false,
                "Attendance is already completed for today",
                attendance.checkInTime(),
                attendance.checkOutTime(),
                attendance.status(),
                attendance.checkOutLatitude() != null ? attendance.checkOutLatitude() : attendance.latitude(),
                attendance.checkOutLongitude() != null ? attendance.checkOutLongitude() : attendance.longitude(),
                attendance.checkOutAccuracyMeters() != null
                        ? attendance.checkOutAccuracyMeters()
                        : attendance.accuracyMeters()
        );
    }


    private String normalizeDeviceType(String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            throw new IllegalArgumentException("Device type is required");
        }

        String normalized = deviceType.trim().toUpperCase(Locale.ROOT);

        if (!"MOBILE".equals(normalized) && !"DESKTOP".equals(normalized)) {
            throw new IllegalArgumentException("Device type must be MOBILE or DESKTOP");
        }

        return normalized;
    }


    private void validateInsideFarmBoundary(
            Long farmId,
            Double latitude,
            Double longitude
    ) {
        String boundaryJson = attendanceRepository.findFarmBoundary(farmId)
                .orElseThrow(() -> new IllegalStateException(
                        "This farm does not have a valid boundary. Draw and save the farm boundary before using attendance."
                ));

        try {
            JsonNode root = objectMapper.readTree(boundaryJson);

            if (!root.isArray() || root.size() < 3) {
                throw new IllegalStateException(
                        "This farm boundary is invalid. At least 3 boundary points are required."
                );
            }

            List<double[]> polygon = new ArrayList<>();

            for (JsonNode item : root) {
                if (!item.isArray() || item.size() < 2) {
                    continue;
                }

                double lat = item.get(0).asDouble(Double.NaN);
                double lng = item.get(1).asDouble(Double.NaN);

                if (Double.isFinite(lat) && Double.isFinite(lng)) {
                    polygon.add(new double[]{lat, lng});
                }
            }

            if (polygon.size() < 3) {
                throw new IllegalStateException(
                        "This farm boundary is invalid. At least 3 valid boundary points are required."
                );
            }

            if (!isPointInsidePolygon(latitude, longitude, polygon)) {
                throw new AccessDeniedException(
                        "Attendance can only be marked while you are inside the selected farm boundary."
                );
            }

        } catch (AccessDeniedException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to read the saved farm boundary. Please update the farm boundary and try again.",
                    e
            );
        }
    }

    private boolean isPointInsidePolygon(
            double latitude,
            double longitude,
            List<double[]> polygon
    ) {
        boolean inside = false;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double yi = polygon.get(i)[0];
            double xi = polygon.get(i)[1];
            double yj = polygon.get(j)[0];
            double xj = polygon.get(j)[1];

            if (isPointOnSegment(latitude, longitude, yi, xi, yj, xj)) {
                return true;
            }

            boolean intersects = ((yi > latitude) != (yj > latitude))
                    && (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi);

            if (intersects) {
                inside = !inside;
            }
        }

        return inside;
    }

    private boolean isPointOnSegment(
            double py,
            double px,
            double ay,
            double ax,
            double by,
            double bx
    ) {
        double cross = (px - ax) * (by - ay) - (py - ay) * (bx - ax);

        if (Math.abs(cross) > 1e-10) {
            return false;
        }

        double minX = Math.min(ax, bx) - 1e-10;
        double maxX = Math.max(ax, bx) + 1e-10;
        double minY = Math.min(ay, by) - 1e-10;
        double maxY = Math.max(ay, by) + 1e-10;

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    private void validateLocation(Double latitude, Double longitude, Double accuracyMeters) {
        if (latitude == null || longitude == null || accuracyMeters == null) {
            throw new IllegalArgumentException(
                    "Precise latitude, longitude and accuracy are required to mark attendance"
            );
        }

        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Invalid latitude received from the browser");
        }

        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Invalid longitude received from the browser");
        }

        if (!Double.isFinite(accuracyMeters) || accuracyMeters <= 0.0) {
            throw new IllegalArgumentException("Invalid browser location accuracy");
        }

        if (accuracyMeters > MAX_ACCEPTED_LOCATION_ACCURACY_METERS) {
            throw new IllegalArgumentException(
                    "Location is not accurate enough. Required accuracy is 100 meters or better. Current accuracy: ±"
                            + Math.round(accuracyMeters) + "m"
            );
        }
    }

    private String generateUniqueManualCode(LocalDateTime now) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String code = String.format(Locale.ROOT, "%04d", SECURE_RANDOM.nextInt(10_000));
            if (!attendanceRepository.activeManualCodeExists(code, now)) {
                return code;
            }
        }

        throw new IllegalStateException("Unable to generate a unique attendance code. Please try again.");
    }

    private String rotatingCode(String token, Instant instant) {
        long slot = instant.getEpochSecond() / LIVE_CODE_ROTATION_SECONDS;
        String input = token + ":" + slot;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            int value = (((hash[0] & 0xff) << 8) | (hash[1] & 0xff)) % 100;
            return String.format(Locale.ROOT, "%02d", value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private LocalDateTime rotatingCodeExpiresAt(Instant instant) {
        long current = instant.getEpochSecond();
        long nextBoundary = ((current / LIVE_CODE_ROTATION_SECONDS) + 1)
                * LIVE_CODE_ROTATION_SECONDS;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(nextBoundary),
                ZoneId.systemDefault()
        );
    }

    private int secondsRemainingInRotation(Instant instant) {
        long current = instant.getEpochSecond();
        long elapsed = current % LIVE_CODE_ROTATION_SECONDS;
        return (int) (LIVE_CODE_ROTATION_SECONDS - elapsed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getHistory(Long userId) {
        UserEntity viewer = currentUser();
        Long targetUserId = userId;

        if (viewer.getRole() == Role.FARMER) {
            if (!viewer.getId().equals(userId)) {
                throw new AccessDeniedException("Farmers can only view their own attendance history");
            }
            targetUserId = viewer.getId();
        } else {
            requireUserAccess(viewer, targetUserId);
        }

        return attendanceRepository.findReport(
                viewer.getId(),
                roleName(viewer),
                LocalDate.now().minusMonths(6),
                LocalDate.now(),
                null,
                targetUserId,
                ""
        );
    }

    @Override
    public AttendanceResponse checkOut(
            Long userId,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters
    ) {
        UserEntity viewer = currentUser();

        if (viewer.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Farmers must check out using the active farm QR/code session");
        }

        requireUserAccess(viewer, userId);

        List<AttendanceResponse> todayRows = attendanceRepository.findReport(
                viewer.getId(),
                roleName(viewer),
                LocalDate.now(),
                LocalDate.now(),
                null,
                userId,
                ""
        );

        AttendanceResponse attendance = todayRows.stream()
                .filter(row -> row.checkOutTime() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No open check-in found for this worker today"
                ));

        if (attendance.farmId() != null) {
            requireFarmAccess(viewer, attendance.farmId());
        }

        return attendanceRepository.updateCheckOut(
                attendance.id(),
                LocalDateTime.now(),
                location,
                latitude,
                longitude,
                accuracyMeters
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getReport(
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    ) {
        validateDateRange(from, to);

        UserEntity viewer = currentUser();
        requireFarmAccess(viewer, farmId);
        Long effectiveUserId = effectiveReportUserId(viewer, userId);

        return attendanceRepository.findReport(
                viewer.getId(),
                roleName(viewer),
                from,
                to,
                farmId,
                effectiveUserId,
                search
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceReportSummary> getReportSummary(
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    ) {
        validateDateRange(from, to);

        UserEntity viewer = currentUser();
        requireFarmAccess(viewer, farmId);
        Long effectiveUserId = effectiveReportUserId(viewer, userId);

        return attendanceRepository.findReportSummary(
                viewer.getId(),
                roleName(viewer),
                from,
                to,
                farmId,
                effectiveUserId,
                search
        );
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Report start date and end date are required");
        }

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Report end date cannot be before start date");
        }
    }
}