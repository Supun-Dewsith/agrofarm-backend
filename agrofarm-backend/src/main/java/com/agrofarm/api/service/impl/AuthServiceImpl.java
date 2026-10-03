package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.auth.AuthResponse;
import com.agrofarm.api.dto.auth.LoginRequest;
import com.agrofarm.api.dto.auth.RegisterRequest;
import com.agrofarm.api.dto.auth.TokenRefreshRequest;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.exception.TokenRefreshException;
import com.agrofarm.api.repository.RefreshTokenRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.security.AuthenticatedUser;
import com.agrofarm.api.service.AuthService;
import com.agrofarm.api.service.CustomUserDetailsService;
import com.agrofarm.api.service.StorageService;
import com.agrofarm.api.service.TokenService;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StorageService storageService;

    public AuthResponse register(RegisterRequest request, MultipartFile file) {
        // Authenticate registrar
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthorized: Authentication required to register users.");
        }

        String registrarUsername = authentication.getName();
        UserEntity registrar = userRepository.findByUsername(registrarUsername)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found."));

        Role registrarRole = registrar.getRole();
        if (registrarRole == Role.FARMER) {
            throw new AccessDeniedException("Farmers are not permitted to register users.");
        }

        if (registrarRole == Role.SUPERVISOR) {
            if (registrar.getPermissions() != null && !registrar.getPermissions().contains("CAN_REGISTER_USERS")) {
                throw new AccessDeniedException("Supervisor does not have permission to register users.");
            }
        }

        Role targetRole = request.getRole();
        if (targetRole == null) {
            throw new IllegalArgumentException("System role is required.");
        }

        // Rule: Remove Admin from system roles
        if (targetRole == Role.ADMIN) {
            throw new IllegalArgumentException("Registering an Admin role is not permitted.");
        }

        // Rule: Admin can register Supervisor and Farmer, but Supervisor can only register Farmer
        if (registrarRole == Role.ADMIN) {
            if (targetRole != Role.SUPERVISOR && targetRole != Role.FARMER) {
                throw new IllegalArgumentException("Admins can only register Supervisors and Farmers.");
            }
        } else if (registrarRole == Role.SUPERVISOR) {
            if (targetRole != Role.FARMER) {
                throw new IllegalArgumentException("Supervisors can only register Farmers.");
            }
            if (registrar.getFarmIds() != null && !registrar.getFarmIds().isEmpty()) {
                for (Long farmId : request.getFarmIds()) {
                    if (!registrar.getFarmIds().contains(farmId)) {
                        throw new IllegalArgumentException("Supervisors can only assign farms that are allocated to them.");
                    }
                }
            }
        } else {
            throw new AccessDeniedException("Unauthorized to register users.");
        }

        // Rule: Can register user permission should be disabled when system role selected to farmer
        if (targetRole == Role.FARMER && request.getPermissions() != null) {
            request.getPermissions().remove("CAN_REGISTER_USERS");
        }

        // Rule: Make unable to register user without farm allocation
        if (request.getFarmIds() == null || request.getFarmIds().isEmpty()) {
            throw new IllegalArgumentException("Farm allocation is required. Please assign at least one farm.");
        }

        if (targetRole == Role.FARMER && request.getFarmIds().size() > 1) {
            throw new IllegalArgumentException("Farmers can only be assigned to a single farm.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepository.findByUsernameOrEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = targetRole;

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = storageService.uploadFile(file);
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setRole(role);
        entity.setEnabled(true);
        entity.setImageUrl(imageUrl);

        if (request.getPermissions() != null) {
            entity.setPermissions(new HashSet<>(request.getPermissions()));
        }

        if(request.getFarmIds() != null){
            entity.setFarmIds(new ArrayList<>(request.getFarmIds()));
        }

        userRepository.save(entity);

        UserEntity savedUser = userRepository.findByUsername(entity.getUsername())
                .orElseThrow(()-> new RuntimeException("Failed to retrieve saved user"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(entity.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.createAndSaveRefreshToken(savedUser.getId());

        return new AuthResponse(
                savedUser.getId(),
                accessToken,
                refreshToken,
                savedUser.getUsername(),
                role,
                savedUser.getPermissions(),
                savedUser.getImageUrl()
        );
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        AuthenticatedUser userDetails = (AuthenticatedUser) authentication.getPrincipal();

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.createAndSaveRefreshToken(userDetails.getId());

        Role role = Role.valueOf(
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("FARMER")
        );

        return new AuthResponse(
                userDetails.getId(),
                accessToken,
                refreshToken,
                userDetails.getUsername(),
                role,
                userDetails.getPermissions(),
                userDetails.getImageUrl()
        );
    }

    public AuthResponse refreshToken(TokenRefreshRequest request){
        String requestRefreshToken = request.refreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(tokenService::verifyExpiration)
                .map(refreshToken -> {
                    UserEntity user = userRepository.findById(refreshToken.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));

                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                    String newAccessToken = tokenService.generateAccessToken(userDetails);

                    Role role = Role.valueOf(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .findFirst().orElse("FARMER"));

                    return new AuthResponse(user.getId(), newAccessToken, requestRefreshToken, user.getUsername(), role ,user.getPermissions(),user.getImageUrl());
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token is invalid or not found!"));
    }
}
