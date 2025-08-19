package com.pulse.application.service;

import com.pulse.domain.model.SalesAnalysis;
import com.pulse.domain.model.SalesData;
import com.pulse.domain.port.AiAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SalesAnalysisUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(SalesAnalysisUseCase.class);
    
    private final AiAnalysisService aiAnalysisService;

    public SalesAnalysisUseCase(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    public SalesAnalysis execute(SalesData salesData) {
        logger.info("Iniciando análise de vendas para período: {} a {}", 
                   salesData.getPeriodStart(), salesData.getPeriodEnd());
        
        try {
            logger.info("Usando análise com IA");
            return aiAnalysisService.analyzeWithAi(salesData);
        } catch (Exception e) {
            logger.warn("Falha na análise com IA: {}", e.getMessage());
        }
        return null;
    }
} 