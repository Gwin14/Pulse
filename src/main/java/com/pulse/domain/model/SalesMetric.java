package com.pulse.domain.model;

import java.time.LocalDate;

public class SalesMetric {  
    private final LocalDate date;
    private final double revenue;
    private final int transactions;
    private final double ticket;

    public SalesMetric(LocalDate date, double revenue, int transactions, double ticket) {
        this.date = validateDate(date);
        this.revenue = validateRevenue(revenue);
        this.transactions = validateTransactions(transactions);
        this.ticket = validateTicket(ticket);
    }

    public LocalDate getDate() { return date; }
    public double getRevenue() { return revenue; }
    public int getTransactions() { return transactions; }
    public double getTicket() { return ticket; }

    private LocalDate validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        return date;
    }

    private double validateRevenue(double revenue) {
        if (revenue < 0.0) {
            throw new IllegalArgumentException("Receita deve ser maior ou igual a zero");
        }
        return revenue;
    }

    private int validateTransactions(int transactions) {
        if (transactions < 0) {
            throw new IllegalArgumentException("Número de transações deve ser maior ou igual a zero");
        }
        return transactions;
    }

    private double validateTicket(double ticket) {
        if (ticket < 0.0) {
            throw new IllegalArgumentException("Ticket médio deve ser maior ou igual a zero");
        }
        return ticket;
    }

    @Override
    public String toString() {
        return "SalesMetric{" +
                "date=" + date +
                ", revenue=" + revenue +
                ", transactions=" + transactions +
                ", ticket=" + ticket +
                '}';
    }
} 