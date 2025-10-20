package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PetController {

    private final PetService petService;

    // Injeção de dependência do PetService
    @Autowired
    public PetController(PetService petService) {
        this.petService = petService;
    }

    // 1. Mapeamento para exibir a página "Meus Pets"
    @GetMapping("/pets")
    public String listPets(Model model) {

        // --- Lógica de Segurança Temporária ---
        Long tutorId = 1L; // Usando um ID fixo temporário

        List<Pet> pets = petService.findPetsByTutor(tutorId);

        model.addAttribute("pets", pets);
        // Garante que o objeto para o formulário se chama "pet"
        model.addAttribute("pet", new Pet());

        return "pets"; // Retorna o template pets.html
    }

    // 2. Mapeamento para receber e salvar um novo Pet
    @PostMapping("/pets/save")
    public String savePet(@ModelAttribute("pet") Pet pet, RedirectAttributes redirectAttributes) {

        // --- Lógica de Segurança Temporária: Atribui o ID do tutor antes de salvar ---
        pet.setTutorId(1L);

        petService.savePet(pet);

        redirectAttributes.addFlashAttribute("message", "Pet salvo com sucesso!");

        return "redirect:/pets";
    }

    //  Mapeamento para deletar um Pet
    // Usamos um @PathVariable para capturar o ID do Pet na URL
    @PostMapping("/pets/delete/{id}")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        // --- Lógica de Segurança Temporária ---

        try {
            petService.deletePet(id);
            redirectAttributes.addFlashAttribute("message", "Pet deletado com sucesso!");
        } catch (Exception e) {
            // Em caso de erro (ex: Pet não encontrado)
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar o Pet.");
        }

        return "redirect:/pets";
    }
}