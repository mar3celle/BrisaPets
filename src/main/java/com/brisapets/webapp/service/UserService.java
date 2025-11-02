package com.brisapets.webapp.service;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.dto.UserPasswordUpdateDto;
import com.brisapets.webapp.dto.UserAddressUpdateDto;
import com.brisapets.webapp.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List; // NOVO: Import para findAllUsers
import java.util.Optional; // NOVO: Import para findUserById

public interface UserService extends UserDetailsService {
    // Para registo de novos utilizadores
    User save(UserRegistrationDto registrationDto);

    // Para obter o utilizador pelo email (username)
    User findByEmail(String email);

    // Implementação do Spring Security UserDetailsService
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // -----------------------------------------------------------------------
    // MÉTODOS DE ADMIN/GESTÃO
    // -----------------------------------------------------------------------

    // Método para deletar um utilizador pelo ID
    void deleteUserById(Long id);

    //Método para listar todos os utilizadores (útil para Admin)
    List<User> findAllUsers();

    // Método para buscar um utilizador pelo ID
    Optional<User> findUserById(Long id);

    // Profile update methods
    void updateProfile(Long userId, UserProfileUpdateDto profileDto);
    void updatePassword(Long userId, UserPasswordUpdateDto passwordDto);
    void updateAddress(Long userId, UserAddressUpdateDto addressDto);
}