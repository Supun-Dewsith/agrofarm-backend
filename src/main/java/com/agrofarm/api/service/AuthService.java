package com.agrofarm.api.service;

import com.agrofarm.api.dto.AuthResponse;
import com.agrofarm.api.dto.LoginRequest;
import com.agrofarm.api.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
