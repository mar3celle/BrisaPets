package com.brisapets.webapp.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDtoTest {

    @Test
    void shouldIdentifyDateRangeService() {
        var petSittingDto = new AppointmentDto(
            1L, "Pet Sitting", null, null, null, null, 1L, "Rex", 
            BigDecimal.valueOf(30), false, "Confirmed"
        );
        
        var hostingDto = new AppointmentDto(
            2L, "Hosting", null, null, null, null, 1L, "Rex", 
            BigDecimal.valueOf(40), false, "Confirmed"
        );
        
        var banhoDto = new AppointmentDto(
            3L, "Banho", null, null, null, null, 1L, "Rex", 
            BigDecimal.valueOf(25), false, "Confirmed"
        );
        
        assertTrue(petSittingDto.isDateRangeService());
        assertTrue(hostingDto.isDateRangeService());
        assertFalse(banhoDto.isDateRangeService());
    }

    @Test
    void shouldFormatDateTime() {
        var dto = new AppointmentDto(
            1L, "Banho", LocalDateTime.of(2024, 1, 15, 10, 30), 
            null, null, null, 1L, "Rex", BigDecimal.valueOf(25), false, "Confirmed"
        );
        
        assertEquals("15/01/2024 10:30", dto.getFormattedDateTime());
    }

    @Test
    void shouldFormatDateRange() {
        var singleDayDto = new AppointmentDto(
            1L, "Pet Sitting", null, LocalDate.of(2024, 1, 15), 
            LocalDate.of(2024, 1, 15), null, 1L, "Rex", 
            BigDecimal.valueOf(30), false, "Confirmed"
        );
        
        var multiDayDto = new AppointmentDto(
            2L, "Hosting", null, LocalDate.of(2024, 1, 15), 
            LocalDate.of(2024, 1, 17), null, 1L, "Rex", 
            BigDecimal.valueOf(120), false, "Confirmed"
        );
        
        assertEquals("15/01/2024", singleDayDto.getFormattedDateRange());
        assertEquals("15/01/2024 - 17/01/2024", multiDayDto.getFormattedDateRange());
    }

    @Test
    void shouldHandleNullDates() {
        var dto = new AppointmentDto(
            1L, "Banho", null, null, null, null, 1L, "Rex", 
            BigDecimal.valueOf(25), false, "Confirmed"
        );
        
        assertEquals("Data não definida", dto.getFormattedDateTime());
        assertEquals("Data não definida", dto.getFormattedDateRange());
    }
}