package com.pulse.domain.port;

import com.pulse.domain.model.SalesAnalysis;
import com.pulse.domain.model.SalesData;

public interface AiAnalysisService {
    
    SalesAnalysis analyzeWithAi(SalesData salesData);
    
} 