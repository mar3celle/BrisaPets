package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Role;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.repository.RoleRepository;
import com.brisapets.webapp.repository.UserRepository;
import com.brisapets.webapp.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DebugController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public DebugController(UserService userService, UserRepository userRepository, RoleRepository roleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/debug/user")
    @ResponseBody
    public String debugCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(email);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Email: ").append(user.getEmail()).append("<br>");
            sb.append("Roles: ");
            user.getRoles().forEach(role -> sb.append(role.getName()).append(" "));
            
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/debug/make-admin")
    @ResponseBody
    public String makeCurrentUserAdmin() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(email);
            
            // Find or create ROLE_ADMIN
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
            
            // Add ADMIN role if not already present
            if (!user.getRoles().contains(adminRole)) {
                user.getRoles().add(adminRole);
                userRepository.save(user);
                return "ADMIN role added to " + email + ". Please logout and login again.";
            } else {
                return "User " + email + " already has ADMIN role.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}