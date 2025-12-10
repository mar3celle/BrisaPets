package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.repository.RoleRepository;
import com.brisapets.webapp.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        
        // Check if user exists, if not create new user
        User user = userRepository.findByEmailIncludingDeleted(email)
                .orElseGet(() -> createNewUser(email, firstName, lastName));
        
        // If user was soft deleted, restore them
        if (user.getDeleted()) {
            user.restore();
            userRepository.save(user);
        }
        
        return oauth2User;
    }
    
    private User createNewUser(String email, String firstName, String lastName) {
        System.out.println("Creating new OAuth2 user: " + email);
        
        Role clientRole = roleRepository.findByName("ROLE_CLIENTE")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CLIENTE")));
        
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword(""); // OAuth users don't need password
        newUser.setRoles(Collections.singletonList(clientRole));
        
        User savedUser = userRepository.save(newUser);
        System.out.println("OAuth2 user created with ID: " + savedUser.getId());
        
        return savedUser;
    }
}