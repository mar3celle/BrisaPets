package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Importado para o POST
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importado para RedirectAttributes

/**
 * Controller responsável por mapear as URLs públicas para as páginas HTML.
 */
@Controller
public class WebController {

    // INJEÇÃO DE DEPENDÊNCIA DO UserService
    private final UserService userService;

    // Construtor para injeção
    public WebController(UserService userService) {
        this.userService = userService;
    }


    // Mapeia a URL base (/) para o template 'index.html'
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Mapeia as URLs de login para o template 'login.html'.
     */
    @GetMapping({"/entrar", "/login", "/autenticar"})
    public String loginPage(Model model) {

        // Garante que o objeto 'user' (DTO) está no Model para o Thymeleaf do Registo.
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
     * AGORA USA DADOS REAIS DO UTILIZADOR LOGADO!
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
            // Caso raro: utilizador autenticado, mas não encontrado no DB. Forçar o logout.
            System.err.println("Erro de segurança: Utilizador autenticado não encontrado no DB.");
            return "redirect:/logout";
        } catch (Exception e) {
            // Captura qualquer outro erro e força o logout para segurança
            System.err.println("Erro ao carregar perfil: " + e.getMessage());
            return "redirect:/logout";
        }

        return "profile";
    }

    /**
     * NOVA ROTA: Processa a deleção da conta do utilizador logado.
     *
     * IMPORTANTE: Esta operação é crítica e resulta no logout e exclusão de todos os dados.
     */
    @PostMapping("/perfil/deletar")
    public String deleteProfile(RedirectAttributes redirectAttributes) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // 1. Buscar o User ID pelo email
            User user = userService.findByEmail(userEmail);
            Long userId = user.getId();

            // 2. Deletar o utilizador (incluindo Pets e Agendamentos em cascata)
            userService.deleteUserById(userId);

            // 3. O logout é ESSENCIAL após a deleção.
            return "redirect:/logout";

        } catch (UsernameNotFoundException e) {
            // O utilizador já não existe, apenas redireciona para logout.
            return "redirect:/logout";
        } catch (Exception e) {
            System.err.println("Erro ao deletar perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar a conta. Por favor, tente novamente.");
            return "redirect:/perfil"; // Redireciona de volta para a página de perfil com erro
        }
    }

    @GetMapping("/agendar")
    public String showBookingPage() {
        return "booking";
    }

    // Rota para Sair/Logout
    @GetMapping("/logout")
    public String logout() {
        // Redireciona o Spring Security para a rota de logout para limpar a sessão
        return "redirect:/entrar";
    }
    @GetMapping("/admin")
    public String adminPanel() {
        return "admin";
    }
}