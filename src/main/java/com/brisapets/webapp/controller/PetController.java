package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class PetController {

    private final PetService petService;
    private final UserService userService; // NOVO: Campo para o UserService

    // Construtor com as DUAS injeções de dependência
    @Autowired
    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService; // NOVO: Atribuição
    }

    /**
     * NOVO MÉTODO DE AJUDA: Obtém o ID do utilizador logado usando o email
     */
    private Long getLoggedInUserId() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(userEmail).getId();
        } catch (Exception e) {
            throw new SecurityException("Utilizador autenticado não encontrado na base de dados.", e);
        }
    }


    // 1. Mapeamento para exibir a página "Meus Pets"
    @GetMapping("/pets")
    public String listPets(Model model) {

        // --- Lógica de Segurança Corrigida ---
        Long tutorId = getLoggedInUserId(); // <-- CHAMA O MÉTODO CORRIGIDO

        List<Pet> pets = petService.findPetsByTutor(tutorId); // Usa o ID real
        model.addAttribute("pets", pets);
        model.addAttribute("pet", new Pet());

        return "pets";
    }

    // 2. Mapeamento para receber e salvar um novo Pet
    @PostMapping("/pets/save")
    public String savePet(@ModelAttribute("pet") Pet pet, RedirectAttributes redirectAttributes) {

        // --- Lógica de Segurança Corrigida ---
        pet.setTutorId(getLoggedInUserId()); // <-- USA O ID REAL

        petService.savePet(pet);

        redirectAttributes.addFlashAttribute("message", "Pet salvo com sucesso!");

        return "redirect:/pets";
    }

    // 3. Mapeamento para deletar um Pet
    @PostMapping("/pets/delete/{id}")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // SECURITY: Verify pet ownership before deletion
            Optional<Pet> petOpt = petService.findPetById(id);
            if (petOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pet não encontrado.");
                return "redirect:/pets";
            }
            
            Pet pet = petOpt.get();
            Long currentUserId = getLoggedInUserId();
            if (pet.getTutorId() == null || !pet.getTutorId().equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "Não tem permissão para deletar este Pet.");
                return "redirect:/pets";
            }
            
            petService.deletePet(id);
            redirectAttributes.addFlashAttribute("message", "Pet deletado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar o Pet.");
        }

        return "redirect:/pets";
    }
}