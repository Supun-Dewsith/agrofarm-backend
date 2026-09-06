package com.agrofarm.api.dto;

import com.agrofarm.api.util.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private Role role;
    private Set<String> permissions = new HashSet<>();
    private String imageUrl;

    public AuthResponse(String accessToken, String refreshToken, String username, Role role, Set<String> permissions, String imageUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.imageUrl = imageUrl;
    }
}
