package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.CalendarService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.PricingService;
import com.brisapets.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final CalendarService calendarService; //  Injeção do CalendarService

    // Construtor com injeção de dependências
    @Autowired
    public AppointmentController(
            PetService petService,
            AppointmentService appointmentService,
            UserService userService,
            PricingService pricingService,
            CalendarService calendarService
    ) {
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.pricingService = pricingService;
        this.calendarService = calendarService;
    }

    /**
     * MÉTODO DE AJUDA: Obtém o ID do utilizador logado usando o email
     */
    private Long getLoggedInUserId() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(userEmail).getId();
        } catch (UsernameNotFoundException e) {

            throw new RuntimeException("Utilizador não autenticado ou não encontrado.", e);
        }
    }


    /**
     * 1. Rota para a página de Agendamentos (Tabela de Agendamentos do Utilizador)
     */
    @GetMapping("/appointments")
    public String listAppointments(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getLoggedInUserId();

        // Lista de agendamentos para o utilizador logado com paginação
        List<Appointment> userAppointments = appointmentService.findAppointmentsByTutor(currentUserId);
        model.addAttribute("appointments", userAppointments);

        return "appointments"; // Assume que o template é appointments.html
    }

    /**
     * 2. Rota para o formulário de Agendamento (Seleção de Serviço, Pet, Data/Hora)
     */
    @GetMapping("/agendar")
    public String showAppointmentForm(Model model,
                                      @RequestParam(value = "yearMonth", required = false) String yearMonthStr) {
        Long currentUserId = getLoggedInUserId();

        // 1. Carregar lista de Pets do utilizador logado
        List<Pet> userPets = petService.findPetsByTutor(currentUserId);
        model.addAttribute("pets", userPets);

        // 2. Se o utilizador não tem pets, mostra aviso e não tenta montar o formulário completo
        if (userPets == null || userPets.isEmpty()) {
            model.addAttribute("noPets", true);
            YearMonth currentYearMonth = (yearMonthStr != null && !yearMonthStr.isEmpty())
                    ? YearMonth.parse(yearMonthStr)
                    : YearMonth.now();

            model.addAttribute("currentYearMonth", currentYearMonth);
            model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
            model.addAttribute("currentDate", LocalDate.now());
            model.addAttribute("monthName", currentYearMonth.getMonth()
                    .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "PT")));
            model.addAttribute("yearValue", currentYearMonth.getYear());
            model.addAttribute("prevMonthYear", currentYearMonth.minusMonths(1).getYear());
            model.addAttribute("prevMonth", currentYearMonth.minusMonths(1).getMonthValue());
            model.addAttribute("nextMonthYear", currentYearMonth.plusMonths(1).getYear());
            model.addAttribute("nextMonth", currentYearMonth.plusMonths(1).getMonthValue());

            return "booking";
        }

        model.addAttribute("appointmentForm", new Appointment());

        YearMonth currentYearMonth;
        if (yearMonthStr != null && !yearMonthStr.isEmpty()) {
            try {
                currentYearMonth = YearMonth.parse(yearMonthStr);
            } catch (Exception e) {
                currentYearMonth = YearMonth.now();
            }
        } else {
            currentYearMonth = YearMonth.now();
        }

        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("monthName", currentYearMonth.getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "PT")));
        model.addAttribute("yearValue", currentYearMonth.getYear());
        model.addAttribute("prevMonthYear", currentYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", currentYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextMonthYear", currentYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", currentYearMonth.plusMonths(1).getMonthValue());

        List<String> availableTimes = List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00");
        model.addAttribute("availableTimes", availableTimes);

        Map<String, BigDecimal> servicesAndPrices = new HashMap<>();
        servicesAndPrices.put("Banho", pricingService.getServicePrice("Banho"));
        servicesAndPrices.put("Banho e Tosquia Intima", pricingService.getServicePrice("Banho e Tosquia Intima"));
        servicesAndPrices.put("Banho e Tosquia Geral", pricingService.getServicePrice("Banho e Tosquia Geral"));
        servicesAndPrices.put("Pet Sitting", pricingService.getServicePrice("Pet Sitting"));
        servicesAndPrices.put("Hosting", pricingService.getServicePrice("Hosting"));
        model.addAttribute("services", servicesAndPrices);

        return "booking";
    }

    // 3. Método POST para salvar o agendamento
    @PostMapping("/agendar/save")
    public String saveAppointment(
            @ModelAttribute("appointmentForm") Appointment appointment,
            @RequestParam("petId") Long petId,
            @RequestParam(value = "selectedDate", required = false) String dateStr,
            @RequestParam(value = "selectedTime", required = false) String timeStr,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "observations", required = false) String observations,
            @RequestParam("serviceName") String serviceName,
            RedirectAttributes redirectAttributes) {

        try {
            // Set service name first
            appointment.setServiceName(serviceName);
            
            // Handle date range services vs regular services
            boolean isDateRangeService = "Pet Sitting".equals(serviceName) || "Hosting".equals(serviceName);
            
            if (isDateRangeService) {
                // Handle date range services
                if (startDateStr == null || startDateStr.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erro: Data de início é obrigatória.");
                    return "redirect:/agendar";
                }
                
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) ? 
                    LocalDate.parse(endDateStr) : startDate;
                
                // Validate date range
                if (endDate.isBefore(startDate)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erro: Data de fim deve ser posterior à data de início.");
                    return "redirect:/agendar";
                }
                
                appointment.setStartDate(startDate);
                appointment.setEndDate(endDate);
                appointment.setObservations(observations);
                
                // Set appointment datetime to start date at 9:00 for compatibility
                appointment.setAppointmentDateTime(startDate.atTime(9, 0));
            } else {
                // Handle regular services
                if (dateStr == null || timeStr == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erro: Data e hora são obrigatórias.");
                    return "redirect:/agendar";
                }
                
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime appointmentDateTime = date.atTime(java.time.LocalTime.parse(timeStr));
                appointment.setAppointmentDateTime(appointmentDateTime);
            }

            // 3. Buscar o Pet para associar (garantir que ele pertence ao utilizador logado)
            Optional<Pet> optionalPet = petService.findPetById(petId);

            if (optionalPet.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Pet não encontrado.");
                return "redirect:/agendar";
            }

            Pet pet = optionalPet.get();
            //  Garantir que o Pet pertence ao utilizador logado
            if (!pet.getTutorId().equals(getLoggedInUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Pet não pertence à sua conta.");
                return "redirect:/agendar";
            }

            appointment.setPet(pet);

            // 4. Definir o preço do serviço usando o peso do pet
            BigDecimal value = pricingService.getServicePrice(
                    appointment.getServiceName(),
                    pet.getWeightKg()
            );
            appointment.setValue(value);
            appointment.setIsPaid(false); // Por defeito não pago

            // 5. Validar se a data é futura
            LocalDateTime checkDateTime = isDateRangeService ? 
                appointment.getStartDate().atTime(9, 0) : appointment.getAppointmentDateTime();
                
            if (checkDateTime.isBefore(java.time.LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Não é possível agendar para uma data passada.");
                return "redirect:/agendar";
            }
            
            // 6. Verificar disponibilidade
            if (isDateRangeService) {
                if (!appointmentService.isDateRangeAvailable(appointment.getStartDate(), appointment.getEndDate())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erro: Período não disponível. Escolha outras datas.");
                    return "redirect:/agendar";
                }
            } else {
                if (!appointmentService.isSlotAvailable(appointment.getAppointmentDateTime())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erro: Este horário já está ocupado. Escolha outro horário.");
                    return "redirect:/agendar";
                }
            }


            // 7. Salvar o agendamento
            appointmentService.saveAppointment(appointment);
            String successMessage = isDateRangeService ? 
                "Reserva criada com sucesso para " + appointment.getFormattedDateRange() + "!" :
                "Agendamento criado com sucesso para " + appointment.getFormattedDateTime() + "!";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (java.time.format.DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro no formato da Data/Hora.");
            return "redirect:/agendar";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao criar o agendamento. " + e.getMessage());
            return "redirect:/agendar";
        }

        return "redirect:/appointments";
    }
    
    @GetMapping("/api/availability")
    @ResponseBody
    public List<String> getAvailableSlots(@RequestParam("date") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return appointmentService.getAvailableTimeSlots(date);
        } catch (Exception e) {
            return List.of(); // Return empty list on error
        }
    }
}