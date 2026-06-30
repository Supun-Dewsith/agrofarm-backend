package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.AuthResponse;
import com.agrofarm.api.dto.LoginRequest;
import com.agrofarm.api.dto.RegisterRequest;
import com.agrofarm.api.dto.TokenRefreshRequest;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.exception.TokenRefreshException;
import com.agrofarm.api.repository.RefreshTokenRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.AuthService;
import com.agrofarm.api.service.CustomUserDetailsService;
import com.agrofarm.api.service.TokenService;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        Role role = (request.getRole() != null) ? request.getRole() : Role.FARMER;

        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setRole(role);
        entity.setEnabled(true);

        userRepository.save(entity);

        UserEntity savedUser = userRepository.findByUsername(entity.getUsername())
                .orElseThrow(()-> new RuntimeException("Failed to retrieve saved user"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(entity.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.createAndSaveRefreshToken(savedUser.getId());

        return new AuthResponse(accessToken, refreshToken, savedUser.getUsername(), role);
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserEntity entity = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.createAndSaveRefreshToken(entity.getId());

        Role role = Role.valueOf(
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("FARMER")
        );

        return new AuthResponse(accessToken, refreshToken, userDetails.getUsername(),role);
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

                    return new AuthResponse(newAccessToken, requestRefreshToken, user.getUsername(), role);
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token is invalid or not found!"));
    }
}
