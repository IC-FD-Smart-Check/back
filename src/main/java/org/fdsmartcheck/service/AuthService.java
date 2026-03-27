package org.fdsmartcheck.service;

import org.fdsmartcheck.dto.request.ForgotPasswordRequest;
import org.fdsmartcheck.dto.request.LoginRequest;
import org.fdsmartcheck.dto.request.UserRequest;
import org.fdsmartcheck.dto.response.LoginResponse;
import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.repository.UserRepository;
import org.fdsmartcheck.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private final java.util.concurrent.ConcurrentHashMap<String, Integer> failedAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, Long> lockoutUntil = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000L; // 5 minutes

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        Long lockedUntil = lockoutUntil.get(email);
        if (lockedUntil != null && System.currentTimeMillis() < lockedUntil) {
            long secondsLeft = (lockedUntil - System.currentTimeMillis()) / 1000;
            throw new BadRequestException("Conta temporariamente bloqueada. Tente novamente em " + secondsLeft + " segundos.");
        }

        // Autenticar usuário
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            int attempts = failedAttempts.merge(email, 1, Integer::sum);
            if (attempts >= MAX_ATTEMPTS) {
                lockoutUntil.put(email, System.currentTimeMillis() + LOCKOUT_DURATION_MS);
                failedAttempts.remove(email);
                throw new BadRequestException("Conta bloqueada após " + MAX_ATTEMPTS + " tentativas. Tente novamente em 5 minutos.");
            }
            throw new BadRequestException("Email ou senha inválidos");
        }

        // Clear failed attempts on success
        failedAttempts.remove(email);
        lockoutUntil.remove(email);

        // Buscar usuário
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Usuário inativo");
        }

        // Gerar token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        // Construir resposta
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        // Always log, never reveal if email exists (prevents user enumeration)
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            // TODO: Generate secure reset token, save with TTL, send email
            // For now: log token for development (replace with email service in production)
            String resetToken = java.util.UUID.randomUUID().toString();
            logger.info("Password reset requested for user: {} | token: {}", user.getEmail(), resetToken);
        });
        // Always returns normally — same response whether email exists or not
    }
}