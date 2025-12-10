
package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.deleted = false")
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActive();
    
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActive(Pageable pageable);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = ?1 AND u.deleted = false")
    Optional<User> findByIdWithRoles(Long id);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.deleted = false")
    List<User> findAllWithRoles();
    
    // For OAuth2 - find by email without deleted filter
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmailIncludingDeleted(String email);
}