package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;
    private final PetService petService;
    private final AppointmentService appointmentService;

    // Injeção de dependências no construtor
    @Autowired
    public AdminController(UserService userService, PetService petService, AppointmentService appointmentService) {
        this.userService = userService;
        this.petService = petService;
        this.appointmentService = appointmentService;
    }

    /**
     * Rota do Painel de Administração.
     * Carrega todos os dados críticos do sistema para exibição.
     */
    @GetMapping("/admin")
    // A segurança já está definida em SecurityConfig, garantindo que apenas ADMIN acede.
    public String adminPanel(Model model) {
        // 1. Carregar todos os Utilizadores
        List<User> users = userService.findAllUsers();
        model.addAttribute("allUsers", users);

        // 2. Carregar todos os Pets
        List<Pet> pets = petService.findAllPets();
        model.addAttribute("allPets", pets);

        // 3. Carregar todos os Agendamentos
        List<Appointment> appointments = appointmentService.findAllAppointments();
        model.addAttribute("allAppointments", appointments);

        return "admin"; // Busca o template admin.html
    }

    @PostMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Chama o novo método de serviço para eliminação
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Utilizador com ID " + id + " removido com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao remover o utilizador: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}
