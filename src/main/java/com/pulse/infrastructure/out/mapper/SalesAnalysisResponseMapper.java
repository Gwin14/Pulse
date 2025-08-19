package com.pulse.infrastructure.out.mapper;

import com.pulse.domain.model.SalesAnalysis;
import com.pulse.infrastructure.out.dto.SalesAnalysisResponse;
import org.springframework.stereotype.Component;

@Component
public class SalesAnalysisResponseMapper {
    
    public SalesAnalysisResponse toResponse(SalesAnalysis analysis) {
        SalesAnalysisResponse response = new SalesAnalysisResponse();
        response.setBest_day_date(analysis.getBestDayDate().toString());
        response.setBest_day_weekday(analysis.getBestDayWeekday().toString());
        response.setScore(analysis.getScore());
        response.setReason(analysis.getReason());
        response.setVolatility(analysis.getVolatility());
        response.setInsights(analysis.getInsights());
        return response;
    }
} 