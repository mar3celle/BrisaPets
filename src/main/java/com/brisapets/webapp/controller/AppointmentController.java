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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final CalendarService calendarService; // NOVO: Injeção do CalendarService

    // Construtor com injeção de dependências
    @Autowired
    public AppointmentController(
            PetService petService,
            AppointmentService appointmentService,
            UserService userService,
            PricingService pricingService,
            CalendarService calendarService // NOVO: Adicionado
    ) {
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.pricingService = pricingService;
        this.calendarService = calendarService; // NOVO: Atribuição
    }

    /**
     * MÉTODO DE AJUDA: Obtém o ID do utilizador logado usando o email
     */
    private Long getLoggedInUserId() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(userEmail).getId();
        } catch (UsernameNotFoundException e) {
            // Em um sistema real, isto deve ser tratado com uma exceção de segurança
            // Ou um redirecionamento para login.
            throw new RuntimeException("Utilizador não autenticado ou não encontrado.", e);
        }
    }


    /**
     * 1. Rota para a página de Agendamentos (Tabela de Agendamentos do Utilizador)
     */
    @GetMapping("/appointments")
    public String listAppointments(Model model) {
        Long currentUserId = getLoggedInUserId();

        // Lista de agendamentos para o utilizador logado
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

        // Se o utilizador não tem pets, não pode agendar
        if (userPets.isEmpty()) {
            model.addAttribute("error", "Não pode agendar sem registar um Pet primeiro.");
            return "booking"; // Retorna para a página de agendamento, mas com erro
        }

        // 2. Adicionar o Pet e um novo Appointment (vazio) ao Model
        model.addAttribute("pets", userPets);
        // O objeto 'appointmentForm' é crucial para o Thymeleaf preencher
        model.addAttribute("appointmentForm", new Appointment());

        // 3. Lógica do Calendário
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

        // Adiciona informações para construir o calendário (dias, mês/ano atual, links)
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("calendarDays", calendarService.calculateCalendarDays(currentYearMonth));
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("monthName", currentYearMonth.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "PT")));
        model.addAttribute("yearValue", currentYearMonth.getYear());

        // Links de navegação do calendário
        model.addAttribute("prevMonthYear", currentYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", currentYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextMonthYear", currentYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", currentYearMonth.plusMonths(1).getMonthValue());

        // 4. Placeholder para Horários Disponíveis
        List<String> availableTimes = List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00");
        model.addAttribute("availableTimes", availableTimes);

        // 5. Placeholder para Serviços e Preços

        Map<String, BigDecimal> servicesAndPrices = new HashMap<>();
        servicesAndPrices.put("Banho", pricingService.getServicePrice("Banho"));
        servicesAndPrices.put("Banho e Tosquia Intima", pricingService.getServicePrice("Banho e Tosquia Intima"));
        servicesAndPrices.put("Banho e Tosquia Geral", pricingService.getServicePrice("Banho e Tosquia Geral"));
        model.addAttribute("services", servicesAndPrices);


        return "booking"; // Assume que o template é booking.html (o nome do ficheiro javascript é booking.js)
    }


    // 3. Método POST para salvar o agendamento
    @PostMapping("/agendar/save")
    public String saveAppointment(
            @ModelAttribute("appointmentForm") Appointment appointment,
            @RequestParam("petId") Long petId, // ID do Pet selecionado
            @RequestParam("selectedDate") String dateStr, // Data selecionada (YYYY-MM-DD)
            @RequestParam("selectedTime") String timeStr, // Hora selecionada (HH:MM)
            @RequestParam("serviceName") String serviceName, // Serviço selecionado
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Validar e converter Data e Hora para LocalDateTime
            LocalDate date = LocalDate.parse(dateStr);
            // Combina a data e hora. O `timeStr` deve estar em HH:MM.
            LocalDateTime appointmentDateTime = date.atTime(java.time.LocalTime.parse(timeStr));

            // 2. Setar os campos do objeto Appointment com a data/hora e nome do serviço
            appointment.setAppointmentDateTime(appointmentDateTime);
            appointment.setServiceName(serviceName);

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

            // 4. Definir o preço do serviço
            appointment.setValue(pricingService.getServicePrice(appointment.getServiceName()));
            appointment.setIsPaid(false); // Por defeito não pago

            // 5. Validar se a data é futura
            if (appointmentDateTime.isBefore(java.time.LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Não é possível agendar para uma data passada.");
                return "redirect:/agendar";
            }


            // 6. Salvar o agendamento
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Agendamento criado com sucesso para " + appointment.getFormattedDateTime() + "!");
        } catch (java.time.format.DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro no formato da Data/Hora.");
            return "redirect:/agendar";
        } catch (Exception e) {
            // Logar o erro para debug
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao criar o agendamento. " + e.getMessage());
            return "redirect:/agendar";
        }

        return "redirect:/appointments";
    }
}