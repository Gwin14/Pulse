package com.pulse.infrastructure.in.mapper;

import com.pulse.domain.model.SalesData;
import com.pulse.domain.model.SalesMetric;
import com.pulse.infrastructure.in.dto.SalesAnalysisRequest;
import com.pulse.infrastructure.in.dto.SalesMetricRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SalesDataMapper {
    
    public SalesData toDomain(SalesAnalysisRequest request) {
        List<SalesMetric> metrics = request.getMetrics().stream()
            .map(this::toMetric)
            .toList();
            
        return new SalesData(
            request.getTimezone(),
            LocalDate.parse(request.getPeriod_start()),
            LocalDate.parse(request.getPeriod_end()),
            metrics
        );
    }
    
    private SalesMetric toMetric(SalesMetricRequest request) {
        double ticket = request.getTicket();
        if (ticket == 0.0 && request.getTransactions() > 0) {
            ticket = request.getRevenue() / request.getTransactions();
        }
        
        return new SalesMetric(
            LocalDate.parse(request.getDate()),
            request.getRevenue(),
            request.getTransactions(),
            ticket
        );
    }
} 