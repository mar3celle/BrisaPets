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
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // 1. Método para salvar ou atualizar um Pet
    public Pet savePet(Pet pet) {
        return petRepository.save(pet);
    }

    // 2. Método para buscar todos os Pets ativos (uso interno, ou para Admin)
    public List<Pet> findAllPets() {
        return petRepository.findAllActive();
    }

    // 3. Método para buscar um Pet pelo ID
    public Optional<Pet> findPetById(Long id) {
        return petRepository.findById(id);
    }

    // 4. Método para soft delete de um Pet pelo ID
    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet not found"));
        pet.softDelete();
        petRepository.save(pet);
    }

    /**
     * ✅ MÉTODO CORRIGIDO E FINAL:
     * Usa o método findByTutorId(tutorId) declarado no PetRepository
     * para devolver APENAS os pets pertencentes ao utilizador com o ID fornecido.
     */
    public List<Pet> findPetsByTutor(Long tutorId) {
        return petRepository.findByTutorId(tutorId);
    }
}