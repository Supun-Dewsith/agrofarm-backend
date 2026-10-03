package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UpdateUserRequest;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserManagementRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.StorageService;
import com.agrofarm.api.service.UserManagementService;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserManagementRepository userManagementRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Override
    public List<UserData> getAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Role currentRole = null;
        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<UserEntity> currentUser = userRepository.findByUsername(username);
            if (currentUser.isPresent()) {
                currentRole = currentUser.get().getRole();
                currentUserId = currentUser.get().getId();
            }
        }
        return userManagementRepository.getAll(currentRole, currentUserId);
    }

    @Override
    @Transactional
    public UserData updateUser(Long id, UpdateUserRequest request, MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthorized: Authentication required to edit staff.");
        }

        String currentUsername = authentication.getName();
        UserEntity currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        Role registrarRole = currentUser.getRole();
        if (registrarRole == Role.FARMER) {
            throw new AccessDeniedException("Farmers are not permitted to edit users.");
        }

        if (registrarRole == Role.SUPERVISOR) {
            if (currentUser.getPermissions() != null && !currentUser.getPermissions().contains("CAN_REGISTER_USERS")) {
                throw new AccessDeniedException("Supervisor does not have permission to edit users.");
            }
        }

        UserEntity targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Admin accounts cannot be edited from this staff management screen
        if (targetUser.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be modified here.");
        }

        // Supervisor can only edit Farmers allocated to their farms
        if (registrarRole == Role.SUPERVISOR) {
            if (targetUser.getRole() != Role.FARMER) {
                throw new AccessDeniedException("Supervisors can only edit Farmers.");
            }
            if (request.getRole() != null && request.getRole() != Role.FARMER) {
                throw new IllegalArgumentException("Supervisors can only edit Farmers and cannot change user role.");
            }

            boolean belongsToSupervisorFarms = false;
            if (currentUser.getFarmIds() != null && targetUser.getFarmIds() != null) {
                for (Long farmId : targetUser.getFarmIds()) {
                    if (currentUser.getFarmIds().contains(farmId)) {
                        belongsToSupervisorFarms = true;
                        break;
                    }
                }
            }
            if (!belongsToSupervisorFarms) {
                throw new AccessDeniedException("Supervisors can only edit farmers allocated to their farms.");
            }
        }

        // Admin cannot change role to ADMIN
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot assign ADMIN role to staff.");
        }

        Role newRole = request.getRole() != null ? request.getRole() : targetUser.getRole();

        // Permissions check
        Set<String> newPermissions = request.getPermissions() != null ? new HashSet<>(request.getPermissions()) : new HashSet<>(targetUser.getPermissions());
        if (newRole == Role.FARMER) {
            newPermissions.remove("CAN_REGISTER_USERS");
        }

        // Farm allocation check: must not be empty
        List<Long> newFarmIds = request.getFarmIds();
        if (newFarmIds == null || newFarmIds.isEmpty()) {
            throw new IllegalArgumentException("Farm allocation is required. Please assign at least one farm.");
        }
        if (newRole == Role.FARMER && newFarmIds.size() > 1) {
            throw new IllegalArgumentException("Farmers can only be assigned to a single farm.");
        }

        if (registrarRole == Role.SUPERVISOR && currentUser.getFarmIds() != null && !currentUser.getFarmIds().isEmpty()) {
            for (Long farmId : newFarmIds) {
                if (!currentUser.getFarmIds().contains(farmId)) {
                    throw new IllegalArgumentException("Supervisors can only assign farms that are allocated to them.");
                }
            }
        }

        // Check username uniqueness if changed
        if (!targetUser.getUsername().equalsIgnoreCase(request.getUsername())) {
            Optional<UserEntity> existing = userRepository.findByUsername(request.getUsername());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("Username already taken: " + request.getUsername());
            }
            targetUser.setUsername(request.getUsername());
        }

        // Check email uniqueness if changed
        if (!targetUser.getEmail().equalsIgnoreCase(request.getEmail())) {
            Optional<UserEntity> existing = userRepository.findByUsernameOrEmail(request.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email already in use: " + request.getEmail());
            }
            targetUser.setEmail(request.getEmail());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            }
            targetUser.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        // Upload new image if provided
        if (file != null && !file.isEmpty()) {
            String imageUrl = storageService.uploadFile(file);
            targetUser.setImageUrl(imageUrl);
        }

        targetUser.setRole(newRole);
        targetUser.setPermissions(newPermissions);
        targetUser.setFarmIds(newFarmIds);

        userManagementRepository.updateUser(targetUser);

        return new UserData(
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getCreatedAt(),
                targetUser.getEmail(),
                targetUser.getPhoneNumber(),
                targetUser.getRole(),
                targetUser.getEnabled(),
                targetUser.getImageUrl(),
                targetUser.getPermissions(),
                targetUser.getFarmIds()
        );
    }

    @Override
    public AvailableFarmsResponse getAvailableFarms() {
        return userManagementRepository.getAvailableFarms();
    }

    @Override
    public AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId) {
        return userManagementRepository.getAvailableFarmsByOwnerId(ownerId);
    }
}
