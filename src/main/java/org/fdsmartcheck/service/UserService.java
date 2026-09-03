package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.fdsmartcheck.dto.request.UserRequest;
import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // método createUser
    public UserResponse createUser(UserRequest request) {
        validateEmailOrRaPresent(request);

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email já cadastrado");
        }
        if (request.getRa() != null && !request.getRa().isBlank()
                && userRepository.existsByRa(request.getRa())) {
            throw new BadRequestException("RA já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .ra(request.getRa())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // método updateUser
    public UserResponse updateUser(String id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        validateEmailOrRaPresent(request);

        boolean emailChanged = !java.util.Objects.equals(user.getEmail(), request.getEmail());
        if (emailChanged && request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email já cadastrado");
        }
        boolean raChanged = !java.util.Objects.equals(user.getRa(), request.getRa());
        if (raChanged && request.getRa() != null && !request.getRa().isBlank()
                && userRepository.existsByRa(request.getRa())) {
            throw new BadRequestException("RA já cadastrado");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRa(request.getRa());
        user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);

    }

    private void validateEmailOrRaPresent(UserRequest request) {
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        boolean hasRa = request.getRa() != null && !request.getRa().isBlank();
        if (!hasEmail && !hasRa) {
            throw new BadRequestException("Informe email ou RA");
        }
    }

    // método getAllUsers
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // método searchUsers por nome, email ou RA
    public List<UserResponse> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRaContainingIgnoreCase(query, query, query).stream()
                .filter(User::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // método getUserById
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.getIsActive()) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        return mapToResponse(user);
    }

    // método deleteUser (soft delete)
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .ra(user.getRa())
                .role(user.getRole())
                .build();
    }
}