package com.brisapets.webapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private LocalDateTime appointmentDateTime;
    
    // New fields for date range services
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Column(columnDefinition = "TEXT")
    private String observations;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "service_value", precision = 10, scale = 2)
    private BigDecimal value;

    private Boolean isPaid = false;
    
    @Column(name = "status")
    private String status = "Pending"; // Pending, Confirmed, Canceled

    public Appointment() {}
    
    // Business logic methods
    public boolean isUpcoming() {
        return appointmentDateTime.isAfter(LocalDateTime.now());
    }
    
    public boolean isPastDue() {
        return appointmentDateTime.isBefore(LocalDateTime.now()) && !isPaid;
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
    
    public boolean isDateRangeService() {
        return "Pet Sitting".equals(serviceName) || "Hosting".equals(serviceName);
    }
    
    public boolean isPending() {
        return "Pending".equals(status);
    }
    
    public boolean isConfirmed() {
        return "Confirmed".equals(status);
    }
}