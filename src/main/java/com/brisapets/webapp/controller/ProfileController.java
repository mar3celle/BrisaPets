package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserAddressUpdateDto;
import com.brisapets.webapp.dto.UserPasswordUpdateDto;
import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.service.AuthenticationService;
import com.brisapets.webapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ProfileController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public ProfileController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String profile(Model model) {
        try {
            var user = authenticationService.getCurrentUser();
            model.addAttribute("user", user);

            var profileDto = new UserProfileUpdateDto();
            profileDto.setFirstName(user.getFirstName());
            profileDto.setLastName(user.getLastName());
            profileDto.setPhone(user.getPhone());

            model.addAttribute("profileDto", profileDto);
            model.addAttribute("passwordDto", new UserPasswordUpdateDto());

            var addressDto = new UserAddressUpdateDto();
            addressDto.setAddress(user.getAddress());
            addressDto.setCity(user.getCity());
            addressDto.setZipCode(user.getZipCode());

            model.addAttribute("addressDto", addressDto);

        } catch (Exception e) {
            return "redirect:/logout";
        }

        return "profile";
    }

    @PostMapping("/edit/profile")
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
            var userId = authenticationService.getCurrentUserId();
            userService.updateProfile(userId, profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil. Tente novamente.");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/edit/password")
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

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "A nova senha e a confirmação não coincidem.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", "A nova senha e a confirmação não coincidem.");
            return "redirect:/perfil";
        }

        try {
            var userId = authenticationService.getCurrentUserId();
            userService.updatePassword(userId, passwordDto);
            redirectAttributes.addFlashAttribute("successMessage", "Senha atualizada com sucesso! Por favor, faça login novamente.");
            return "redirect:/logout";

        } catch (IllegalArgumentException e) {
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

    @PostMapping("/edit/address")
    public String updateAddress(
            @ModelAttribute("addressDto") UserAddressUpdateDto addressDto,
            RedirectAttributes redirectAttributes) {

        try {
            var userId = authenticationService.getCurrentUserId();
            userService.updateAddress(userId, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Morada atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar morada. Tente novamente.");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/deletar")
    public String deleteProfile(RedirectAttributes redirectAttributes) {
        try {
            var userId = authenticationService.getCurrentUserId();
            userService.deleteUserById(userId);
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar conta. Tente novamente.");
            return "redirect:/perfil";
        }
    }
}