package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller RESTful para a Entidade Pet.
 * Expõe a API em /api/pets
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    @Autowired
    public PetController(PetService petService) {
        this.petService = petService;
    }

    // POST: /api/pets - Criar novo Pet
    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        Pet savedPet = petService.save(pet);
        return new ResponseEntity<>(savedPet, HttpStatus.CREATED);
    }

    // GET: /api/pets - Listar todos os Pets
    @GetMapping
    public List<Pet> getAllPets() {
        return petService.findAll();
    }

    // ... (Métodos PUT e DELETE são os mesmos do código anterior) ...
    // Para simplificar, focamos nos métodos de leitura e criação que são essenciais para o frontend.
}