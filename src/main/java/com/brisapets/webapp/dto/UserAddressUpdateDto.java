package com.brisapets.webapp.dto;

import lombok.Data;

/**
 * DTO para editar os campos de Morada do utilizador.
 */
@Data
public class UserAddressUpdateDto {

    private String address;
    private String city;
    private String zipCode;
}
