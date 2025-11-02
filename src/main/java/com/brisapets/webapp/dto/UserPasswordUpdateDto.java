package com.brisapets.webapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para editar apenas a Senha do utilizador.
 */
@Data
public class UserPasswordUpdateDto {

    @NotEmpty(message = "A senha atual é obrigatória.")
    private String currentPassword;

    @NotEmpty(message = "A nova senha é obrigatória.")
    @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres.")
    private String newPassword;

    @NotEmpty(message = "A confirmação da senha é obrigatória.")
    private String confirmPassword;

    // NOTA: A validação de 'newPassword' vs 'confirmPassword' será feita na camada de serviço/controlador
}