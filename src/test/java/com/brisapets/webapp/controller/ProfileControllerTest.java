package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.AuthenticationService;
import com.brisapets.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser
    void shouldShowProfilePage() throws Exception {
        var user = new User();
        user.setFirstName("João");
        user.setLastName("Silva");
        user.setEmail("joao@example.com");
        
        when(authenticationService.getCurrentUser()).thenReturn(user);
        
        mockMvc.perform(get("/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "profileDto", "passwordDto", "addressDto"));
    }

    @Test
    @WithMockUser
    void shouldUpdateProfile() throws Exception {
        when(authenticationService.getCurrentUserId()).thenReturn(1L);
        
        mockMvc.perform(post("/perfil/edit/profile")
                .with(csrf())
                .param("firstName", "João")
                .param("lastName", "Silva")
                .param("phone", "123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));
        
        verify(userService).updateProfile(eq(1L), any(UserProfileUpdateDto.class));
    }

    @Test
    @WithMockUser
    void shouldDeleteProfile() throws Exception {
        when(authenticationService.getCurrentUserId()).thenReturn(1L);
        
        mockMvc.perform(post("/perfil/deletar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"));
        
        verify(userService).deleteUserById(1L);
    }

    @Test
    void shouldRedirectToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection());
    }
}