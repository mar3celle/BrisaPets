package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        // Validate required fields
        if (appointment.getPet() == null) {
            throw new IllegalArgumentException("Pet é obrigatório");
        }
        if (appointment.getServiceName() == null || appointment.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do serviço é obrigatório");
        }
        if (appointment.getValue() == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }
        
        // Set default status if not provided
        if (appointment.getStatus() == null) {
            appointment.setStatus("Pending");
        }
        
        // Set default isPaid if not provided
        if (appointment.getIsPaid() == null) {
            appointment.setIsPaid(false);
        }
        
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAllActive();
    }

    public List<Appointment> findAppointmentByTutor(Long tutorId){
        return appointmentRepository.findByPet_TutorId(tutorId);
    }
    public List<Appointment> findAppointmentsByTutor(Long tutorId) {
        return appointmentRepository.findByPet_TutorId(tutorId);
    }
    public boolean isSlotAvailable(LocalDateTime startTime) {
        // Check for appointments within 1 hour window (typical service duration)
        LocalDateTime endTime = startTime.plusHours(1);
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByAppointmentDateTimeBetween(startTime.minusMinutes(59), endTime);
        return conflictingAppointments.isEmpty();
    }
    
    @Transactional
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }
    
    public List<String> getAvailableTimeSlots(LocalDate date) {
        List<String> allSlots = List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00");
        return allSlots.stream()
            .filter(time -> isSlotAvailable(date.atTime(LocalTime.parse(time))))
            .toList();
    }
    
    public boolean isDateRangeAvailable(LocalDate startDate, LocalDate endDate) {
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        return conflictingAppointments.isEmpty();
    }
    
    public List<Appointment> findAppointmentsInMonth(YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);
        return appointmentRepository.findByAppointmentDateTimeBetween(startDateTime, endDateTime);
    }
    
    public Appointment findAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElse(null);
    }
    
    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.softDelete();
        appointmentRepository.save(appointment);
    }

  }