CREATE DATABASE IF NOT EXISTS freelancer_desk;
USE freelancer_desk;

CREATE TABLE IF NOT EXISTS freelancer (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    profession  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS client (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(120) NOT NULL UNIQUE,
    company  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS billable_service (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id INT NOT NULL,
    description   VARCHAR(200) NOT NULL,
    unit_rate     DECIMAL(10,2) NOT NULL,
    tax_percent   DECIMAL(5,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (freelancer_id) REFERENCES freelancer(id)
);

CREATE TABLE IF NOT EXISTS invoice (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    freelancer_id  INT NOT NULL,
    client_id      INT NOT NULL,
    issue_date     DATE NOT NULL,
    due_date       DATE NOT NULL,
    FOREIGN KEY (freelancer_id) REFERENCES freelancer(id),
    FOREIGN KEY (client_id) REFERENCES client(id)
);

CREATE TABLE IF NOT EXISTS invoice_line (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id  INT NOT NULL,
    description VARCHAR(200) NOT NULL,
    quantity    INT NOT NULL,
    unit_rate   DECIMAL(10,2) NOT NULL,
    tax_percent DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payment (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id   INT NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    method       VARCHAR(30) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoice(id)
);
