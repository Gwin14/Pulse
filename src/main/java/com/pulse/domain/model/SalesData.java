package com.pulse.domain.model;

import java.time.LocalDate;
import java.util.List;

public class SalesData {
    private final String timezone;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final List<SalesMetric> metrics;

    public SalesData(String timezone, LocalDate periodStart, LocalDate periodEnd, List<SalesMetric> metrics) {
        this.timezone = validateTimezone(timezone);
        this.periodStart = validatePeriodStart(periodStart);
        this.periodEnd = validatePeriodEnd(periodEnd);
        this.metrics = validateMetrics(metrics);
        validatePeriodConsistency();
    }

    public String getTimezone() { return timezone; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public List<SalesMetric> getMetrics() { return metrics; }

    private String validateTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            throw new IllegalArgumentException("Timezone é obrigatório");
        }
        return timezone.trim();
    }

    private LocalDate validatePeriodStart(LocalDate periodStart) {
        if (periodStart == null) {
            throw new IllegalArgumentException("Data de início do período é obrigatória");
        }
        return periodStart;
    }

    private LocalDate validatePeriodEnd(LocalDate periodEnd) {
        if (periodEnd == null) {
            throw new IllegalArgumentException("Data de fim do período é obrigatória");
        }
        return periodEnd;
    }

    private List<SalesMetric> validateMetrics(List<SalesMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("Métricas não podem ser vazias");
        }
        if (metrics.size() < 2) {
            throw new IllegalArgumentException("É necessário pelo menos 2 métricas para análise");
        }
        return metrics;
    }

    private void validatePeriodConsistency() {
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        
        for (SalesMetric metric : metrics) {
            if (metric.getDate().isBefore(periodStart) || metric.getDate().isAfter(periodEnd)) {
                throw new IllegalArgumentException(
                    "Métrica de " + metric.getDate() + " está fora do período especificado"
                );
            }
        }
    }

    @Override
    public String toString() {
        return "SalesData{" +
                "timezone='" + timezone + '\'' +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                ", metrics=" + metrics +
                '}';
    }
} 