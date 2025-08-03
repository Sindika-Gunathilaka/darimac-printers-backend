package com.example.printingApp.service;

import com.example.printingApp.config.JwtUtil;
import com.example.printingApp.model.RefreshToken;
import com.example.printingApp.model.User;
import com.example.printingApp.repository.RefreshTokenRepository;
import com.example.printingApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke all existing refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        // Use a fresh user reference from database to avoid password=null issue
        User userRef = userRepository.getReferenceById(user.getId());
        refreshToken.setUser(userRef);
        refreshToken.setToken(jwtUtil.generateRefreshToken());
        refreshToken.setExpiryDate(jwtUtil.getRefreshTokenExpiryDate());

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user.getId(), LocalDateTime.now());
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    public boolean isValidRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            return false;
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        return refreshToken.isValid();
    }
}