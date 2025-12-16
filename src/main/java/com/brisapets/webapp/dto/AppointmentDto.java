package com.brisapets.webapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

public record AppointmentDto(
    Long id,
    String serviceName,
    LocalDateTime appointmentDateTime,
    LocalDate startDate,
    LocalDate endDate,
    String observations,
    Long petId,
    String petName,
    BigDecimal value,
    Boolean isPaid,
    String status
) {
    
    public boolean isDateRangeService() {
        return "Pet Sitting".equals(serviceName) || "Hosting".equals(serviceName);
    }
    
    public String getFormattedDateTime() {
        if (appointmentDateTime == null) {
            return "Data não definida";
        }
        return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public String getFormattedDateRange() {
        if (startDate == null) {
            return "Data não definida";
        }
        if (endDate == null || startDate.equals(endDate)) {
            return startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
               " - " + endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}