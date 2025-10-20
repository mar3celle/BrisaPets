package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Apenas carrega o AppointmentController e o contexto MVC
@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // "Moca" o Service, ou seja, simula o comportamento sem tocar no banco de dados real
    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PetService petService;

    @Test
    void testShowBookingPage() throws Exception {
        // ARRANGE: Configura o PetService para retornar uma lista de Pets
        Long tutorId = 1L;
        Pet pet1 = new Pet();
        pet1.setId(10L);
        pet1.setNome("Rex");
        when(petService.findPetsByTutor(tutorId)).thenReturn(Arrays.asList(pet1));

        // ACT & ASSERT: Verifica se o GET para /agendar retorna a view correta
        mockMvc.perform(get("/agendar"))
                .andExpect(status().isOk()) // Espera status 200 OK
                .andExpect(view().name("booking")) // Espera a view 'booking.html'
                .andExpect(model().attributeExists("pets")) // Espera que a lista de pets esteja no Model
                .andExpect(model().attributeExists("appointment")); // Espera o objeto de Agendamento

        // Verifica se o PetService foi chamado
        verify(petService, times(1)).findPetsByTutor(tutorId);
    }

    @Test
    void testSaveAppointmentSuccess() throws Exception {
        // ARRANGE:
        // 1. Moca o PetService para retornar um Pet existente
        Pet existingPet = new Pet();
        existingPet.setId(10L);
        when(petService.findPetById(10L)).thenReturn(Optional.of(existingPet));

        // 2. Moca o AppointmentService para simular o salvamento
        when(appointmentService.saveAppointment(any())).thenAnswer(i -> i.getArguments()[0]);

        // ACT & ASSERT: Simula o POST do formulário
        mockMvc.perform(post("/agendar/save")
                        .param("serviceName", "Banho e Tosquia Intima")
                        .param("selectedDate", "2025-11-25")
                        .param("selectedTime", "15:30")
                        .param("petId", "10"))
                .andExpect(status().is3xxRedirection()) // Espera um redirecionamento (status 302/303)
                .andExpect(redirectedUrl("/pets")); // Espera o redirecionamento para /pets

        // Verifica se os serviços foram chamados
        verify(petService, times(1)).findPetById(10L);
        verify(appointmentService, times(1)).saveAppointment(any());
    }
}