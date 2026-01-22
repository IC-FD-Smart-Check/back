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

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        // Autenticar usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Buscar usuário
        User user = userRepository.findByEmail(request.getEmail())
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
        // Verificar se usuário existe
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // TODO: Implementar envio de email com token de redefinição de senha
        // Por enquanto, apenas simular o envio
        System.out.println("Email de recuperação enviado para: " + user.getEmail());
    }
}