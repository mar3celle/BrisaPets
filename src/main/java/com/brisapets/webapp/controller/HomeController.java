package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserRegistrationDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping({"/entrar", "/login", "/autenticar"})
    public String loginPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "login";
    }

    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }
}