package com.freelancedesk.service;

import com.freelancedesk.dao.InvoiceDao;
import com.freelancedesk.dao.PaymentDao;
import com.freelancedesk.exception.InvalidPaymentException;
import com.freelancedesk.exception.RecordNotFoundException;
import com.freelancedesk.model.Invoice;
import com.freelancedesk.model.InvoiceStatus;
import com.freelancedesk.model.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;

/** Records full or partial payments against invoices. */
public class PaymentService {
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;

    public PaymentService(InvoiceDao invoiceDao, PaymentDao paymentDao) {
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
    }

    /** Saves the payment and returns the invoice status after it. */
    public InvoiceStatus recordPayment(String invoiceNumber, BigDecimal amount, String method)
            throws InvalidPaymentException, RecordNotFoundException, SQLException {
        Invoice invoice = find(invoiceNumber);
        BigDecimal payment = amount.setScale(2, RoundingMode.HALF_UP);
        if (payment.signum() <= 0) {
            throw new InvalidPaymentException("Payment must be greater than zero.");
        }
        if (payment.compareTo(invoice.getBalance()) > 0) {
            throw new InvalidPaymentException("Payment " + payment
                    + " is more than the outstanding balance " + invoice.getBalance() + ".");
        }
        paymentDao.save(new Payment(0, invoice.getId(), payment, LocalDate.now(), method));
        return find(invoiceNumber).getStatus(LocalDate.now());
    }

    private Invoice find(String invoiceNumber) throws RecordNotFoundException, SQLException {
        return invoiceDao.findByNumber(invoiceNumber)
                .orElseThrow(() -> new RecordNotFoundException("Invoice " + invoiceNumber + " not found."));
    }
}
