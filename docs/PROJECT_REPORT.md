# Project Report: Freelancer Invoice and Payment Desk

Course: Programming in Java (25CSE0202) | Project: JAVA-P048

## 1. Problem statement

Freelancers often track clients, invoices and payments in scattered notes and spreadsheets. Invoice numbers get duplicated, totals are calculated by hand, and it is hard to see which clients still owe money. This project provides a single Java application where a freelancer records clients and services, issues invoices with correct totals, records full or partial payments, and sees what is outstanding.

## 2. Objectives

1. Maintain freelancer profiles, client records and billable service descriptions.
2. Create invoices with service quantities, unit rates, tax and due dates.
3. Generate unique invoice numbers and calculate invoice totals.
4. Record full or partial payments and track Pending, Partially Paid, Paid and Overdue states.
5. Produce client-wise outstanding balances, monthly earnings and a collection summary.

## 3. Scope

In scope: single-user console application, MySQL persistence, CSV report export.
Out of scope: user login, PDF invoices, multiple currencies, email sending, graphical interface.

## 4. Design

Layered structure, each layer only talks to the one below it:

`ui (MainMenu)` -> `service` -> `dao` -> `MySQL`

- **model**: plain domain classes. `Party` is the abstract base of `Freelancer` and `Client`. `Invoice` derives its totals, balance and status from its lines and paid amount, so they can never go out of sync.
- **dao**: one DAO per table behind a generic `Repository<T, ID>` interface. All SQL uses `PreparedStatement`.
- **service**: business rules and validation. `ReportService` produces the reports.
- **ui**: console menu and input handling.

### Database tables

`freelancer`, `client`, `billable_service`, `invoice`, `invoice_line`, `payment` (see `sql/schema.sql`).

### Invoice status rules

| Condition | Status |
|---|---|
| Balance is zero | PAID |
| Balance remains and due date has passed | OVERDUE |
| Balance remains, something paid, not yet due | PARTIALLY_PAID |
| Nothing paid, not yet due | PENDING |

## 5. Course concepts and where they are used

| Concept (CHO) | Where |
|---|---|
| OOP: inheritance, abstraction, polymorphism, encapsulation | `Party` with `Freelancer` and `Client`, abstract `describe()` |
| Strings, wrapper classes | email validation with regex, `Integer.parseInt`, invoice number formatting |
| Custom exceptions, try-catch-finally, throws | `ValidationException`, `InvalidPaymentException`, `RecordNotFoundException`; handled in `MainMenu.run()` |
| Collections: ArrayList, HashMap, TreeMap, LinkedHashMap | invoice lines and results in `ArrayList`; id-to-object lookups in `HashMap`; sorted reports in `TreeMap`; `LinkedHashMap` for the summary |
| Comparator and sorting | invoices sorted by due date in `InvoiceService.getInvoices()` |
| Threads and executors | `ReportExporter` exports three reports in parallel using `ExecutorService` and `Callable`/`Future`; `createInvoice` is `synchronized` so invoice numbers stay unique |
| JDBC | `DatabaseConnection`, DAOs, `PreparedStatement`, `ResultSet` mapping to objects, batch insert |
| JDBC transactions | `InvoiceDao.save()` commits the invoice and its lines together or rolls back |
| Lambdas and method references | `forEach` printing, `Comparator.comparing(Invoice::getDueDate)`, `BigDecimal::add` |
| Stream API and Collectors | `ReportService`: `filter`, `groupingBy`, `reducing`, `partitioningBy`, `counting`; `Invoice` totals |
| Generics | `Repository<T, ID>` implemented by every DAO |
| File handling | `ReportExporter` writes CSV with `BufferedWriter` inside try-with-resources |
| Date-Time | `LocalDate`, `YearMonth` for due dates and monthly grouping |

## 6. Test evidence

Run each case and attach a screenshot of the console output.

| # | Test | Input | Expected result |
|---|---|---|---|
| 1 | Add freelancer | valid name, email, profession | Added, shown in option 4 |
| 2 | Duplicate email | same email again | "already exists" error, no crash |
| 3 | Invalid email | `abc` | "Enter a valid email address." |
| 4 | Add service with bad tax | tax 150 | Tax must be between 0 and 100 |
| 5 | Create invoice | 2 lines, future due date | Number `INV-<year>-0001`, correct subtotal, tax, total |
| 6 | Second invoice | any valid invoice | Number ends `0002` |
| 7 | Past due date | yesterday | "Due date cannot be before the issue date." |
| 8 | Service of another freelancer | wrong service id | Validation error |
| 9 | Partial payment | less than total | Status PARTIALLY_PAID, balance reduced |
| 10 | Overpayment | more than balance | InvalidPaymentException message |
| 11 | Final payment | exact balance | Status PAID, balance 0 |
| 12 | Overdue | invoice with passed due date and balance | Status OVERDUE in the invoice list |
| 13 | Reports | options 8.1, 8.2, 8.3 | Values match invoices and payments entered |
| 14 | CSV export | option 9 | Three files in `exports/` |
| 15 | Database down | stop MySQL, use any option | "Database error" message, program keeps running |

To test case 12, insert an invoice with an earlier due date directly in MySQL, because the application blocks past due dates.

## 7. Viva preparation

Be ready to explain: why `BigDecimal` is used for money, why status is derived instead of stored, why `PreparedStatement` prevents SQL injection, how the transaction in `InvoiceDao.save()` works, why `createInvoice` is synchronized, and how the `groupingBy` collectors in `ReportService` produce each report.
