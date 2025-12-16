package com.brisapets.webapp.controller;

import com.brisapets.webapp.service.AppointmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/appointment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/confirm/{id}")
    public String confirmAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateAppointmentStatus(id, "Confirmed");
            redirectAttributes.addFlashAttribute("adminMessage", "Agendamento confirmado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao confirmar agendamento: " + e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateAppointmentStatus(id, "Canceled");
            redirectAttributes.addFlashAttribute("adminMessage", "Agendamento cancelado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao cancelar agendamento: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}