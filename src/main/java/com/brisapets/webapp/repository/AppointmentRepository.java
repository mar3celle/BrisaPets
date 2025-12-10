package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    @Query("SELECT a FROM Appointment a WHERE a.pet.tutorId = ?1 AND a.deleted = false")
    List<Appointment> findByPet_TutorId(Long tutorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.pet.tutorId = ?1 AND a.deleted = false")
    Page<Appointment> findByPet_TutorId(Long tutorId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime = ?1 AND a.deleted = false")
    List<Appointment> findByAppointmentDateTime(LocalDateTime appointmentDateTime);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN ?1 AND ?2 AND a.deleted = false")
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Appointment a WHERE a.startDate <= ?1 AND a.endDate >= ?2 AND a.deleted = false")
    List<Appointment> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.deleted = false")
    List<Appointment> findAllActive();
    
    @Query("SELECT a FROM Appointment a WHERE a.deleted = false")
    Page<Appointment> findAllActive(Pageable pageable);
}