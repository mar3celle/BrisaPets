package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Controller
public class WebController {

    private final UserService userService;

    // CONSTRUTOR COM INJEÇÃO DE DEPENDÊNCIA
    public WebController(UserService userService) {
        this.userService = userService;
    }

    // Mapeia a URL base (/) para o template 'index.html'
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping({"/entrar", "/login", "/autenticar"})
    public String loginPage(Model model) {
        // Garante que o objeto 'user' (DTO) está no Model para o Thymeleaf do Registo.
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "login";
    }

    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    /**
     * Rota para a página de Perfil.
     * Usa o Spring Security para buscar os dados reais do utilizador.
     */
    @GetMapping("/perfil")
    public String profile(Model model) {

        try {
            // 1. Obter o email (username) do utilizador logado do contexto de segurança
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Buscar o objeto User completo na base de dados
            User user = userService.findByEmail(userEmail);

            // 3. Passar o objeto User (entity) para o template com a chave "user"
            model.addAttribute("user", user);

        } catch (UsernameNotFoundException e) {
            System.err.println("Erro de segurança: Utilizador autenticado não encontrado no DB.");
            return "redirect:/logout";
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar perfil: " + e.getMessage());
            return "redirect:/logout";
        }

        return "profile"; // Busca o template profile.html
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }

    // Endpoint para a página de Acesso Negado (403 Forbidden)
    @GetMapping("/403")
    public String accessDenied() {
        return "403"; // Assumindo que tem um ficheiro 403.html
    }
}
