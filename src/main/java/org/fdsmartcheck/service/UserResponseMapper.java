package org.fdsmartcheck.service;

import org.fdsmartcheck.dto.response.UserResponse;
import org.fdsmartcheck.model.ClassGroup;
import org.fdsmartcheck.model.User;
import org.springframework.stereotype.Component;

/**
 * Conversão de User para UserResponse em um lugar só.
 * Evita que login, gestão de usuários e perfil devolvam formatos diferentes
 * quando um campo novo é adicionado.
 */
@Component
public class UserResponseMapper {

    public UserResponse toResponse(User user) {
        UserResponse.UserResponseBuilder response = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .ra(user.getRa())
                .role(user.getRole());

        ClassGroup classGroup = user.getClassGroup();
        if (classGroup != null) {
            response.classGroupId(classGroup.getId())
                    .classGroupName(classGroup.getName())
                    .semester(classGroup.getSemester())
                    .courseId(classGroup.getCourse().getId())
                    .courseName(classGroup.getCourse().getName());
        }

        return response.build();
    }
}
