package com.pulse.infrastructure.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesAnalysisRequest {
    
    @NotBlank(message = "Timezone é obrigatório")
    private String timezone;
    
    @NotBlank(message = "Data de início do período é obrigatória")
    private String period_start;
    
    @NotBlank(message = "Data de fim do período é obrigatória")
    private String period_end;
    
    @NotNull(message = "Métricas não podem ser nulas")
    @NotEmpty(message = "Lista de métricas não pode estar vazia")
    private List<SalesMetricRequest> metrics;
} 