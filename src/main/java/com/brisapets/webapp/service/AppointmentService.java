package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment saveAppointment(Appointment appointment) {
        // Lógica de negócio, como verificação de disponibilidade, iria aqui
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> findAppointmentByTutor(Long tutorId){
        return appointmentRepository.findByPet_TutorId(tutorId);
    }
    public List<Appointment> findAppointmentsByTutor(Long tutorId) {
        return appointmentRepository.findByPet_TutorId(tutorId);
    }
  }