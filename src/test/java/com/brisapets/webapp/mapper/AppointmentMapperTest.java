package com.brisapets.webapp.mapper;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentMapperTest {

    @Test
    void shouldMapAppointmentToDto() {
        var pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setServiceName("Banho");
        appointment.setAppointmentDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        appointment.setValue(BigDecimal.valueOf(25.0));
        appointment.setStatus("Confirmed");
        appointment.setIsPaid(false);
        appointment.setPet(pet);
        
        var dto = AppointmentMapper.toDto(appointment);
        
        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Banho", dto.serviceName());
        assertEquals("Rex", dto.petName());
        assertEquals(BigDecimal.valueOf(25.0), dto.value());
        assertEquals("Confirmed", dto.status());
        assertFalse(dto.isPaid());
    }

    @Test
    void shouldMapAppointmentToSummaryDto() {
        var pet = new Pet();
        pet.setNome("Rex");
        
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setServiceName("Banho");
        appointment.setAppointmentDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        appointment.setValue(BigDecimal.valueOf(25.0));
        appointment.setStatus("Confirmed");
        appointment.setIsPaid(false);
        appointment.setPet(pet);
        
        var summaryDto = AppointmentMapper.toSummaryDto(appointment);
        
        assertNotNull(summaryDto);
        assertEquals(1L, summaryDto.id());
        assertEquals("Rex", summaryDto.petName());
        assertEquals("Banho", summaryDto.serviceName());
        assertEquals("15/01/2024 10:00", summaryDto.formattedDate());
    }

    @Test
    void shouldHandleNullAppointment() {
        assertNull(AppointmentMapper.toDto(null));
        assertNull(AppointmentMapper.toSummaryDto(null));
    }
}