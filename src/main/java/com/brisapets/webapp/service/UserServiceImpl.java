package com.brisapets.webapp.service;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.dto.UserPasswordUpdateDto;
import com.brisapets.webapp.dto.UserAddressUpdateDto;
import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.repository.RoleRepository;
import com.brisapets.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional; // Import para Optional
import java.util.stream.Collectors;

/**
 * Implementação do serviço de utilizador, estendendo a lógica de segurança (UserDetailsService).
 */
@Service
public class UserServiceImpl implements UserService {

    // E-mail do administrador para atribuição da função ROLE_ADMIN.
    private static final String HARDCODED_ADMIN_EMAIL = "marcellesart@gmail.com";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Construtor com TODAS as injeções de dependência
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Salva um novo utilizador, atribuindo as Roles corretas (ADMIN se for o e-mail hardcoded).
     */
    @Override
    public User save(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        // 1. Encontra ou cria a ROLE_CLIENTE (Role padrão)
        Role roleCliente = roleRepository.findByName("ROLE_CLIENTE")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CLIENTE")));

        List<Role> roles = new java.util.ArrayList<>(Collections.singletonList(roleCliente));

        // 2. LÓGICA DE ADMIN: Se o e-mail for o do administrador, adiciona a ROLE_ADMIN
        if (HARDCODED_ADMIN_EMAIL.equalsIgnoreCase(registrationDto.getEmail())) {
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            roles.add(roleAdmin);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Busca o utilizador completo (com todas as Roles) na base de dados pelo email.
     */
    @Override
    public User findByEmail(String email) {
        // First try to find active user, then try including deleted ones for OAuth2
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByEmailIncludingDeleted(email))
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado com o email: " + email));
    }


    /**
     * Implementação do Spring Security para carregar o utilizador e as suas autoridades.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Usa o método findByEmail que retorna a entidade completa
        User user = findByEmail(username);

        // Mapeia as Roles de JPA para as GrantedAuthorities do Spring
        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());

        // Retorna o objeto User de segurança do Spring
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    /**
     * Método auxiliar para mapear Roles em GrantedAuthorities.
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                // Converte o nome da Role (ex: "ROLE_ADMIN") para um SimpleGrantedAuthority
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    // --------------------------------------------------------------------------------------------------
    // ⭐ MÉTODOS DE ADMIN/GESTÃO (IMPLEMENTAÇÕES QUE ESTAVAM FALTANDO)
    // --------------------------------------------------------------------------------------------------

    /**
     *  Implementa o método para listar todos os utilizadores ativos.
     */
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllActive();
    }

    /**
     *
     * Busca um utilizador pelo ID, retornando um Optional.
     */
    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     ** método para soft delete de um utilizador pelo seu ID.
     */
    @Override
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.softDelete();
        userRepository.save(user);
    }

    @Override
    public void updateProfile(Long userId, UserProfileUpdateDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setPhone(profileDto.getPhone());
        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, UserPasswordUpdateDto passwordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateAddress(Long userId, UserAddressUpdateDto addressDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAddress(addressDto.getAddress());
        user.setCity(addressDto.getCity());
        user.setZipCode(addressDto.getZipCode());
        userRepository.save(user);
    }
}
