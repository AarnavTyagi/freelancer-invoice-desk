package com.freelancedesk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** An invoice with its lines. Totals and status are derived, never stored. */
public class Invoice {
    private int id;
    private final String invoiceNumber;
    private final int freelancerId;
    private final int clientId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final List<InvoiceLine> lines;
    private final BigDecimal amountPaid;

    public Invoice(int id, String invoiceNumber, int freelancerId, int clientId,
                   LocalDate issueDate, LocalDate dueDate,
                   List<InvoiceLine> lines, BigDecimal amountPaid) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.freelancerId = freelancerId;
        this.clientId = clientId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.lines = List.copyOf(lines);
        this.amountPaid = amountPaid;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public int getFreelancerId() { return freelancerId; }
    public int getClientId() { return clientId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public List<InvoiceLine> getLines() { return lines; }
    public BigDecimal getAmountPaid() { return amountPaid; }

    public BigDecimal getSubtotal() {
        return lines.stream().map(InvoiceLine::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTaxTotal() {
        return lines.stream().map(InvoiceLine::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxTotal());
    }

    public BigDecimal getBalance() {
        return getTotal().subtract(amountPaid);
    }

    public InvoiceStatus getStatus(LocalDate today) {
        if (getBalance().signum() <= 0) {
            return InvoiceStatus.PAID;
        }
        if (today.isAfter(dueDate)) {
            return InvoiceStatus.OVERDUE;
        }
        return amountPaid.signum() > 0 ? InvoiceStatus.PARTIALLY_PAID : InvoiceStatus.PENDING;
    }
}
