package org.fdsmartcheck.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.UpdateEmailRequest;
import org.fdsmartcheck.dto.request.UpdatePasswordRequest;
import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Dados do próprio usuário autenticado — qualquer papel, sobre si mesmo.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/me — dados atualizados do usuário logado
     */
    @GetMapping
    public ResponseEntity<UserResponse> getCurrentProfile() {
        return ResponseEntity.ok(profileService.getCurrentProfile());
    }

    /**
     * PATCH /api/me/email — o próprio usuário cadastra ou troca seu email
     */
    /**
     * PATCH /api/me/password — o próprio usuário troca sua senha
     */
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        profileService.updatePassword(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/email")
    public ResponseEntity<UserResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(profileService.updateEmail(request));
    }
}
