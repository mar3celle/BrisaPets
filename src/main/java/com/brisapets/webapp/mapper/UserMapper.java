package com.brisapets.webapp.mapper;

import com.brisapets.webapp.dto.UserDto;
import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class UserMapper {
    
    public static UserDto toDto(User user) {
        if (user == null) return null;
        
        List<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .toList();
        
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getNif(),
            user.getAddress(),
            user.getCity(),
            user.getZipCode(),
            roleNames
        );
    }
}