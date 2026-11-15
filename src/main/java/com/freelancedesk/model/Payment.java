package com.freelancedesk.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    private int id;
    private final int invoiceId;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final String method;

    public Payment(int id, int invoiceId, BigDecimal amount, LocalDate paymentDate, String method) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getInvoiceId() { return invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getMethod() { return method; }
}
