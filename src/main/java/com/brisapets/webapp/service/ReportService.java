package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    private final AppointmentService appointmentService;
    
    @Autowired
    public ReportService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    
    public List<Appointment> getFilteredAppointments(String dateRange, String serviceType, String status) {
        List<Appointment> appointments = appointmentService.findAllAppointments();
        
        // Filter by date range
        LocalDate startDate = getStartDateFromRange(dateRange);
        LocalDate endDate = LocalDate.now();
        
        appointments = appointments.stream()
            .filter(appointment -> {
                if (appointment.getAppointmentDateTime() == null) {
                    return false;
                }
                LocalDate appointmentDate = appointment.getAppointmentDateTime().toLocalDate();
                return !appointmentDate.isBefore(startDate) && !appointmentDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        // Filter by service type
        if (!"all".equals(serviceType)) {
            appointments = appointments.stream()
                .filter(appointment -> appointment.getServiceName().toLowerCase().contains(serviceType.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // Filter by status
        if (!"all".equals(status)) {
            if ("paid".equals(status)) {
                appointments = appointments.stream()
                    .filter(Appointment::getIsPaid)
                    .collect(Collectors.toList());
            } else if ("pending".equals(status)) {
                appointments = appointments.stream()
                    .filter(appointment -> !appointment.getIsPaid())
                    .collect(Collectors.toList());
            }
        }
        
        return appointments;
    }
    
    private LocalDate getStartDateFromRange(String dateRange) {
        LocalDate now = LocalDate.now();
        switch (dateRange) {
            case "last-30":
                return now.minusDays(30);
            case "this-month":
                return now.withDayOfMonth(1);
            case "last-month":
                return now.minusMonths(1).withDayOfMonth(1);
            default:
                return now.minusDays(30);
        }
    }
    
    public BigDecimal calculateTotalRevenue() {
        return calculateTotalRevenue(appointmentService.findAllAppointments());
    }
    
    public BigDecimal calculateTotalRevenue(List<Appointment> appointments) {
        return appointments.stream()
            .filter(appointment -> Boolean.TRUE.equals(appointment.getIsPaid()))
            .map(Appointment::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public long getTotalServices() {
        return getTotalServices(appointmentService.findAllAppointments());
    }
    
    public long getTotalServices(List<Appointment> appointments) {
        return appointments.size();
    }
    
    public BigDecimal calculateAverageTicket() {
        return calculateAverageTicket(appointmentService.findAllAppointments());
    }
    
    public BigDecimal calculateAverageTicket(List<Appointment> appointments) {
        BigDecimal totalRevenue = calculateTotalRevenue(appointments);
        long paidServices = appointments.stream()
            .filter(appointment -> Boolean.TRUE.equals(appointment.getIsPaid()))
            .count();
        return paidServices > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(paidServices), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
    }
    
    public long getNewClients(List<Appointment> appointments) {
        return appointments.stream()
            .map(appointment -> appointment.getPet().getId())
            .distinct()
            .count();
    }
}