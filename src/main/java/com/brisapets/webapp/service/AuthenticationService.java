package com.brisapets.webapp.service;

import com.brisapets.webapp.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserService userService;

    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser() {
        var userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(userEmail);
    }

    public Long getCurrentUserId() {
        try {
            return getCurrentUser().getId();
        } catch (UsernameNotFoundException e) {
            throw new SecurityException("Utilizador autenticado não encontrado na base de dados.", e);
        }
    }

    public boolean isCurrentUserAdmin() {
        try {
            var user = getCurrentUser();
            return user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        } catch (Exception e) {
            return false;
        }
    }
}