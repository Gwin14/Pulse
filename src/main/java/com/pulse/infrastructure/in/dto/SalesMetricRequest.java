package com.pulse.infrastructure.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SalesMetricRequest {
    
    @NotBlank(message = "Data é obrigatória")
    private String date;
    
    @NotNull(message = "Receita não pode ser nula")
    @PositiveOrZero(message = "Receita deve ser maior ou igual a zero")
    private double revenue;
    
    @NotNull(message = "Número de transações não pode ser nulo")
    @PositiveOrZero(message = "Número de transações deve ser maior ou igual a zero")
    private int transactions;
    
    @PositiveOrZero(message = "Ticket médio deve ser maior ou igual a zero")
    private double ticket = 0.0;
} 