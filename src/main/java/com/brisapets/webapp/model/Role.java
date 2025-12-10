// src/main/java/com/brisapets/webapp/model/Role.java

package com.brisapets.webapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "ROLE_ADMIN", "ROLE_CLIENTE"
    private String name;

    public Role(String name) {
        this.name = name;
    }
}