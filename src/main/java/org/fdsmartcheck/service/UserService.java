package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.fdsmartcheck.dto.request.UserRequest;
import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.exception.BadRequestException;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.ClassGroup;
import org.fdsmartcheck.model.User;
import org.fdsmartcheck.model.enums.Role;
import org.fdsmartcheck.repository.ClassGroupRepository;
import org.fdsmartcheck.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClassGroupRepository classGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserResponseMapper userResponseMapper;

    // método createUser
    @Transactional
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

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Senha é obrigatória");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .ra(request.getRa())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .classGroup(resolveClassGroup(request))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    // método updateUser
    @Transactional
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
        user.setClassGroup(resolveClassGroup(request));

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
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllActiveWithClassGroup().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // método searchUsers por nome, email ou RA
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        return userRepository.searchActiveWithClassGroup(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // método getUserById
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.getIsActive()) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        return mapToResponse(user);
    }

    // método deleteUser (soft delete)
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Resolve a turma do usuário conforme o papel:
     * - STUDENT: turma é obrigatória e precisa existir
     * - ADMIN: turma não é aceita (usuário sem vínculo de turma)
     */
    private ClassGroup resolveClassGroup(UserRequest request) {
        String classGroupId = request.getClassGroupId();
        boolean classGroupInformed = classGroupId != null && !classGroupId.isBlank();

        if (Role.STUDENT.equals(request.getRole())) {
            if (!classGroupInformed) {
                throw new BadRequestException("Turma é obrigatória para usuários com papel STUDENT");
            }

            return classGroupRepository.findById(classGroupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
        }

        if (classGroupInformed) {
            throw new BadRequestException("Apenas usuários com papel STUDENT podem ser vinculados a uma turma");
        }

        return null;
    }

    private UserResponse mapToResponse(User user) {
        return userResponseMapper.toResponse(user);
    }

}
