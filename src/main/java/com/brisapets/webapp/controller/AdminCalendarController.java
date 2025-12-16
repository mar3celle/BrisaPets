package com.brisapets.webapp.controller;

import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.CalendarService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.PricingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequestMapping("/adminCalendar")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCalendarController {

    private final AppointmentService appointmentService;
    private final CalendarService calendarService;
    private final PetService petService;
    private final PricingService pricingService;

    public AdminCalendarController(AppointmentService appointmentService, CalendarService calendarService,
                                 PetService petService, PricingService pricingService) {
        this.appointmentService = appointmentService;
        this.calendarService = calendarService;
        this.petService = petService;
        this.pricingService = pricingService;
    }

    @GetMapping
    public String adminCalendar(Model model, 
                               @RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(value = "month", required = false) Integer month) {
        
        var currentYearMonth = (year != null && month != null) ? 
            YearMonth.of(year, month) : YearMonth.now();
        
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
        model.addAttribute("currentDate", LocalDate.now());
        
        // Navigation links
        model.addAttribute("prevMonthYear", currentYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", currentYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextMonthYear", currentYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", currentYearMonth.plusMonths(1).getMonthValue());
        
        // Load data
        model.addAttribute("allPets", petService.findAllPets());
        model.addAttribute("monthAppointments", appointmentService.findAppointmentsInMonth(currentYearMonth));
        model.addAttribute("currentPage", "adminCalendar");
        
        return "adminCalendar";
    }

    @PostMapping("/appointment/save")
    public String saveAdminAppointment(
            @RequestParam("petId") Long petId,
            @RequestParam("serviceName") String serviceName,
            @RequestParam(value = "selectedDate", required = false) String dateStr,
            @RequestParam(value = "selectedTime", required = false) String timeStr,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "observations", required = false) String observations,
            RedirectAttributes redirectAttributes) {
        
        try {
            var optionalPet = petService.findPetById(petId);
            if (optionalPet.isEmpty()) {
                redirectAttributes.addFlashAttribute("adminError", "Pet não encontrado.");
                return "redirect:/adminCalendar";
            }
            
            var appointment = new com.brisapets.webapp.model.Appointment();
            appointment.setPet(optionalPet.get());
            appointment.setServiceName(serviceName);
            appointment.setValue(pricingService.getServicePrice(serviceName));
            appointment.setIsPaid(false);
            appointment.setStatus("Confirmed");
            
            boolean isDateRangeService = "Pet Sitting".equals(serviceName) || "Hosting".equals(serviceName);
            
            if (isDateRangeService) {
                if (startDateStr == null || startDateStr.isEmpty()) {
                    redirectAttributes.addFlashAttribute("adminError", "Data de início é obrigatória.");
                    return "redirect:/adminCalendar";
                }
                
                var startDate = LocalDate.parse(startDateStr);
                var endDate = (endDateStr != null && !endDateStr.isEmpty()) ? 
                    LocalDate.parse(endDateStr) : startDate;
                
                appointment.setStartDate(startDate);
                appointment.setEndDate(endDate);
                appointment.setObservations(observations);
                appointment.setAppointmentDateTime(startDate.atTime(9, 0));
                
                if (!appointmentService.isDateRangeAvailable(startDate, endDate)) {
                    redirectAttributes.addFlashAttribute("adminError", "Período não disponível. Existe conflito com outras reservas.");
                    return "redirect:/adminCalendar";
                }
            } else {
                if (dateStr == null || timeStr == null) {
                    redirectAttributes.addFlashAttribute("adminError", "Data e hora são obrigatórias.");
                    return "redirect:/adminCalendar";
                }
                
                var date = LocalDate.parse(dateStr);
                var appointmentDateTime = date.atTime(java.time.LocalTime.parse(timeStr));
                appointment.setAppointmentDateTime(appointmentDateTime);
                
                if (!appointmentService.isSlotAvailable(appointmentDateTime)) {
                    redirectAttributes.addFlashAttribute("adminError", "Horário não disponível. Já existe agendamento neste horário.");
                    return "redirect:/adminCalendar";
                }
            }
            
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("adminMessage", "Agendamento criado com sucesso!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao criar agendamento: " + e.getMessage());
        }
        
        return "redirect:/adminCalendar";
    }

    @GetMapping("/api/availability/range")
    @ResponseBody
    public boolean checkDateRangeAvailability(@RequestParam("startDate") String startDateStr,
                                            @RequestParam("endDate") String endDateStr) {
        try {
            var startDate = LocalDate.parse(startDateStr);
            var endDate = LocalDate.parse(endDateStr);
            return appointmentService.isDateRangeAvailable(startDate, endDate);
        } catch (Exception e) {
            return false;
        }
    }
}