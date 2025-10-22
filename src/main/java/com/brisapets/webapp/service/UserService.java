
package com.brisapets.webapp.service;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {
    // Para registo de novos utilizadores
    User save(UserRegistrationDto registrationDto);

    // Para obter o utilizador pelo email (username)
    User findByEmail(String email);

    // Implementação do Spring Security UserDetailsService
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    // Para carregar o utilizador pelo Spring Security
    // (Herdado de UserDetailsService)
}