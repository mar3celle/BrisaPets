
package com.brisapets.webapp.service;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Injeção de dependências no construtor
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para salvar um novo utilizador a partir do formulário de registo
    @Override
    public User save(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        // CRÍTICO: Codificar a senha antes de salvar!
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        // Novo utilizador é sempre CLIENTE por padrão
        user.setRoles(List.of(new Role("ROLE_CLIENTE")));

        return userRepository.save(user);
    }

    // Método de negócio para encontrar por email
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado com o email: " + email));
    }

    // Implementação do Spring Security UserDetailsService
    // Este método é usado pelo Spring Security durante o processo de LOGIN
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nome de utilizador ou senha inválidos."));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // A senha já encriptada do BD
                mapRolesToAuthorities(user.getRoles()) // As permissões
        );
    }

    // Mapeia os nossos objetos Role para os objetos de Autoridade do Spring Security
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}