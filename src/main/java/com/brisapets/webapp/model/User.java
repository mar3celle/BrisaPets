package com.brisapets.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_deleted", columnList = "deleted"),
    @Index(name = "idx_email_deleted", columnList = "email, deleted")
})
public class User extends BaseEntity {



    // Credenciais
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Informações Pessoais
    private String firstName;
    private String lastName;
    private String phone;
    private String nif;

    // NOVOS CAMPOS DE MORADA
    private String address;
    private String city;
    private String zipCode; // Ex: 4700-000

    // Relação com Perfis (Roles)
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @org.hibernate.annotations.BatchSize(size = 10)
    private Collection<Role> roles = new HashSet<>();

    // Construtor usado no UserServiceImpl para Registo
    public User(String email, String password, String firstName, String lastName, String phone, Collection<Role> roles) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.roles = roles;
    }
}
