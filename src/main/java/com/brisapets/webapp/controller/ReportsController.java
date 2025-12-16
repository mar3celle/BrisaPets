package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.AppointmentSummaryDto;
import com.brisapets.webapp.mapper.AppointmentMapper;
import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportsController {

    private final AppointmentService appointmentService;
    private final ReportService reportService;

    public ReportsController(AppointmentService appointmentService, ReportService reportService) {
        this.appointmentService = appointmentService;
        this.reportService = reportService;
    }

    @GetMapping
    public String reports(Model model,
                         @RequestParam(value = "dateRange", defaultValue = "all") String dateRange,
                         @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
                         @RequestParam(value = "status", defaultValue = "all") String status) {
        
        var filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
        model.addAttribute("appointments", filteredAppointments);
        model.addAttribute("totalRevenue", String.format("€ %.2f", reportService.calculateTotalRevenue(filteredAppointments)));
        model.addAttribute("totalServices", String.valueOf(reportService.getTotalServices(filteredAppointments)));
        model.addAttribute("avgTicket", String.format("€ %.2f", reportService.calculateAverageTicket(filteredAppointments)));
        model.addAttribute("newClients", String.valueOf(reportService.getNewClients(filteredAppointments)));
        model.addAttribute("currentPage", "reports");
        
        return "reports";
    }

    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchReports(
            @RequestParam(value = "dateRange", defaultValue = "all") String dateRange,
            @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
            @RequestParam(value = "status", defaultValue = "all") String status) {
        
        var filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> metrics = new HashMap<>();
        
        metrics.put("totalRevenue", String.format("€ %.2f", reportService.calculateTotalRevenue(filteredAppointments)));
        metrics.put("totalServices", String.valueOf(reportService.getTotalServices(filteredAppointments)));
        metrics.put("avgTicket", String.format("€ %.2f", reportService.calculateAverageTicket(filteredAppointments)));
        metrics.put("newClients", String.valueOf(reportService.getNewClients(filteredAppointments)));
        
        var appointmentData = filteredAppointments.stream()
            .map(AppointmentMapper::toSummaryDto)
            .toList();
        
        response.put("metrics", metrics);
        response.put("appointments", appointmentData);
        
        return response;
    }

    @GetMapping("/export")
    public void exportReports(
            @RequestParam(value = "dateRange", defaultValue = "last-30") String dateRange,
            @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
            @RequestParam(value = "status", defaultValue = "all") String status,
            HttpServletResponse response) throws IOException {
        
        var filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_agendamentos.csv");
        
        PrintWriter writer = response.getWriter();
        writer.println("ID,Cliente,Data,Serviço,Valor,Estado");
        
        for (Appointment appointment : filteredAppointments) {
            writer.printf("%d,%s,%s,%s,%.2f,%s%n",
                appointment.getId(),
                appointment.getPet().getNome(),
                appointment.getFormattedDateTime(),
                appointment.getServiceName(),
                appointment.getValue(),
                appointment.getStatus()
            );
        }
        
        writer.flush();
    }

    @PatchMapping("/appointments/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            appointmentService.updateAppointmentStatus(id, newStatus);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/appointments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id) {
        try {
            var appointment = appointmentService.findAppointmentById(id);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", appointment.getId());
            response.put("petName", appointment.getPet().getNome());
            response.put("serviceName", appointment.getServiceName());
            response.put("dateTimeForInput", appointment.getAppointmentDateTime().toString());
            response.put("value", appointment.getValue());
            response.put("status", appointment.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/appointments/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            var appointment = appointmentService.findAppointmentById(id);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            
            appointment.setServiceName((String) request.get("serviceName"));
            appointment.setAppointmentDateTime(java.time.LocalDateTime.parse((String) request.get("appointmentDateTime")));
            appointment.setValue(new BigDecimal(request.get("value").toString()));
            appointment.setStatus((String) request.get("status"));
            
            appointmentService.saveAppointment(appointment);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/appointments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        try {
            var appointment = appointmentService.findAppointmentById(id);
            if (appointment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.notFound().build();
            }
            
            appointmentService.deleteAppointment(id);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "Appointment deleted successfully");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    private List<Appointment> getFilteredAppointments(String dateRange, String serviceType, String status) {
        var appointments = appointmentService.findAllAppointments();
        
        // Filter by date range
        if (!"all".equals(dateRange)) {
            var currentMonth = YearMonth.now();
            switch (dateRange) {
                case "this-month" -> appointments = appointmentService.findAppointmentsInMonth(currentMonth);
                case "last-month" -> appointments = appointmentService.findAppointmentsInMonth(currentMonth.minusMonths(1));
                case "last-30" -> {
                    var thirtyDaysAgo = LocalDate.now().minusDays(30);
                    appointments = appointments.stream()
                        .filter(apt -> apt.getAppointmentDateTime() != null && 
                                      !apt.getAppointmentDateTime().toLocalDate().isBefore(thirtyDaysAgo))
                        .toList();
                }
            }
        }
        
        // Filter by service type
        if (!"all".equals(serviceType)) {
            appointments = appointments.stream()
                .filter(apt -> apt.getServiceName() != null && serviceType.equals(apt.getServiceName()))
                .toList();
        }
        
        // Filter by status
        if (!"all".equals(status)) {
            appointments = appointments.stream()
                .filter(apt -> switch (status) {
                    case "paid" -> Boolean.TRUE.equals(apt.getIsPaid());
                    case "pending" -> "Pending".equals(apt.getStatus()) && !Boolean.TRUE.equals(apt.getIsPaid());
                    case "confirmed" -> "Confirmed".equals(apt.getStatus());
                    case "canceled" -> "Canceled".equals(apt.getStatus());
                    default -> true;
                })
                .toList();
        }
        
        return appointments;
    }
}