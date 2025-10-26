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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final PetService petService;
    private final AppointmentService appointmentService;
    private final UserService userService;

    // Construtor com as TRÊS injeções de dependência
    @Autowired
    public AppointmentController(PetService petService, AppointmentService appointmentService, UserService userService) {
        this.petService = petService;
        this.appointmentService = appointmentService;
        this.userService = userService;
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

        model.addAttribute("days", calculateCalendarDays(currentYearMonth));
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
                redirectAttributes.addFlashAttribute("errorMessage", "Erro: Tentativa de agendar serviço para um Pet não registado na sua conta.");
                return "redirect:/agendar";
            }

            appointment.setPet(optionalPet.get());

            // 2. Salvar o agendamento
            appointmentService.saveAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Agendamento criado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro: Pet não encontrado.");
        }

        return "redirect:/appointments";
    }

    // 4. MÉTODO AUXILIAR ESSENCIAL: Lógica para gerar a grelha do calendário
    private List<Integer> calculateCalendarDays(YearMonth yearMonth) {
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Obtém o valor do dia da semana (1=Segunda, 7=Domingo)
        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        // Ajusta para o padding correto (se for Domingo, o padding deve ser 0 para começar na primeira coluna, ou 6 se a semana começar no domingo, mas aqui a grelha começa na Segunda)
        // Para uma grelha que começa em Domingo: padding = firstDayOfWeekValue % 7;
        // Para uma grelha que começa em Segunda (como no seu HTML): padding é 0 para Seg, 1 para Terça, etc.
        // O valor 1 (Monday) no Java deve mapear para a primeira coluna no HTML.
        // O valor 7 (Sunday) no Java deve mapear para a última coluna no HTML.

        // Ajuste para o seu HTML: A sua grelha HTML começa em DOMINGO, mas o enum Java DayOfWeek começa em SEGUNDA (1).
        // DOM: 7 (Java) -> 0 (Padding)
        // SEG: 1 (Java) -> 1 (Padding)
        // O seu HTML começa em DOM, por isso vamos usar 7 como o primeiro dia.
        int dayOfWeekForCalendar = firstDayOfWeekValue % 7;
        if (dayOfWeekForCalendar == 0) { // Se for domingo (7), torna-se 0 após o módulo.
            dayOfWeekForCalendar = 7;
        }

        // Se o seu calendário no HTML começa em Domingo:
        int padding;
        if (dayOfWeekForCalendar == 7) { // Domingo
            padding = 0;
        } else {
            padding = dayOfWeekForCalendar;
        }

        List<Integer> days = new ArrayList<>();

        // Adiciona preenchimento (nulls)
        for (int i = 0; i < padding; i++) {
            days.add(null);
        }

        // Adiciona dias reais do mês
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        return days;
    }
}