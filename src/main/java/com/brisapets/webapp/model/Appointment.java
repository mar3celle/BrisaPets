package com.brisapets.webapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private LocalDateTime appointmentDateTime;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "service_value", precision = 10, scale = 2)
    private BigDecimal value;

    private Boolean isPaid = false;

    public Appointment() {}
    
    // Business logic methods
    public boolean isUpcoming() {
        return appointmentDateTime.isAfter(LocalDateTime.now());
    }
    
    public boolean isPastDue() {
        return appointmentDateTime.isBefore(LocalDateTime.now()) && !isPaid;
    }
    
    public String getFormattedDateTime() {
        return appointmentDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}