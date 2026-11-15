package com.freelancedesk;

import com.freelancedesk.dao.BillableServiceDao;
import com.freelancedesk.dao.ClientDao;
import com.freelancedesk.dao.FreelancerDao;
import com.freelancedesk.dao.InvoiceDao;
import com.freelancedesk.dao.PaymentDao;
import com.freelancedesk.service.InvoiceService;
import com.freelancedesk.service.PaymentService;
import com.freelancedesk.service.ProfileService;
import com.freelancedesk.service.ReportExporter;
import com.freelancedesk.service.ReportService;
import com.freelancedesk.ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        FreelancerDao freelancerDao = new FreelancerDao();
        ClientDao clientDao = new ClientDao();
        BillableServiceDao serviceDao = new BillableServiceDao();
        InvoiceDao invoiceDao = new InvoiceDao();
        PaymentDao paymentDao = new PaymentDao();

        ReportService reportService = new ReportService(invoiceDao, paymentDao, clientDao);
        new MainMenu(
                new ProfileService(freelancerDao, clientDao, serviceDao),
                new InvoiceService(invoiceDao, freelancerDao, clientDao, serviceDao),
                new PaymentService(invoiceDao, paymentDao),
                reportService,
                new ReportExporter(reportService)
        ).run();
    }
}
