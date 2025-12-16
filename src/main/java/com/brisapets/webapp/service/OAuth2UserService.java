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
public final class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var oauth2User = super.loadUser(userRequest);
        
        var email = oauth2User.<String>getAttribute("email");
        var firstName = oauth2User.<String>getAttribute("given_name");
        var lastName = oauth2User.<String>getAttribute("family_name");
        
        var user = findOrCreateUser(email, firstName, lastName);
        
        if (user.getDeleted()) {
            user.restore();
            userRepository.save(user);
        }
        
        return oauth2User;
    }
    
    private User findOrCreateUser(String email, String firstName, String lastName) {
        return userRepository.findByEmailIncludingDeleted(email)
                .orElseGet(() -> createNewOAuth2User(email, firstName, lastName));
    }
    
    private User createNewOAuth2User(String email, String firstName, String lastName) {
        var clientRole = roleRepository.findByName("ROLE_CLIENTE")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CLIENTE")));
        
        var newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPassword(""); // OAuth users don't need password
        newUser.setRoles(Collections.singletonList(clientRole));
        
        return userRepository.save(newUser);
    }
}