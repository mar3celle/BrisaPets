package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ClientListController {

    private final UserService userService;
    private final PetService petService;

    public ClientListController(UserService userService, PetService petService) {
        this.userService = userService;
        this.petService = petService;
    }

    @GetMapping("/clientlist")
    @PreAuthorize("hasRole('ADMIN')")
    public String showClientList(Model model) {
        List<User> allUsers = userService.findAllUsers();
        
        // Create a map with pet counts for each user
        Map<Long, Integer> petCounts = allUsers.stream()
            .collect(Collectors.toMap(
                User::getId,
                user -> petService.findPetsByTutor(user.getId()).size()
            ));
        
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("petCounts", petCounts);
        model.addAttribute("currentPage", "clientlist");
        
        return "clientList";
    }
}