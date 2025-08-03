package com.example.printingApp.controller;

import com.example.printingApp.config.JwtUtil;
import com.example.printingApp.model.RefreshToken;
import com.example.printingApp.model.User;
import com.example.printingApp.service.RefreshTokenService;
import com.example.printingApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticateUser(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
            
            String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().toString());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken.getToken());
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtUtil.getJwtExpirationMs() / 1000); // seconds
            response.put("user", user);
            response.put("message", "Login successful");
            
            // Keep 'token' for backward compatibility
            response.put("token", accessToken);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User savedUser = userService.createUser(user);
            
            String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId(), savedUser.getRole().toString());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", savedUser);
            response.put("message", "Registration successful");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Long userId = (Long) authentication.getDetails();
                User user = userService.getUserProfile(userId);
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String requestRefreshToken = refreshTokenRequest.getRefreshToken();
            
            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().toString());
                        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("accessToken", newAccessToken);
                        response.put("refreshToken", newRefreshToken.getToken());
                        response.put("tokenType", "Bearer");
                        response.put("expiresIn", jwtUtil.getJwtExpirationMs() / 1000);
                        response.put("message", "Token refreshed successfully");
                        
                        // Keep 'token' for backward compatibility
                        response.put("token", newAccessToken);
                        
                        return ResponseEntity.ok(response);
                    })
                    .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest logoutRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Long) {
                Long userId = (Long) authentication.getDetails();
                userService.getUserById(userId).ifPresent(user -> {
                    // Revoke refresh tokens
                    if (logoutRequest != null && logoutRequest.getRefreshToken() != null) {
                        // Revoke specific refresh token
                        refreshTokenService.findByToken(logoutRequest.getRefreshToken())
                                .ifPresent(refreshTokenService::revokeToken);
                    } else {
                        // Revoke all refresh tokens for user
                        refreshTokenService.revokeAllUserTokens(user);
                    }
                });
            }
            
            SecurityContextHolder.clearContext();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        }
    }

    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String usernameOrEmail, String password) {
            this.usernameOrEmail = usernameOrEmail;
            this.password = password;
        }

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public RefreshTokenRequest() {}

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class LogoutRequest {
        private String refreshToken;

        public LogoutRequest() {}

        public LogoutRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}