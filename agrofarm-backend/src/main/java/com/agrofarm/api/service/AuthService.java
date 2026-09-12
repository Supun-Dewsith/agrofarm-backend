package com.agrofarm.api.service;

import com.agrofarm.api.dto.auth.AuthResponse;
import com.agrofarm.api.dto.auth.LoginRequest;
import com.agrofarm.api.dto.auth.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(RegisterRequest request, MultipartFile file);
    AuthResponse login(LoginRequest request);
}
