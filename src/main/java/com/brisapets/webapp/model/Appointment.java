package com.brisapets.webapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data // Gera Getters, Setters, etc.
@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do serviço
    private String serviceName;

    // A data e hora exata do agendamento
    private LocalDateTime appointmentDateTime;

    // Referência ao Pet que está a ser agendado
    // Mapeia o relacionamento de muitos agendamentos para um Pet
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // Construtor vazio padrão é importante para JPA
    public Appointment() {}
}