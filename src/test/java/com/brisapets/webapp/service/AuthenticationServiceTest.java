package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldGetCurrentUser() {
        var user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        
        var result = authenticationService.getCurrentUser();
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void shouldGetCurrentUserId() {
        var user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        
        var result = authenticationService.getCurrentUserId();
        
        assertEquals(1L, result);
    }

    @Test
    void shouldCheckIfCurrentUserIsAdmin() {
        var adminRole = new Role("ROLE_ADMIN");
        var user = new User();
        user.setRoles(List.of(adminRole));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);
        
        assertTrue(authenticationService.isCurrentUserAdmin());
    }

    @Test
    void shouldReturnFalseForNonAdminUser() {
        var clientRole = new Role("ROLE_CLIENTE");
        var user = new User();
        user.setRoles(List.of(clientRole));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("client@example.com");
        when(userService.findByEmail("client@example.com")).thenReturn(user);
        
        assertFalse(authenticationService.isCurrentUserAdmin());
    }
}