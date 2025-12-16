package com.brisapets.webapp.dto;

import java.math.BigDecimal;

public record AppointmentSummaryDto(
    Long id,
    String petName,
    String serviceName,
    String formattedDate,
    BigDecimal value,
    String status,
    Boolean isPaid
) {}