package com.pulse.domain.port;

import com.pulse.domain.model.SalesAnalysis;
import com.pulse.domain.model.SalesData;

/**
 * Porta de saída para análise de vendas
 * Define o contrato para serviços que realizam análise de dados de vendas
 */
public interface SalesAnalysisService {
    
    /**
     * Analisa dados de vendas e retorna a melhor data para promoções
     * 
     * @param salesData dados de vendas para análise
     * @return análise de vendas com recomendação
     * @throws RuntimeException se houver erro na análise
     */
    SalesAnalysis analyzeSales(SalesData salesData);
} 