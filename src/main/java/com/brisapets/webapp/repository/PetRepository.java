package com.brisapets.webapp.repository;
import com.brisapets.webapp.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}