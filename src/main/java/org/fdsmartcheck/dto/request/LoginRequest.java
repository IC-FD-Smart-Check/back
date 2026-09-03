package org.fdsmartcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email ou RA é obrigatório")
    private String identifier;

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}