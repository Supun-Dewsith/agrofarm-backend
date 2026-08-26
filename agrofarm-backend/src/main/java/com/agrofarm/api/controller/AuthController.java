package com.agrofarm.api.controller;

import com.agrofarm.api.dto.AuthResponse;
import com.agrofarm.api.dto.LoginRequest;
import com.agrofarm.api.dto.RegisterRequest;
import com.agrofarm.api.dto.TokenRefreshRequest;
import com.agrofarm.api.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRefreshRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
