package com.agrofarm.api.service.impl;


import com.agrofarm.api.entity.RefreshToken;
import com.agrofarm.api.exception.TokenRefreshException;
import com.agrofarm.api.repository.RefreshTokenRepository;
import com.agrofarm.api.service.JwtService;
import com.agrofarm.api.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    private final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return jwtService.generateToken(userDetails);
    }

    @Override
    public String createAndSaveRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        String refreshToken = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS);
        refreshTokenRepository.save(refreshToken,userId,expiryDate);
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.deleteByToken(token.getToken());
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
