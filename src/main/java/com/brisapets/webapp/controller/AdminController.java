package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.CalendarService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.PricingService;
import com.brisapets.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    private final UserService userService;
    private final PetService petService;
    private final AppointmentService appointmentService;
    private final CalendarService calendarService;
    private final PricingService pricingService;

    @Autowired
    public AdminController(UserService userService, PetService petService, AppointmentService appointmentService,
                          CalendarService calendarService, PricingService pricingService) {
        this.userService = userService;
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.calendarService = calendarService;
        this.pricingService = pricingService;
    }

    /**
     * Rota do Painel de Administração.
     * Carrega todos os dados críticos do sistema para exibição.
     */
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("allUsers", users);

        List<Pet> pets = petService.findAllPets();
        model.addAttribute("allPets", pets);

        List<Appointment> appointments = appointmentService.findAllAppointments();
        model.addAttribute("allAppointments", appointments);
        
        model.addAttribute("currentPage", "admin");

        return "admin";
    }

    @PostMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Prevent self-deletion
            String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.findByEmail(currentUserEmail);
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("adminError", "Não pode deletar a sua própria conta.");
                return "redirect:/admin";
            }
            
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Utilizador com ID " + id + " removido com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao remover o utilizador: " + e.getMessage());
        }

        return "redirect:/admin";
    }
    
    @PostMapping("/admin/appointment/confirm/{id}")
    public String confirmAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateAppointmentStatus(id, "Confirmed");
            redirectAttributes.addFlashAttribute("adminMessage", "Agendamento confirmado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao confirmar agendamento: " + e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/appointment/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateAppointmentStatus(id, "Canceled");
            redirectAttributes.addFlashAttribute("adminMessage", "Agendamento cancelado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Erro ao cancelar agendamento: " + e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @GetMapping("/adminCalendar")
    public String adminCalendar(Model model, @RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(value = "month", required = false) Integer month) {
        YearMonth currentYearMonth;
        if (year != null && month != null) {
            currentYearMonth = YearMonth.of(year, month);
        } else {
            currentYearMonth = YearMonth.now();
        }
        
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
        model.addAttribute("currentDate", LocalDate.now());
        
        // Navigation links
        model.addAttribute("prevMonthYear", currentYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", currentYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextMonthYear", currentYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", currentYearMonth.plusMonths(1).getMonthValue());
        
        // Load all pets for the dropdown
        List<Pet> allPets = petService.findAllPets();
        model.addAttribute("allPets", allPets);
        
        // Load appointments for the current month
        List<Appointment> monthAppointments = appointmentService.findAppointmentsInMonth(currentYearMonth);
        model.addAttribute("monthAppointments", monthAppointments);
        
        model.addAttribute("currentPage", "adminCalendar");
        return "adminCalendar";
    }
    
    @PostMapping("/admin/appointment/save")
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
            Optional<Pet> optionalPet = petService.findPetById(petId);
            if (optionalPet.isEmpty()) {
                redirectAttributes.addFlashAttribute("adminError", "Pet não encontrado.");
                return "redirect:/adminCalendar";
            }
            
            Appointment appointment = new Appointment();
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
                
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) ? 
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
                
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime appointmentDateTime = date.atTime(java.time.LocalTime.parse(timeStr));
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
    
    @GetMapping("/api/admin/availability/range")
    @ResponseBody
    public boolean checkDateRangeAvailability(@RequestParam("startDate") String startDateStr,
                                            @RequestParam("endDate") String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            return appointmentService.isDateRangeAvailable(startDate, endDate);
        } catch (Exception e) {
            return false;
        }
    }
}
