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
}