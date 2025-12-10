package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    @Query("SELECT p FROM Pet p WHERE p.tutorId = ?1 AND p.deleted = false")
    List<Pet> findByTutorId(Long tutorId);
    
    @Query("SELECT p FROM Pet p WHERE p.deleted = false")
    List<Pet> findAllActive();
    
    @Query("SELECT p FROM Pet p WHERE p.id = ?1 AND p.deleted = false")
    Optional<Pet> findActiveById(Long id);
}