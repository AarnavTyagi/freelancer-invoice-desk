package com.freelancedesk.service;

import com.freelancedesk.dao.BillableServiceDao;
import com.freelancedesk.dao.ClientDao;
import com.freelancedesk.dao.FreelancerDao;
import com.freelancedesk.dao.InvoiceDao;
import com.freelancedesk.exception.RecordNotFoundException;
import com.freelancedesk.exception.ValidationException;
import com.freelancedesk.model.BillableService;
import com.freelancedesk.model.Invoice;
import com.freelancedesk.model.InvoiceLine;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Builds invoices, assigns unique invoice numbers and computes totals. */
public class InvoiceService {
    private final InvoiceDao invoiceDao;
    private final FreelancerDao freelancerDao;
    private final ClientDao clientDao;
    private final BillableServiceDao serviceDao;

    public InvoiceService(InvoiceDao invoiceDao, FreelancerDao freelancerDao,
                          ClientDao clientDao, BillableServiceDao serviceDao) {
        this.invoiceDao = invoiceDao;
        this.freelancerDao = freelancerDao;
        this.clientDao = clientDao;
        this.serviceDao = serviceDao;
    }

    /** Copies the rate and tax of a catalogue service into a new invoice line. */
    public InvoiceLine buildLine(int freelancerId, int serviceId, int quantity)
            throws ValidationException, RecordNotFoundException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero.");
        }
        BillableService service = serviceDao.findById(serviceId)
                .orElseThrow(() -> new RecordNotFoundException("Service " + serviceId + " not found."));
        if (service.getFreelancerId() != freelancerId) {
            throw new ValidationException("Service " + serviceId + " belongs to another freelancer.");
        }
        return new InvoiceLine(service.getDescription(), quantity,
                service.getUnitRate(), service.getTaxPercent());
    }

    /** Synchronized so that two callers can never receive the same invoice number. */
    public synchronized Invoice createInvoice(int freelancerId, int clientId, LocalDate dueDate,
                                              List<InvoiceLine> lines)
            throws ValidationException, RecordNotFoundException, SQLException {
        freelancerDao.findById(freelancerId)
                .orElseThrow(() -> new RecordNotFoundException("Freelancer " + freelancerId + " not found."));
        clientDao.findById(clientId)
                .orElseThrow(() -> new RecordNotFoundException("Client " + clientId + " not found."));
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            throw new ValidationException("Due date cannot be before the issue date.");
        }
        if (lines.isEmpty()) {
            throw new ValidationException("An invoice needs at least one line.");
        }
        Invoice invoice = new Invoice(0, nextInvoiceNumber(today.getYear()), freelancerId, clientId,
                today, dueDate, lines, BigDecimal.ZERO);
        return invoiceDao.save(invoice);
    }

    public List<Invoice> getInvoices() throws SQLException {
        return invoiceDao.findAll().stream()
                .sorted(Comparator.comparing(Invoice::getDueDate))
                .toList();
    }

    public Invoice getByNumber(String invoiceNumber) throws RecordNotFoundException, SQLException {
        return invoiceDao.findByNumber(invoiceNumber)
                .orElseThrow(() -> new RecordNotFoundException("Invoice " + invoiceNumber + " not found."));
    }

    private String nextInvoiceNumber(int year) throws SQLException {
        String prefix = "INV-" + year + "-";
        return prefix + String.format("%04d", invoiceDao.countByPrefix(prefix) + 1);
    }
}
