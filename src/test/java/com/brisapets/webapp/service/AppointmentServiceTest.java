package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    // Injetamos o PetRepository para garantir que temos um Pet para agendar
    @Autowired
    private PetRepository petRepository;

    @Test
    void testSaveAndFindAllAppointments() {
        // ARRANGE:
        // 1. Criar e salvar um Pet (chave estrangeira obrigatória)
        Pet pet = new Pet();
        pet.setNome("Bolinha");
        pet.setRaca("Poodle");
        pet.setIdade(5);
        pet.setCastrado(true);
        pet.setTutorId(1L);
        Pet savedPet = petRepository.save(pet);

        // 2. Criar o Agendamento
        Appointment newAppointment = new Appointment();
        newAppointment.setServiceName("Banho e Tosquia Geral");
        newAppointment.setAppointmentDateTime(LocalDateTime.of(2025, 11, 20, 14, 0));
        newAppointment.setPet(savedPet);
        newAppointment.setValue(new java.math.BigDecimal("50.00"));
        newAppointment.setIsPaid(false);

        // ACT: Salvar o Agendamento
        Appointment savedAppointment = appointmentService.saveAppointment(newAppointment);

        // ASSERT (Salvar):
        assertNotNull(savedAppointment.getId(), "O ID do Agendamento não deve ser nulo após salvar.");
        assertEquals("Bolinha", savedAppointment.getPet().getNome(), "O Pet associado deve ser 'Bolinha'.");

        // ACT & ASSERT (Find All):
        List<Appointment> allAppointments = appointmentService.findAllAppointments();
        assertFalse(allAppointments.isEmpty(), "A lista de agendamentos não deve estar vazia.");
        assertEquals(1, allAppointments.size(), "Deve haver exatamente 1 agendamento na lista.");
    }
}