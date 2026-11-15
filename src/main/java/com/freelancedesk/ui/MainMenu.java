package com.freelancedesk.ui;

import com.freelancedesk.exception.RecordNotFoundException;
import com.freelancedesk.exception.ValidationException;
import com.freelancedesk.model.Client;
import com.freelancedesk.model.Invoice;
import com.freelancedesk.model.InvoiceLine;
import com.freelancedesk.model.InvoiceStatus;
import com.freelancedesk.service.InvoiceService;
import com.freelancedesk.service.PaymentService;
import com.freelancedesk.service.ProfileService;
import com.freelancedesk.service.ReportExporter;
import com.freelancedesk.service.ReportService;
import com.freelancedesk.util.ConsoleInput;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Console menu that connects the user to the services. */
public class MainMenu {
    private final ConsoleInput in = new ConsoleInput();
    private final ProfileService profiles;
    private final InvoiceService invoices;
    private final PaymentService payments;
    private final ReportService reports;
    private final ReportExporter exporter;

    public MainMenu(ProfileService profiles, InvoiceService invoices, PaymentService payments,
                    ReportService reports, ReportExporter exporter) {
        this.profiles = profiles;
        this.invoices = invoices;
        this.payments = payments;
        this.reports = reports;
        this.exporter = exporter;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = in.readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> addFreelancer();
                    case 2 -> addClient();
                    case 3 -> addService();
                    case 4 -> viewDirectory();
                    case 5 -> createInvoice();
                    case 6 -> viewInvoices();
                    case 7 -> recordPayment();
                    case 8 -> showReport();
                    case 9 -> exportReports();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (ValidationException | RecordNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Freelancer Invoice and Payment Desk ===");
        System.out.println("1. Add freelancer");
        System.out.println("2. Add client");
        System.out.println("3. Add billable service");
        System.out.println("4. View freelancers and clients");
        System.out.println("5. Create invoice");
        System.out.println("6. View invoices");
        System.out.println("7. Record payment");
        System.out.println("8. Reports");
        System.out.println("9. Export reports to CSV");
        System.out.println("0. Exit");
    }

    private void addFreelancer() throws ValidationException, SQLException {
        String name = in.readText("Name: ");
        String email = in.readText("Email: ");
        String profession = in.readText("Profession: ");
        System.out.println("Added " + profiles.addFreelancer(name, email, profession).describe());
    }

    private void addClient() throws ValidationException, SQLException {
        String name = in.readText("Name: ");
        String email = in.readText("Email: ");
        String company = in.readText("Company: ");
        System.out.println("Added " + profiles.addClient(name, email, company).describe());
    }

    private void addService() throws ValidationException, RecordNotFoundException, SQLException {
        int freelancerId = in.readInt("Freelancer ID: ");
        String description = in.readText("Service description: ");
        BigDecimal rate = in.readDecimal("Unit rate: ");
        BigDecimal tax = in.readDecimal("Tax percent: ");
        profiles.addService(freelancerId, description, rate, tax);
        System.out.println("Service added.");
    }

    private void viewDirectory() throws SQLException {
        System.out.println("-- Freelancers --");
        profiles.getFreelancers().forEach(f -> System.out.println(f.describe()));
        System.out.println("-- Clients --");
        profiles.getClients().forEach(c -> System.out.println(c.describe()));
    }

    private void printServices(int freelancerId) throws SQLException {
        System.out.println("-- Services offered --");
        profiles.getServicesOf(freelancerId).forEach(s -> System.out.printf(
                "[%d] %s | rate %s | tax %s%%%n",
                s.getId(), s.getDescription(), s.getUnitRate(), s.getTaxPercent()));
    }

    private void createInvoice() throws ValidationException, RecordNotFoundException, SQLException {
        viewDirectory();
        int freelancerId = in.readInt("Freelancer ID: ");
        int clientId = in.readInt("Client ID: ");
        LocalDate dueDate = in.readDate("Due date");

        List<InvoiceLine> lines = new ArrayList<>();
        do {
            printServices(freelancerId);
            int serviceId = in.readInt("Service ID: ");
            int quantity = in.readInt("Quantity: ");
            lines.add(invoices.buildLine(freelancerId, serviceId, quantity));
        } while (in.confirm("Add another line?"));

        Invoice invoice = invoices.createInvoice(freelancerId, clientId, dueDate, lines);
        printInvoice(invoice, profiles.clientsById());
    }

    private void viewInvoices() throws SQLException, RecordNotFoundException {
        Map<Integer, Client> clients = profiles.clientsById();
        LocalDate today = LocalDate.now();
        System.out.printf("%-14s %-18s %-11s %10s %10s %10s  %s%n",
                "Invoice", "Client", "Due", "Total", "Paid", "Balance", "Status");
        invoices.getInvoices().forEach(i -> System.out.printf("%-14s %-18s %-11s %10s %10s %10s  %s%n",
                i.getInvoiceNumber(), clients.get(i.getClientId()).getName(), i.getDueDate(),
                i.getTotal(), i.getAmountPaid(), i.getBalance(), i.getStatus(today)));

        String number = in.readText("Invoice number for details (blank to skip): ");
        if (!number.isEmpty()) {
            printInvoice(invoices.getByNumber(number), clients);
        }
    }

    private void printInvoice(Invoice invoice, Map<Integer, Client> clients) {
        System.out.println();
        System.out.printf("Invoice %s | Client: %s | Issued: %s | Due: %s%n",
                invoice.getInvoiceNumber(), clients.get(invoice.getClientId()).getName(),
                invoice.getIssueDate(), invoice.getDueDate());
        invoice.getLines().forEach(l -> System.out.printf("  %-30s %3d x %9s  tax %5s%%  = %10s%n",
                l.getDescription(), l.getQuantity(), l.getUnitRate(), l.getTaxPercent(), l.getLineTotal()));
        System.out.printf("  Subtotal %s | Tax %s | Total %s | Paid %s | Balance %s | %s%n",
                invoice.getSubtotal(), invoice.getTaxTotal(), invoice.getTotal(),
                invoice.getAmountPaid(), invoice.getBalance(), invoice.getStatus(LocalDate.now()));
    }

    private void recordPayment() throws ValidationException, RecordNotFoundException, SQLException {
        String number = in.readText("Invoice number: ");
        BigDecimal amount = in.readDecimal("Amount paid: ");
        String method = in.readText("Payment method (for example UPI, Bank transfer, Cash): ");
        InvoiceStatus status = payments.recordPayment(number, amount, method);
        System.out.println("Payment recorded. Invoice is now " + status + ".");
    }

    private void showReport() throws SQLException {
        System.out.println("1. Client-wise outstanding balances");
        System.out.println("2. Monthly earnings of a freelancer");
        System.out.println("3. Payment collection summary");
        switch (in.readInt("Choose a report: ")) {
            case 1 -> printReport("Outstanding by client", reports.clientOutstanding());
            case 2 -> printReport("Monthly earnings",
                    reports.monthlyEarnings(in.readInt("Freelancer ID: ")));
            case 3 -> printReport("Collection summary", reports.collectionSummary());
            default -> System.out.println("Invalid report.");
        }
    }

    private void printReport(String title, Map<String, BigDecimal> data) {
        System.out.println("-- " + title + " --");
        if (data.isEmpty()) {
            System.out.println("No data.");
        }
        data.forEach((label, value) -> System.out.printf("%-25s %s%n", label, value.toPlainString()));
    }

    private void exportReports() throws IOException {
        int freelancerId = in.readInt("Freelancer ID for the monthly earnings report: ");
        List<Path> files = exporter.exportAll(freelancerId);
        files.forEach(file -> System.out.println("Written: " + file));
    }
}
