package com.brisapets.webapp.dto;

import java.util.List;

public record UserDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phone,
    String nif,
    String address,
    String city,
    String zipCode,
    List<String> roles
) {
    
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    
    public String getFullAddress() {
        if (address == null) return null;
        var fullAddress = new StringBuilder(address);
        if (city != null) fullAddress.append(", ").append(city);
        if (zipCode != null) fullAddress.append(" ").append(zipCode);
        return fullAddress.toString();
    }
}