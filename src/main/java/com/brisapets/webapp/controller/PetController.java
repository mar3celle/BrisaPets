package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        // Aqui, obter o ID do tutor logado
        Long tutorId = 1L; // Usando um ID fixo temporário

        // Busca a lista de pets pertencentes a este tutor
        List<Pet> pets = petService.findPetsByTutor(tutorId);

        // Adiciona a lista de pets e um objeto Pet vazio (para o formulário de adição) ao modelo
        model.addAttribute("pets", pets);
        model.addAttribute("pet", new Pet());

        return "pets"; // Retorna o template pets.html
    }

    // 2. Mapeamento para receber e salvar um novo Pet
    // URL: POST para /pets/save
    @PostMapping("/pets/save")
    public String savePet(@ModelAttribute("pet") Pet pet, RedirectAttributes redirectAttributes) {

        // --- Lógica de Segurança Temporária ---
        // Atribui o ID do tutor antes de salvar
        //pet.setTutorId(1L); // ID fixo para o pet pertencer a alguém

        // Salva o objeto Pet no banco de dados
        petService.savePet(pet);

        // Mensagem de feedback (opcional)
        redirectAttributes.addFlashAttribute("message", "Pet salvo com sucesso!");

        // Redireciona de volta para a lista de pets (para evitar o reenvio do formulário)
        return "redirect:/pets";
    }

    // TODO: Mapeamento para deletar um Pet (ex: @GetMapping("/pets/delete/{id}"))
}
