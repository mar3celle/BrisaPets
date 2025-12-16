package com.brisapets.webapp.mapper;

import com.brisapets.webapp.dto.AppointmentDto;
import com.brisapets.webapp.dto.AppointmentSummaryDto;
import com.brisapets.webapp.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public final class AppointmentMapper {
    
    public static AppointmentDto toDto(Appointment appointment) {
        if (appointment == null) return null;
        
        return new AppointmentDto(
            appointment.getId(),
            appointment.getServiceName(),
            appointment.getAppointmentDateTime(),
            appointment.getStartDate(),
            appointment.getEndDate(),
            appointment.getObservations(),
            appointment.getPet() != null ? appointment.getPet().getId() : null,
            appointment.getPet() != null ? appointment.getPet().getNome() : null,
            appointment.getValue(),
            appointment.getIsPaid(),
            appointment.getStatus()
        );
    }
    
    public static AppointmentSummaryDto toSummaryDto(Appointment appointment) {
        if (appointment == null) return null;
        
        return new AppointmentSummaryDto(
            appointment.getId(),
            appointment.getPet() != null ? appointment.getPet().getNome() : "Pet não encontrado",
            appointment.getServiceName(),
            appointment.isDateRangeService() ? appointment.getFormattedDateRange() : appointment.getFormattedDateTime(),
            appointment.getValue(),
            appointment.getStatus(),
            appointment.getIsPaid()
        );
    }
}