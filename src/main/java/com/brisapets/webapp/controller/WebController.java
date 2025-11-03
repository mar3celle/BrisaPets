package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserAddressUpdateDto;
import com.brisapets.webapp.dto.UserPasswordUpdateDto;
import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.ReportService;
import com.brisapets.webapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class WebController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ReportService reportService;

    public WebController(UserService userService, AppointmentService appointmentService, ReportService reportService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.reportService = reportService;
    }

    // Método de ajuda para obter o ID do utilizador logado
    private Long getLoggedInUserId() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(userEmail).getId();
        } catch (UsernameNotFoundException e) {
            throw new SecurityException("Utilizador autenticado não encontrado na base de dados.", e);
        }
    }

    // Mapeia a URL base (/) para o template 'index.html'
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

    @GetMapping("/reports")
    public String reports(Model model) {
        List<Appointment> allAppointments = appointmentService.findAllAppointments();
        
        model.addAttribute("appointments", allAppointments);
        model.addAttribute("totalRevenue", String.format("€ %.2f", reportService.calculateTotalRevenue()));
        model.addAttribute("totalServices", String.valueOf(reportService.getTotalServices()));
        model.addAttribute("avgTicket", String.format("€ %.2f", reportService.calculateAverageTicket()));
        model.addAttribute("newClients", "18"); // Mock data
        
        return "reports";
    }

    /**
     * Rota para a página de Perfil.
     * Além do User, injeta os DTOs vazios para os formulários de edição.
     */
    @GetMapping("/perfil")
    public String profile(Model model) {

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(userEmail);

            model.addAttribute("user", user);

            // Adiciona os DTOs para os formulários no perfil
            // Usamos os dados existentes do User para pré-preencher o form de perfil
            UserProfileUpdateDto profileDto = new UserProfileUpdateDto();
            profileDto.setFirstName(user.getFirstName());
            profileDto.setLastName(user.getLastName());
            profileDto.setPhone(user.getPhone());

            model.addAttribute("profileDto", profileDto);
            model.addAttribute("passwordDto", new UserPasswordUpdateDto());

            UserAddressUpdateDto addressDto = new UserAddressUpdateDto();
            addressDto.setAddress(user.getAddress());
            addressDto.setCity(user.getCity());
            addressDto.setZipCode(user.getZipCode());

            model.addAttribute("addressDto", addressDto);

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

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

    @PostMapping("/perfil/deletar")
    public String deleteProfile(RedirectAttributes redirectAttributes) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(userEmail);
            userService.deleteUserById(user.getId());
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar conta. Tente novamente.");
            return "redirect:/perfil";
        }
    }

    // =========================================================================
    // === NOVOS ENDPOINTS DE EDIÇÃO DO PERFIL ===
    // =========================================================================

    /**
     * 1. Processa a atualização do Nome, Sobrenome e Telefone.
     */
    @PostMapping("/perfil/edit/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") UserProfileUpdateDto profileDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDto", result);
            redirectAttributes.addFlashAttribute("profileDto", profileDto);
            redirectAttributes.addFlashAttribute("errorProfile", "Verifique os erros no formulário de Perfil.");
            return "redirect:/perfil";
        }

        try {
            Long userId = getLoggedInUserId();
            userService.updateProfile(userId, profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil. Tente novamente.");
        }

        return "redirect:/perfil";
    }

    /**
     * 2. Processa a atualização da Senha.
     */
    @PostMapping("/perfil/edit/password")
    public String updatePassword(
            @Valid @ModelAttribute("passwordDto") UserPasswordUpdateDto passwordDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", "Verifique os erros no formulário de Senha.");
            return "redirect:/perfil";
        }

        // Validação adicional de igualdade
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "A nova senha e a confirmação não coincidem.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", "A nova senha e a confirmação não coincidem.");
            return "redirect:/perfil";
        }

        try {
            Long userId = getLoggedInUserId();
            userService.updatePassword(userId, passwordDto);
            redirectAttributes.addFlashAttribute("successMessage", "Senha atualizada com sucesso! Por favor, faça login novamente.");

            // Força o logout após a alteração de senha por segurança
            return "redirect:/logout";

        } catch (IllegalArgumentException e) {
            // Captura erro de senha atual incorreta
            result.rejectValue("currentPassword", null, e.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", e.getMessage());
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao atualizar senha.");
            return "redirect:/perfil";
        }
    }

    /**
     * 3. Processa a atualização da Morada.
     */
    @PostMapping("/perfil/edit/address")
    public String updateAddress(
            @ModelAttribute("addressDto") UserAddressUpdateDto addressDto,
            RedirectAttributes redirectAttributes) {


        try {
            Long userId = getLoggedInUserId();
            userService.updateAddress(userId, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Morada atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar morada. Tente novamente.");
        }

        return "redirect:/perfil";
    }
}
