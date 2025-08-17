package com.pulse.pulse.domain;

import java.util.List;

import lombok.Data;

@Data
public class PulseResponse {
    private String best_day_date;
    private String best_day_weekday;
    private double score;
    private String reason;
    private double volatility;
    private List<String> insights;
}
