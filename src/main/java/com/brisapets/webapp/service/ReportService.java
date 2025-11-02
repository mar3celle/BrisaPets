package com.brisapets.webapp.service;

import com.brisapets.webapp.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {
    
    private final AppointmentService appointmentService;
    
    @Autowired
    public ReportService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    
    public BigDecimal calculateTotalRevenue() {
        return appointmentService.findAllAppointments().stream()
            .filter(appointment -> Boolean.TRUE.equals(appointment.getIsPaid()))
            .map(Appointment::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public long getTotalServices() {
        return appointmentService.findAllAppointments().size();
    }
    
    public BigDecimal calculateAverageTicket() {
        BigDecimal totalRevenue = calculateTotalRevenue();
        long totalServices = getTotalServices();
        return totalServices > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(totalServices), 2, BigDecimal.ROUND_HALF_UP) : 
            BigDecimal.ZERO;
    }
}