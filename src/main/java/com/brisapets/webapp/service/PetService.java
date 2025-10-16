package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private final PetRepository petRepository;

    @Autowired
    // Injeção de dependência do PetRepository
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // Salvar ou atualizar um Pet
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    // Encontrar Pet por ID
    public Optional<Pet> findById(Long id) {
        return petRepository.findById(id);
    }

    // Listar todos os Pets
    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    // Deletar Pet por ID
    public void deleteById(Long id) {
        petRepository.deleteById(id);
    }
}