package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserRegistrationDto; // Import CRÍTICO para o formulário de registo
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import CRÍTICO para passar dados ao template
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Controller responsável por mapear as URLs públicas para as páginas HTML.
 */
@Controller
public class WebController {

    // Mapeia a URL base (/) para o template 'index.html'
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Mapeia as URLs de login para o template 'login.html'.
     * CRÍTICO: Garante que o objeto 'user' (DTO) está no Model para o Thymeleaf.
     */
    @GetMapping({"/entrar", "/login", "/autenticar"})
    public String loginPage(Model model) {

        // Se o Model não contiver o objeto 'user' (DTO para o formulário de Registo), adicione-o.
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "login";
    }

    // Rota para a página de Hospedagem/Galeria. (gallery.html)
    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    /**
     * Rota para a página de Perfil.
     * Os dados são temporários (mockup) até à implementação completa do Spring Security.
     */
    @GetMapping("/perfil")
    public String profile(Model model) {

        // --- Dados Mockup Temporários (Substitua estes dados por dados reais de utilizador) ---
        model.addAttribute("userName", "Mestre Tutor");
        model.addAttribute("userFullName", "Brisa Pets - Sede");
        model.addAttribute("userEmail", "geral@brisapets.pt");
        model.addAttribute("userPhone", "+351 910 000 000");
        model.addAttribute("userRole", "CLIENTE");
        model.addAttribute("userAddressLine1", "Rua do Comércio, 10");
        model.addAttribute("userAddressLine2", "1200-000 Lisboa");

        return "profile"; // Assumindo que o template se chama 'profile.html'
    }

    // Rota para Sair/Logout - O Spring Security é que faz o trabalho, mas esta rota garante
    // que, se for chamada diretamente, redireciona para o login.
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }
}