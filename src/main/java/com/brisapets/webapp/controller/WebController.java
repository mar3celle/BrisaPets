package com.brisapets.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Necessário para passar dados ao template
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

    // Mapeia a URL base (/) para o template 'index.html'
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Mapeia a URL /entrar e /login para o template 'login.html'
    @GetMapping({"/entrar", "/login"})
    public String loginPage() {
        return "login";
    }

    // Rota para a página de Hospedagem/Galeria. (gallery.html)
    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    // Rota opcional para o perfil
    @GetMapping("/perfil")
    public String profile(Model model) { // Adicione 'Model model' como argumento

        // --- Dados Mockup Temporários (Serão substituídos pelo Spring Security) ---
        model.addAttribute("userName", "Mestre Tutor");
        model.addAttribute("userFullName", "Brisa Pets - Sede");
        model.addAttribute("userEmail", "geral@brisapets.pt");
        model.addAttribute("userPhone", "+351 910 000 000");
        model.addAttribute("userRole", "CLIENTE");
        model.addAttribute("userAddressLine1", "Rua do Comércio, 10");
        model.addAttribute("userAddressLine2", "1200-000 Lisboa");

        return "profile";
    }

    // Rota opcional para Sair/Logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }
}