package org.fdsmartcheck.controller;

import org.fdsmartcheck.dto.request.ForgotPasswordRequest;
import org.fdsmartcheck.dto.request.LoginRequest;
import org.fdsmartcheck.dto.response.LoginResponse;
import org.fdsmartcheck.security.TokenBlacklistService;
import org.fdsmartcheck.security.JwtTokenProvider;
import org.fdsmartcheck.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Email de recuperação enviado com sucesso"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                java.util.Date expiration = jwtTokenProvider.extractClaim(token,
                        io.jsonwebtoken.Claims::getExpiration);
                tokenBlacklistService.blacklist(token, expiration.getTime());
            } catch (Exception e) {
                // Token already invalid — ignore
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }
}