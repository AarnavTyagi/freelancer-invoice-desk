package com.freelancedesk.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** One billed service on an invoice: quantity, unit rate and tax. */
public class InvoiceLine {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final String description;
    private final int quantity;
    private final BigDecimal unitRate;
    private final BigDecimal taxPercent;

    public InvoiceLine(String description, int quantity, BigDecimal unitRate, BigDecimal taxPercent) {
        this.description = description;
        this.quantity = quantity;
        this.unitRate = unitRate;
        this.taxPercent = taxPercent;
    }

    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitRate() { return unitRate; }
    public BigDecimal getTaxPercent() { return taxPercent; }

    public BigDecimal getNetAmount() {
        return unitRate.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTaxAmount() {
        return getNetAmount().multiply(taxPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLineTotal() {
        return getNetAmount().add(getTaxAmount());
    }
}
