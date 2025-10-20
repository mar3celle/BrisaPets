package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByTutorId(Long tutorId);
}