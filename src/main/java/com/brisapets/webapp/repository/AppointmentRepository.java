package com.brisapets.webapp.repository;

import com.brisapets.webapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPet_TutorId(Long tutorId);

    List<Appointment> findByAppointmentDateTime(LocalDateTime appointmentDateTime);

}