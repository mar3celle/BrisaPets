package com.brisapets.webapp.controller;

import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.Pet;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final PetService petService;
    private final AppointmentService appointmentService;

    // Construtor com as duas injeções de dependência
    @Autowired
    public AppointmentController(PetService petService, AppointmentService appointmentService) {
        this.petService = petService;
        this.appointmentService = appointmentService;
    }

    // 1. Mapeamento para exibir a página de agendamento com calendário dinâmico
    @GetMapping("/agendar")
    public String showBookingPage(
            Model model,
            // Parâmetros opcionais para navegação do calendário
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {

        // --- Lógica do Calendário Dinâmico ---
        YearMonth currentYearMonth;
        if (year != null && month != null) {
            currentYearMonth = YearMonth.of(year, month);
        } else {
            currentYearMonth = YearMonth.now();
        }

        // Calcula a estrutura de dias para o Thymeleaf
        List<Integer> calendarDays = calculateCalendarDays(currentYearMonth);

        // Links de navegação
        YearMonth nextMonth = currentYearMonth.plusMonths(1);
        YearMonth prevMonth = currentYearMonth.minusMonths(1);

        // Adiciona todos os dados do calendário ao Model
        model.addAttribute("currentYearMonth", currentYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("nextMonthYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("prevMonthYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());

        // --- Lógica de Pets e Formulário ---
        // TODO: Mudar para o ID do tutor logado
        List<Pet> pets = petService.findPetsByTutor(1L);
        model.addAttribute("pets", pets);

        // Adiciona o objeto vazio para o formulário POST
        model.addAttribute("appointment", new Appointment());

        return "booking";
    }

    // 2. Lidar com o agendamento (POST do formulário)
    @PostMapping("/agendar/save")
    public String saveAppointment(
            // Recebe o nome do serviço (do card clicado)
            @RequestParam("serviceName") String serviceName,
            // Recebe a data no formato YYYY-MM-DD
            @RequestParam("selectedDate") String selectedDate,
            // Recebe a hora no formato HH:MM
            @RequestParam("selectedTime") String selectedTime,
            // Recebe o ID do Pet selecionado
            @RequestParam("petId") Long petId,
            Model model) {

        // 1. Buscar o Pet
        Optional<Pet> petOptional = petService.findPetById(petId);

        if (petOptional.isEmpty()) {
            // Se o Pet não for encontrado, adiciona erro e retorna para a página
            model.addAttribute("error", "Pet não encontrado. Por favor, tente novamente.");
            // Recarrega o modelo do calendário para evitar erro de Thymeleaf
            showBookingPage(model, null, null);
            return "booking";
        }

        // 2. Criar e preencher o objeto Appointment
        Appointment newAppointment = new Appointment();
        newAppointment.setServiceName(serviceName);

        try {
            // Combina data e hora
            LocalDate date = LocalDate.parse(selectedDate);
            LocalTime time = LocalTime.parse(selectedTime);
            newAppointment.setAppointmentDateTime(LocalDateTime.of(date, time));
        } catch (Exception e) {
            // Caso a conversão falhe (erro no JS ou formato)
            model.addAttribute("error", "Formato de data ou hora inválido.");
            showBookingPage(model, null, null);
            return "booking";
        }

        newAppointment.setPet(petOptional.get());

        // 3. Salvar
        appointmentService.saveAppointment(newAppointment);

        // 4. Redirecionar para a lista de agendamentos para ver o sucesso
        return "redirect:/appointments";
    }
    @GetMapping("/appointments")
    public String listAppointments(Model model){
        Long tutorId = 1L;
        List<Appointment> appointments= appointmentService.findAppointmentsByTutor(tutorId);
        model.addAttribute("appointments", appointments);
        return "appointments";
    }

    /**
     * Gera uma lista de Integers para os dias do mês.
     * Valores 'null' são usados para preenchimento (padding) do calendário.
     */
    private List<Integer> calculateCalendarDays(YearMonth yearMonth) {
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Obtém o valor do dia da semana (1 = Monday, 7 = Sunday)
        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();

        // Ajusta para o calendário começar no DOMINGO (0)
        // Se for DOMINGO (7), queremos 0 padding.
        // Se for SEGUNDA (1), queremos 1 padding, etc.
        int padding = firstDayOfWeekValue % 7;

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