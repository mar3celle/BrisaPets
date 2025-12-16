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
public final class UserServiceImpl implements UserService {

    private static final String ADMIN_EMAIL = "marcellesart@gmail.com";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User save(UserRegistrationDto registrationDto) {
        System.out.println("[DEBUG] Iniciando salvamento do utilizador: " + registrationDto.getEmail());
        
        var user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setDeleted(false);

        System.out.println("[DEBUG] Utilizador criado, procurando role ROLE_CLIENTE");
        
        var roleCliente = roleRepository.findByName("ROLE_CLIENTE")
                .orElseGet(() -> {
                    System.out.println("[DEBUG] Role ROLE_CLIENTE não encontrada, criando nova");
                    return roleRepository.save(new Role("ROLE_CLIENTE"));
                });

        var roles = new java.util.ArrayList<>(Collections.singletonList(roleCliente));

        if (ADMIN_EMAIL.equalsIgnoreCase(registrationDto.getEmail())) {
            System.out.println("[DEBUG] Email é admin, adicionando role ROLE_ADMIN");
            var roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
            roles.add(roleAdmin);
        }

        user.setRoles(roles);
        
        System.out.println("[DEBUG] Salvando utilizador na base de dados");
        User savedUser = userRepository.save(user);
        System.out.println("[DEBUG] Utilizador salvo com ID: " + savedUser.getId());
        
        return savedUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado com o email: " + email));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = findByEmail(username);
        var authorities = mapRolesToAuthorities(user.getRoles());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAllActive();
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUserById(Long id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.softDelete();
        userRepository.save(user);
    }

    @Override
    public void updateProfile(Long userId, UserProfileUpdateDto profileDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setPhone(profileDto.getPhone());
        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, UserPasswordUpdateDto passwordDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateAddress(Long userId, UserAddressUpdateDto addressDto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAddress(addressDto.getAddress());
        user.setCity(addressDto.getCity());
        user.setZipCode(addressDto.getZipCode());
        userRepository.save(user);
    }
}
