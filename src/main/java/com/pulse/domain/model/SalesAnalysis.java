package com.pulse.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class SalesAnalysis {
    private final LocalDate bestDayDate;
    private final DayOfWeek bestDayWeekday;
    private final double score;
    private final String reason;
    private final double volatility;
    private final List<String> insights;

    public SalesAnalysis(LocalDate bestDayDate, DayOfWeek bestDayWeekday, double score, 
                        String reason, double volatility, List<String> insights) {
        this.bestDayDate = bestDayDate;
        this.bestDayWeekday = bestDayWeekday;
        this.score = validateScore(score);
        this.reason = validateReason(reason);
        this.volatility = validateVolatility(volatility);
        this.insights = validateInsights(insights);
    }

    public LocalDate getBestDayDate() { return bestDayDate; }
    public DayOfWeek getBestDayWeekday() { return bestDayWeekday; }
    public double getScore() { return score; }
    public String getReason() { return reason; }
    public double getVolatility() { return volatility; }
    public List<String> getInsights() { return insights; }

    private double validateScore(double score) {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Score deve estar entre 0.0 e 1.0");
        }
        return score;
    }

    private String validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason não pode ser vazio");
        }

        return reason.trim();
    }

    private double validateVolatility(double volatility) {
        if (volatility < 0.0 || volatility > 1.0) {
            throw new IllegalArgumentException("Volatility deve estar entre 0.0 e 1.0");
        }
        return volatility;
    }

    private List<String> validateInsights(List<String> insights) {
        if (insights == null || insights.isEmpty()) {
            throw new IllegalArgumentException("Insights não pode ser vazio");
        }
        if (insights.size() < 2 || insights.size() > 5) {
            throw new IllegalArgumentException("Insights deve ter entre 2 e 5 itens");
        }
        
        return insights.stream()
            .map(insight -> {
                if (insight == null || insight.trim().isEmpty()) {
                    throw new IllegalArgumentException("Insight não pode ser vazio");
                }

                return insight.trim();
            })
            .toList();
    }

    @Override
    public String toString() {
        return "SalesAnalysis{" +
                "bestDayDate=" + bestDayDate +
                ", bestDayWeekday=" + bestDayWeekday +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                ", volatility=" + volatility +
                ", insights=" + insights +
                '}';
    }
} 