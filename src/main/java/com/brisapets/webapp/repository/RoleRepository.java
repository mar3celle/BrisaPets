package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de Repositório para a entidade Role.
 * Extende JpaRepository para fornecer métodos CRUD básicos.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Encontra uma Role pelo seu nome (Ex: "ROLE_CLIENTE" ou "ROLE_ADMIN").
     * Este é um método crucial para o processo de registo e autenticação,
     * permitindo ao serviço (UserServiceImpl) buscar a role a ser atribuída.
     *
     * @param name o nome da Role (String)
     * @return um Optional contendo a Role, se encontrada.
     */
    Optional<Role> findByName(String name);
}
