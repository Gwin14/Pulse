package com.pulse.pulse.domain;

import java.util.List;

import lombok.Data;

@Data
public class InputData {
    private String timezone;
    private String period_start;
    private String period_end;
    private List<Metric> metrics;
}
