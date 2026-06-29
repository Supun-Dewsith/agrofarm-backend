package com.agrofarm.api.dto;

import com.agrofarm.api.util.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private Role role;

    public AuthResponse(String accessToken, String refreshToken, String username, Role role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
    }
}
