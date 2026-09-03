package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fdsmartcheck.model.enums.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    private String ra;

    // Obrigatória na criação; opcional na atualização (validado em UserService)
    private String password;
    
    @NotNull(message = "Papel é obrigatório")
    private Role role;

    // Obrigatório quando role = STUDENT; não deve ser enviado para ADMIN
    private String classGroupId;
}
