package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Adicione esta importação

// REMOVIDA: A anotação @RequestMapping("/register")
@Controller
public class UserRegistrationController {

    private final UserService userService;

    public UserRegistrationController(UserService userService) {
        this.userService = userService;
    }

    // REMOVIDO: O método @GetMapping para /register.
    // O formulário de registo é mostrado através do WebController @GetMapping("/autenticar")

    // Método auxiliar para garantir que o DTO está no Model para o POST e em caso de erro
    @ModelAttribute("user")
    public UserRegistrationDto userRegistrationDto() {
        return new UserRegistrationDto();
    }


    // 2. Método para processar o registo (POST /register)
    @PostMapping("/register") // Adicione o mapping diretamente ao POST
    public String registerUserAccount(
            @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult result,
            RedirectAttributes redirectAttributes, // Usamos RedirectAttributes em vez de Model
            Model model)
    {
        // ---------------------------------------------------------------------
        // * VALIDAÇÕES CRÍTICAS *
        // ---------------------------------------------------------------------

        // 1. Verificação de Utilizador Existente
        try {
            User existing = userService.findByEmail(registrationDto.getEmail());
            // Se encontrar o utilizador, adiciona um erro ao BindingResult
            result.rejectValue("email", null, "Já existe uma conta registada com este email.");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // Se não encontrar, continua - isto é o comportamento esperado para um novo registo
        }

        // 2. Outras Validações
        if (result.hasErrors()) {
            // Se houver erros de validação, retorna para a página de login/registo
            // O objeto 'user' com os erros já estará no Model
            return "login";
        }

        // ---------------------------------------------------------------------
        // * SALVAR O NOVO UTILIZADOR *
        // ---------------------------------------------------------------------

        userService.save(registrationDto);

        // Adiciona uma mensagem de sucesso que será exibida após o redirecionamento
        redirectAttributes.addFlashAttribute("registrationSuccess", "Registo efetuado com sucesso! Pode agora fazer login.");

        // Redireciona para o GET /autenticar (Login), com um parâmetro de sucesso
        return "redirect:/autenticar?success";
    }
}