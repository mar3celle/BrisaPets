package com.brisapets.webapp.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "pets", indexes = {
    @Index(name = "idx_tutor_id", columnList = "tutorId"),
    @Index(name = "idx_tutor_deleted", columnList = "tutorId, deleted")
})
public class Pet extends BaseEntity {



    private String nome;
    private String raca;
    private Integer idade;
    private Boolean castrado;
    private String imageUrl;
    private Long tutorId;

    // Inicializa a lista para evitar NPE (NullPointerException)
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 20)
    private List<Appointment> appointments = new ArrayList<>(); // Inicialização!
}