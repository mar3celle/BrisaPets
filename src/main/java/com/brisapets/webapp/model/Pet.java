package com.brisapets.webapp.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String raca;
    private Integer idade;
    private Boolean castrado;
    private String imageUrl;
    private Long tutorId;

    // Inicializa a lista para evitar NPE (NullPointerException)
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>(); // Inicialização!
}