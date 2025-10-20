package com.brisapets.webapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data // Gera Getters, Setters, toString, equals e hashCode (Lombok)
@Entity // Define esta classe como uma entidade JPA (tabela no banco)
@Table(name = "pets") // Nome da tabela no PostgreSQL
public class Pet {

    @Id // Define a chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento
    private Long id;

    // Dados que aparecem na página 'Meus Pets'
    private String type; // Ex: cão ou gato
    private String nome; // Ex: Pudim
    private String raca; // Ex: Caniche
    private int idade;   // Ex: 4
    private boolean castrado; // Ex: Sim/Não
    private long tutorId;



}