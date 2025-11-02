package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final PetService petService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PricingService pricingService;
    private final CalendarService calendarService;

    @Autowired
    public AppointmentController(PetService petService, AppointmentService appointmentService, 
                               UserService userService, PricingService pricingService, 
                               CalendarService calendarService) {
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.pricingService = pricingService;
        this.calendarService = calendarService;
    }

    /**
     * MÉTODO DE AJUDA: Obtém o ID do utilizador logado usando o email do Spring Security.
     */
    private Long getLoggedInUserId() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return userService.findByEmail(userEmail).getId();
        } catch (UsernameNotFoundException e) {
            throw new SecurityException("Utilizador autenticado não encontrado na base de dados.", e);
        }
    }




    // 1. Mapeamento para exibir a página de agendamento com calendário dinâmico
    @GetMapping("/agendar")
    public String showBookingPage(
            Model model,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {

        // --- SEGURANÇA FINALIZADA ---
        Long tutorId = getLoggedInUserId();

        // 2. Carregar os pets do utilizador logado
        List<Pet> pets = petService.findPetsByTutor(tutorId);
        model.addAttribute("pets", pets);

        // 3. Lógica do Calendário (Mês Atual/Próximo)
        YearMonth currentYearMonth = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        YearMonth prevMonth = currentYearMonth.minusMonths(1);
        YearMonth nextMonth = currentYearMonth.plusMonths(1);

        // 4. Adicionar os dados do Calendário ao Model (CORREÇÃO DA INJEÇÃO DE VARIÁVEIS)
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("prevMonthYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextMonthYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("currentDate", LocalDate.now()); // Necessário para desativar dias passados

        model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
        model.addAttribute("appointmentForm", new Appointment());

        return "booking"; // Busca o template booking.html
    }


    // 2. Mapeamento para listar os agendamentos
    @GetMapping("/appointments")
    public String listAppointments(Model model){
        // --- SEGURANÇA FINALIZADA: Usa o ID do utilizador logado ---
        Long tutorId = getLoggedInUserId();

        List<Appointment> appointments = appointmentService.findAppointmentsByTutor(tutorId);
        model.addAttribute("appointments", appointments);
        return "appointments"; // Assume que o template é appointments.html
    }

    // 3. Método POST para salvar o agendamento
    @PostMapping("/agendar/save")
    public String saveAppointment(
            @ModelAttribute("appointmentForm") Appointment appointment,
            @RequestParam("petId") Long petId,
            RedirectAttributes redirectAttributes) {

        // 1. Buscar o Pet para associar (garantir que ele pertence ao utilizador logado)
        Optional<Pet> optionalPet = petService.findPetById(petId);

        if (optionalPet.isPresent()) {
            // CRÍTICO: Garantir que o Pet pertence ao utilizador logado
            if (!optionalPet.get().getTutorId().equals(getLoggedInUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Pet não encontrado na sua conta.");
                return "redirect:/agendar";
            }

            appointment.setPet(optionalPet.get());
            
            // 2. Definir o preço do serviço
            appointment.setValue(pricingService.getServicePrice(appointment.getServiceName()));
            appointment.setIsPaid(false); // Por defeito não pago

            // 3. Salvar o agendamento
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Agendamento criado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro: Pet não encontrado.");
        }

        return "redirect:/appointments";
    }


}