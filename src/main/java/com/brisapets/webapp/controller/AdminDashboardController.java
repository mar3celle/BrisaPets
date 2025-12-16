package com.brisapets.webapp.controller;

import com.brisapets.webapp.mapper.UserMapper;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.AuthenticationService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserService userService;
    private final PetService petService;
    private final AppointmentService appointmentService;
    private final AuthenticationService authenticationService;

    public AdminDashboardController(UserService userService, PetService petService, 
                                  AppointmentService appointmentService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String adminPanel(Model model) {
        var users = userService.findAllUsers().stream()
            .map(UserMapper::toDto)
            .toList();
        model.addAttribute("allUsers", users);

        var pets = petService.findAllPets();
        model.addAttribute("allPets", pets);

        var appointments = appointmentService.findAllAppointments();
        model.addAttribute("allAppointments", appointments);
        
        model.addAttribute("currentPage", "admin");

        return "admin";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var currentUserId = authenticationService.getCurrentUserId();
            if (currentUserId.equals(id)) {
                redirectAttributes.addFlashAttribute("adminError", "Não pode deletar a sua própria conta.");
                return "redirect:/admin";
            }
            
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Utilizador com ID " + id + " removido com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao remover o utilizador: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}