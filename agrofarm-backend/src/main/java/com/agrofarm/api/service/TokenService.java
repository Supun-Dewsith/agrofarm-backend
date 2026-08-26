package com.agrofarm.api.service;

import com.agrofarm.api.entity.RefreshToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenService {
    String generateAccessToken(UserDetails userDetails);
    String createAndSaveRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
}
