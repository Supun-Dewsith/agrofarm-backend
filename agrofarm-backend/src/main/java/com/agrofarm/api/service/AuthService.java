package com.agrofarm.api.service;

import com.agrofarm.api.dto.AuthResponse;
import com.agrofarm.api.dto.LoginRequest;
import com.agrofarm.api.dto.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(RegisterRequest request, MultipartFile file);
    AuthResponse login(LoginRequest request);
}
