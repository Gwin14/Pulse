package com.pulse.domain.service;

import com.pulse.domain.model.SalesAnalysis;
import com.pulse.domain.model.SalesData;
import com.pulse.domain.model.SalesMetric;
import com.pulse.domain.port.SalesAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação heurística do serviço de análise de vendas
 * Usa algoritmos estatísticos simples quando IA não está disponível
 */
@Service
public class HeuristicSalesAnalysisService implements SalesAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(HeuristicSalesAnalysisService.class);
    private static final double FALLBACK_SCORE = 0.55;
    private static final double MAX_VOLATILITY = 1.0;
    private static final double VOLATILITY_MULTIPLIER = 2.0;

    @Override
    public SalesAnalysis analyzeSales(SalesData salesData) {
        logger.info("Executando análise heurística para {} métricas", salesData.getMetrics().size());
        
        Map<DayOfWeek, List<SalesMetric>> metricsByDayOfWeek = groupMetricsByDayOfWeek(salesData.getMetrics());
        DayOfWeek bestDayOfWeek = findBestDayOfWeek(metricsByDayOfWeek);
        SalesMetric bestMetric = findBestMetric(salesData.getMetrics());
        double volatility = calculateVolatility(salesData.getMetrics());

        return new SalesAnalysis(
            bestMetric.getDate(),
            bestDayOfWeek,
            FALLBACK_SCORE,
            "Análise heurística baseada em média de receita por dia da semana",
            volatility,
            createHeuristicInsights(metricsByDayOfWeek, bestDayOfWeek)
        );
    }

    private Map<DayOfWeek, List<SalesMetric>> groupMetricsByDayOfWeek(List<SalesMetric> metrics) {
        return metrics.stream()
            .collect(Collectors.groupingBy(metric -> metric.getDate().getDayOfWeek()));
    }

    private DayOfWeek findBestDayOfWeek(Map<DayOfWeek, List<SalesMetric>> metricsByDayOfWeek) {
        return metricsByDayOfWeek.entrySet().stream()
            .max(Comparator.comparingDouble(entry -> calculateAverageRevenue(entry.getValue())))
            .map(Map.Entry::getKey)
            .orElse(DayOfWeek.MONDAY);
    }

    private double calculateAverageRevenue(List<SalesMetric> metrics) {
        return metrics.stream()
            .mapToDouble(SalesMetric::getRevenue)
            .average()
            .orElse(0.0);
    }

    private SalesMetric findBestMetric(List<SalesMetric> metrics) {
        return metrics.stream()
            .max(Comparator.comparingDouble(SalesMetric::getRevenue))
            .orElseThrow(() -> new RuntimeException("Nenhuma métrica encontrada"));
    }

    private double calculateVolatility(List<SalesMetric> metrics) {
        if (metrics.size() < 2) return 0.0;
        
        double meanRevenue = calculateAverageRevenue(metrics);
        if (meanRevenue == 0.0) return 0.0;
        
        double variance = metrics.stream()
            .mapToDouble(metric -> Math.pow(metric.getRevenue() - meanRevenue, 2))
            .average()
            .orElse(0.0);
        
        double standardDeviation = Math.sqrt(variance);
        return Math.min(MAX_VOLATILITY, standardDeviation / (meanRevenue * VOLATILITY_MULTIPLIER));
    }

    private List<String> createHeuristicInsights(Map<DayOfWeek, List<SalesMetric>> metricsByDayOfWeek, DayOfWeek bestDay) {
        List<String> insights = new ArrayList<>();
        
        insights.add("Análise baseada em " + metricsByDayOfWeek.size() + " dias da semana");
        insights.add("Melhor dia identificado: " + bestDay.toString());
        
        if (metricsByDayOfWeek.size() >= 3) {
            insights.add("Padrão de vendas analisado com dados suficientes");
        } else {
            insights.add("Análise limitada - dados insuficientes para padrões");
        }
        
        insights.add("Sem IA disponível - usando algoritmo heurístico");
        
        return insights.subList(0, Math.min(insights.size(), 5));
    }
} 