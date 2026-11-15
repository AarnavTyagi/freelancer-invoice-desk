# Freelancer Invoice and Payment Desk

Console application in Java for freelancers to manage clients, create invoices, record payments and view collection reports. Built for Programming in Java (25CSE0202), project JAVA-P048.

## Features

- Freelancer profiles, client records and billable service descriptions
- Invoices with quantities, unit rates, tax and due dates, with unique invoice numbers (`INV-2026-0001`)
- Full or partial payments, with status tracking: Pending, Partially Paid, Paid, Overdue
- Reports: client-wise outstanding balances, monthly earnings, collection summary
- CSV export of all reports

## Requirements

- JDK 21
- Maven
- MySQL Community Server (8.x)
- VS Code with the Extension Pack for Java

## Setup

1. Create the database and tables:
   ```
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/sample_data.sql   # optional demo data
   ```
2. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and enter your MySQL user and password. This file is git-ignored.
3. Run the application:
   ```
   mvn compile exec:java
   ```
   Or open `Main.java` in VS Code and click Run.

CSV exports are written to the `exports/` folder.

## Project structure

```
freelancer-invoice-desk/
├── pom.xml
├── README.md
├── .gitignore
├── sql/
│   ├── schema.sql
│   └── sample_data.sql
├── docs/
│   └── PROJECT_REPORT.md
└── src/main/
    ├── java/com/freelancedesk/
    │   ├── Main.java
    │   ├── model/       Party, Freelancer, Client, BillableService,
    │   │                Invoice, InvoiceLine, Payment, InvoiceStatus
    │   ├── exception/   ValidationException, InvalidPaymentException,
    │   │                RecordNotFoundException
    │   ├── dao/         Repository<T, ID>, DatabaseConnection, FreelancerDao,
    │   │                ClientDao, BillableServiceDao, InvoiceDao, PaymentDao
    │   ├── service/     ProfileService, InvoiceService, PaymentService,
    │   │                ReportService, ReportExporter
    │   ├── util/        ConsoleInput
    │   └── ui/          MainMenu
    └── resources/
        └── db.properties.example
```

See `docs/PROJECT_REPORT.md` for the problem statement, design and test evidence.
