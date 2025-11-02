package com.brisapets.webapp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * DTO para editar o Nome, Sobrenome e Telefone do utilizador.
 */
@Data
public class UserProfileUpdateDto {

    @NotEmpty(message = "O primeiro nome é obrigatório.")
    private String firstName;

    @NotEmpty(message = "O último nome é obrigatório.")
    private String lastName;

    private String phone; // Pode ser opcionalmente validado com @Pattern
}