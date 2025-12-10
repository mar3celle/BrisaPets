package com.brisapets.webapp.controller;

import com.brisapets.webapp.dto.UserAddressUpdateDto;
import com.brisapets.webapp.dto.UserPasswordUpdateDto;
import com.brisapets.webapp.dto.UserProfileUpdateDto;
import com.brisapets.webapp.dto.UserRegistrationDto;
import com.brisapets.webapp.model.Appointment;
import com.brisapets.webapp.model.User;
import com.brisapets.webapp.service.AppointmentService;
import com.brisapets.webapp.service.CalendarService;
import com.brisapets.webapp.service.PetService;
import com.brisapets.webapp.service.ReportService;
import com.brisapets.webapp.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final ReportService reportService;
    private final CalendarService calendarService;
    private final PetService petService;

    public WebController(UserService userService, AppointmentService appointmentService, ReportService reportService, CalendarService calendarService, PetService petService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.reportService = reportService;
        this.calendarService = calendarService;
        this.petService = petService;
    }

    // Método de ajuda para obter o ID do utilizador logado
    private Long getLoggedInUserId() {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            return userService.findByEmail(userEmail).getId();
        } catch (UsernameNotFoundException e) {
            throw new SecurityException("Utilizador autenticado não encontrado na base de dados.", e);
        }
    }

    // Mapeia a URL base (/) para o template 'index.html'
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping({"/entrar", "/login", "/autenticar"})
    public String loginPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "login";
    }

    @GetMapping("/hospedagem")
    public String gallery() {
        return "gallery";
    }

    @GetMapping("/reports")
    public String reports(Model model,
                         @RequestParam(value = "dateRange", defaultValue = "all") String dateRange,
                         @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
                         @RequestParam(value = "status", defaultValue = "all") String status) {
        
        List<Appointment> filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
        model.addAttribute("appointments", filteredAppointments);
        model.addAttribute("totalRevenue", String.format("€ %.2f", reportService.calculateTotalRevenue(filteredAppointments)));
        model.addAttribute("totalServices", String.valueOf(reportService.getTotalServices(filteredAppointments)));
        model.addAttribute("avgTicket", String.format("€ %.2f", reportService.calculateAverageTicket(filteredAppointments)));
        model.addAttribute("newClients", String.valueOf(reportService.getNewClients(filteredAppointments)));
        model.addAttribute("currentPage", "reports");
        
        return "reports";
    }

    /**
     * Rota para a página de Perfil.
     * Além do User, injeta os DTOs vazios para os formulários de edição.
     */
    @GetMapping("/perfil")
    public String profile(Model model) {

        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(userEmail);

            model.addAttribute("user", user);

            // Adiciona os DTOs para os formulários no perfil
            // Usamos os dados existentes do User para pré-preencher o form de perfil
            UserProfileUpdateDto profileDto = new UserProfileUpdateDto();
            profileDto.setFirstName(user.getFirstName());
            profileDto.setLastName(user.getLastName());
            profileDto.setPhone(user.getPhone());

            model.addAttribute("profileDto", profileDto);
            model.addAttribute("passwordDto", new UserPasswordUpdateDto());

            UserAddressUpdateDto addressDto = new UserAddressUpdateDto();
            addressDto.setAddress(user.getAddress());
            addressDto.setCity(user.getCity());
            addressDto.setZipCode(user.getZipCode());

            model.addAttribute("addressDto", addressDto);

        } catch (UsernameNotFoundException e) {
            System.err.println("Erro de segurança: Utilizador autenticado não encontrado no DB.");
            return "redirect:/logout";
        } catch (Exception e) {
            System.err.println("Erro inesperado ao carregar perfil: " + e.getMessage());
            return "redirect:/logout";
        }

        return "profile"; // Busca o template profile.html
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/entrar";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }



    @PostMapping("/perfil/deletar")
    public String deleteProfile(RedirectAttributes redirectAttributes) {
        try {
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByEmail(userEmail);
            userService.deleteUserById(user.getId());
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar conta. Tente novamente.");
            return "redirect:/perfil";
        }
    }

    // =========================================================================
    // === NOVOS ENDPOINTS DE EDIÇÃO DO PERFIL ===
    // =========================================================================

    /**
     * 1. Processa a atualização do Nome, Sobrenome e Telefone.
     */
    @PostMapping("/perfil/edit/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") UserProfileUpdateDto profileDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDto", result);
            redirectAttributes.addFlashAttribute("profileDto", profileDto);
            redirectAttributes.addFlashAttribute("errorProfile", "Verifique os erros no formulário de Perfil.");
            return "redirect:/perfil";
        }

        try {
            Long userId = getLoggedInUserId();
            userService.updateProfile(userId, profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil. Tente novamente.");
        }

        return "redirect:/perfil";
    }

    /**
     * 2. Processa a atualização da Senha.
     */
    @PostMapping("/perfil/edit/password")
    public String updatePassword(
            @Valid @ModelAttribute("passwordDto") UserPasswordUpdateDto passwordDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", "Verifique os erros no formulário de Senha.");
            return "redirect:/perfil";
        }

        // Validação adicional de igualdade
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "A nova senha e a confirmação não coincidem.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", "A nova senha e a confirmação não coincidem.");
            return "redirect:/perfil";
        }

        try {
            Long userId = getLoggedInUserId();
            userService.updatePassword(userId, passwordDto);
            redirectAttributes.addFlashAttribute("successMessage", "Senha atualizada com sucesso! Por favor, faça login novamente.");

            // Força o logout após a alteração de senha por segurança
            return "redirect:/logout";

        } catch (IllegalArgumentException e) {
            // Captura erro de senha atual incorreta
            result.rejectValue("currentPassword", null, e.getMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            redirectAttributes.addFlashAttribute("passwordDto", passwordDto);
            redirectAttributes.addFlashAttribute("errorPassword", e.getMessage());
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro inesperado ao atualizar senha.");
            return "redirect:/perfil";
        }
    }

    /**
     * 3. Processa a atualização da Morada.
     */
    @PostMapping("/perfil/edit/address")
    public String updateAddress(
            @ModelAttribute("addressDto") UserAddressUpdateDto addressDto,
            RedirectAttributes redirectAttributes) {


        try {
            Long userId = getLoggedInUserId();
            userService.updateAddress(userId, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Morada atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar morada. Tente novamente.");
        }

        return "redirect:/perfil";
    }
    
    @GetMapping("/admin/reports/search")
    @ResponseBody
    public Map<String, Object> searchReports(
            @RequestParam(value = "dateRange", defaultValue = "all") String dateRange,
            @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
            @RequestParam(value = "status", defaultValue = "all") String status) {
        
        List<Appointment> filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> metrics = new HashMap<>();
        
        metrics.put("totalRevenue", String.format("€ %.2f", reportService.calculateTotalRevenue(filteredAppointments)));
        metrics.put("totalServices", String.valueOf(reportService.getTotalServices(filteredAppointments)));
        metrics.put("avgTicket", String.format("€ %.2f", reportService.calculateAverageTicket(filteredAppointments)));
        metrics.put("newClients", String.valueOf(reportService.getNewClients(filteredAppointments)));
        
        List<Map<String, Object>> appointmentData = filteredAppointments.stream()
            .map(this::convertAppointmentToMap)
            .collect(java.util.stream.Collectors.toList());
        
        response.put("metrics", metrics);
        response.put("appointments", appointmentData);
        
        return response;
    }
    
    private List<Appointment> getFilteredAppointments(String dateRange, String serviceType, String status) {
        List<Appointment> appointments = appointmentService.findAllAppointments();
        
        // Filter by date range
        if (!"all".equals(dateRange)) {
            YearMonth currentMonth = YearMonth.now();
            switch (dateRange) {
                case "this-month":
                    appointments = appointmentService.findAppointmentsInMonth(currentMonth);
                    break;
                case "last-month":
                    appointments = appointmentService.findAppointmentsInMonth(currentMonth.minusMonths(1));
                    break;
                case "last-30":
                    LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                    appointments = appointments.stream()
                        .filter(apt -> apt.getAppointmentDateTime() != null && 
                                      !apt.getAppointmentDateTime().toLocalDate().isBefore(thirtyDaysAgo))
                        .collect(java.util.stream.Collectors.toList());
                    break;
            }
        }
        
        // Filter by service type
        if (!"all".equals(serviceType)) {
            appointments = appointments.stream()
                .filter(apt -> apt.getServiceName() != null && serviceType.equals(apt.getServiceName()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter by status
        if (!"all".equals(status)) {
            appointments = appointments.stream()
                .filter(apt -> {
                    switch (status) {
                        case "paid":
                            return Boolean.TRUE.equals(apt.getIsPaid());
                        case "pending":
                            return "Pending".equals(apt.getStatus()) && !Boolean.TRUE.equals(apt.getIsPaid());
                        case "confirmed":
                            return "Confirmed".equals(apt.getStatus());
                        case "canceled":
                            return "Canceled".equals(apt.getStatus());
                        default:
                            return true;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        return appointments;
    }
    
    @GetMapping("/admin/reports/export")
    public void exportReports(
            @RequestParam(value = "dateRange", defaultValue = "last-30") String dateRange,
            @RequestParam(value = "serviceType", defaultValue = "all") String serviceType,
            @RequestParam(value = "status", defaultValue = "all") String status,
            HttpServletResponse response) throws IOException {
        
        List<Appointment> filteredAppointments = getFilteredAppointments(dateRange, serviceType, status);
        
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
    
    @PatchMapping("/admin/appointments/{id}/status")
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
    
    @GetMapping("/admin/appointments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.findAppointmentById(id);
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
    
    @PutMapping("/admin/appointments/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Appointment appointment = appointmentService.findAppointmentById(id);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            
            appointment.setServiceName((String) request.get("serviceName"));
            appointment.setAppointmentDateTime(java.time.LocalDateTime.parse((String) request.get("appointmentDateTime")));
            appointment.setValue(new java.math.BigDecimal(request.get("value").toString()));
            appointment.setStatus((String) request.get("status"));
            
            appointmentService.saveAppointment(appointment);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/admin/appointments/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        try {
            System.out.println("Attempting to delete appointment with ID: " + id);
            
            Appointment appointment = appointmentService.findAppointmentById(id);
            if (appointment == null) {
                System.out.println("Appointment not found with ID: " + id);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.notFound().build();
            }
            
            appointmentService.deleteAppointment(id);
            System.out.println("Successfully deleted appointment with ID: " + id);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "Appointment deleted successfully");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }
    
    private Map<String, Object> convertAppointmentToMap(Appointment appointment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", appointment.getId());
        map.put("petName", appointment.getPet().getNome());
        map.put("formattedDate", appointment.getFormattedDateTime());
        map.put("value", appointment.getValue());
        map.put("status", appointment.getStatus());
        map.put("isPaid", appointment.getIsPaid());
        return map;
    }
}
