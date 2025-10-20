package com.brisapets.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável por mapear as URLs para as páginas HTML (views/templates).
 */
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

    /* Mapeia a URL /pets para o template 'pets.html'
    @GetMapping("/pets")
    public String myPets() {
        return "pets";
    } */

    // Rota para a página de Agendamento. (booking.html)
    @GetMapping("/agendar")
    public String schedule() {
        return "booking";
    }

    // Rota para a página de Hospedagem/Galeria. (gallery.html)
    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    // Rota opcional para o perfil
    @GetMapping("/perfil")
    public String profile() {
        // Redireciona temporariamente, até a criação da página 'perfil.html'
        return "redirect:/";
    }

    // Rota opcional para Sair/Logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }
}