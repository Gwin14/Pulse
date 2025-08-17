package com.pulse.pulse.domain;

import lombok.Data;

@Data
public class Metric {
    private String date;
    private double revenue;
    private int transactions;
    private double ticket;
}
