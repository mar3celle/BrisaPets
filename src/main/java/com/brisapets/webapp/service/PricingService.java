package com.brisapets.webapp.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PricingService {
    
    private final Map<String, BigDecimal> servicePrices;
    
    public PricingService() {
        servicePrices = new HashMap<>();
        servicePrices.put("Banho", new BigDecimal("25.00"));
        servicePrices.put("Banho e Tosquia Intima", new BigDecimal("35.00"));
        servicePrices.put("Banho e Tosquia Geral", new BigDecimal("50.00"));
        servicePrices.put("Pet Sitting", new BigDecimal("30.00"));
        servicePrices.put("Hosting", new BigDecimal("40.00"));
    }
    
    public BigDecimal getServicePrice(String serviceName) {
        return servicePrices.getOrDefault(serviceName, BigDecimal.ZERO);
    }
    
    public BigDecimal getServicePrice(String serviceName, Double weightKg) {
        if (weightKg == null || weightKg <= 0) {
            return switch (serviceName) {
                case "Banho" -> new BigDecimal("15.00");
                case "Banho e Tosquia Intima" -> new BigDecimal("20.00");
                case "Banho e Tosquia Geral", "Tosquia Completa" -> new BigDecimal("25.00");
                default -> getServicePrice(serviceName);
            };
        }
        double w = weightKg;

        return switch (serviceName) {
            case "Banho" -> priceBath(w);
            case "Banho e Tosquia Intima" -> priceBathIntimate(w);
            case "Tosquia Completa", "Banho e Tosquia Geral" -> priceFullTrim(w);
            default -> getServicePrice(serviceName);
        };
    }

    private BigDecimal priceBath(double w) {
        if (w <= 5) return new BigDecimal("10.00");
        if (w <= 10) return new BigDecimal("15.00");
        if (w <= 15) return new BigDecimal("20.00");
        return new BigDecimal("20.00");
    }

    private BigDecimal priceBathIntimate(double w) {
        if (w <= 5) return new BigDecimal("15.00");
        if (w <= 10) return new BigDecimal("20.00");
        if (w <= 15) return new BigDecimal("25.00");
        return new BigDecimal("25.00");
    }

    private BigDecimal priceFullTrim(double w) {
        if (w <= 5) return new BigDecimal("20.00");
        if (w <= 10) return new BigDecimal("25.00");
        if (w <= 15) return new BigDecimal("30.00");
        return new BigDecimal("30.00");
    }
}