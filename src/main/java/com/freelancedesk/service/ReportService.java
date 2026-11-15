package com.freelancedesk.service;

import com.freelancedesk.dao.ClientDao;
import com.freelancedesk.dao.InvoiceDao;
import com.freelancedesk.dao.PaymentDao;
import com.freelancedesk.model.Client;
import com.freelancedesk.model.Invoice;
import com.freelancedesk.model.InvoiceStatus;
import com.freelancedesk.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Builds the three reports. Each report is a label-to-value map, ready to print or export. */
public class ReportService {
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final ClientDao clientDao;

    public ReportService(InvoiceDao invoiceDao, PaymentDao paymentDao, ClientDao clientDao) {
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.clientDao = clientDao;
    }

    /** Outstanding balance per client, for invoices that still have a balance. */
    public Map<String, BigDecimal> clientOutstanding() throws SQLException {
        Map<Integer, String> names = clientDao.findAll().stream()
                .collect(Collectors.toMap(Client::getId, Client::getName));
        return invoiceDao.findAll().stream()
                .filter(invoice -> invoice.getBalance().signum() > 0)
                .collect(Collectors.groupingBy(
                        invoice -> names.get(invoice.getClientId()),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getBalance, BigDecimal::add)));
    }

    /** Payments received by one freelancer, grouped by month. */
    public Map<String, BigDecimal> monthlyEarnings(int freelancerId) throws SQLException {
        Map<Integer, Invoice> invoicesById = invoiceDao.findAll().stream()
                .collect(Collectors.toMap(Invoice::getId, Function.identity()));
        return paymentDao.findAll().stream()
                .filter(payment -> invoicesById.get(payment.getInvoiceId()).getFreelancerId() == freelancerId)
                .collect(Collectors.groupingBy(
                        payment -> YearMonth.from(payment.getPaymentDate()).toString(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));
    }

    /** Overall billing and collection figures. */
    public Map<String, BigDecimal> collectionSummary() throws SQLException {
        LocalDate today = LocalDate.now();
        List<Invoice> all = invoiceDao.findAll();

        Map<Boolean, Long> settled = all.stream().collect(Collectors.partitioningBy(
                invoice -> invoice.getStatus(today) == InvoiceStatus.PAID, Collectors.counting()));
        long overdue = all.stream()
                .filter(invoice -> invoice.getStatus(today) == InvoiceStatus.OVERDUE)
                .count();
        BigDecimal billed = total(all, Invoice::getTotal);
        BigDecimal collected = total(all, Invoice::getAmountPaid);

        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        summary.put("Invoices issued", BigDecimal.valueOf(all.size()));
        summary.put("Invoices fully paid", BigDecimal.valueOf(settled.get(true)));
        summary.put("Invoices still open", BigDecimal.valueOf(settled.get(false)));
        summary.put("Invoices overdue", BigDecimal.valueOf(overdue));
        summary.put("Total billed", billed);
        summary.put("Total collected", collected);
        summary.put("Total outstanding", billed.subtract(collected));
        return summary;
    }

    private BigDecimal total(List<Invoice> invoices, Function<Invoice, BigDecimal> amount) {
        return invoices.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
