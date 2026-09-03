package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.fdsmartcheck.dto.request.UpdateEmailRequest;
import org.fdsmartcheck.dto.request.UpdatePasswordRequest;
import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.UnauthorizedException;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ações que o próprio usuário autenticado faz sobre seus dados,
 * sem depender de um administrador.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile() {
        return userResponseMapper.toResponse(getCurrentUser());
    }

    /**
     * Cadastra ou troca o email do próprio usuário.
     * Alunos importados entram no sistema apenas com RA e usam isto para se completarem.
     */
    @Transactional
    public UserResponse updateEmail(UpdateEmailRequest request) {
        User user = getCurrentUser();

        // Login por email compara o valor exato; guardar em minúsculo evita
        // que o usuário fique sem conseguir entrar por ter digitado com maiúscula
        String email = request.getEmail().trim().toLowerCase();

        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Este email já está em uso por outro usuário");
        }

        user.setEmail(email);

        return userResponseMapper.toResponse(userRepository.save(user));
    }

    /** Troca da própria senha, exigindo a senha atual. */
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Senha atual incorreta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("A nova senha deve ser diferente da atual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {
        // Desde a mudança do login por RA, o principal é sempre o id do usuário
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByIdWithClassGroup(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }
}
