package com.pulse.infrastructure.out.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO de resposta para análise de vendas
 * Mapeia o JSON retornado pelo controller
 */
@Data
public class SalesAnalysisResponse {
    
    private String best_day_date;
    private String best_day_weekday;
    private double score;
    private String reason;
    private double volatility;
    private List<String> insights;
} 