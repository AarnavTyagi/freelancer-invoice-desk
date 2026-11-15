package com.freelancedesk.model;

import java.math.BigDecimal;

/** A service a freelancer offers, with its default rate and tax. */
public class BillableService {
    private int id;
    private final int freelancerId;
    private final String description;
    private final BigDecimal unitRate;
    private final BigDecimal taxPercent;

    public BillableService(int id, int freelancerId, String description,
                           BigDecimal unitRate, BigDecimal taxPercent) {
        this.id = id;
        this.freelancerId = freelancerId;
        this.description = description;
        this.unitRate = unitRate;
        this.taxPercent = taxPercent;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFreelancerId() { return freelancerId; }
    public String getDescription() { return description; }
    public BigDecimal getUnitRate() { return unitRate; }
    public BigDecimal getTaxPercent() { return taxPercent; }
}
