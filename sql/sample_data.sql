USE freelancer_desk;

INSERT INTO freelancer (name, email, profession) VALUES
    ('Riya Sharma', 'riya.sharma@example.com', 'Web Developer'),
    ('Karan Mehta', 'karan.mehta@example.com', 'Graphic Designer');

INSERT INTO client (name, email, company) VALUES
    ('Anil Verma', 'anil.verma@example.com', 'Verma Traders'),
    ('Sunita Rao', 'sunita.rao@example.com', 'Rao Textiles'),
    ('Dev Malhotra', 'dev.malhotra@example.com', 'Malhotra Foods');

INSERT INTO billable_service (freelancer_id, description, unit_rate, tax_percent) VALUES
    (1, 'Website development (per page)', 3000.00, 18.00),
    (1, 'Website maintenance (per hour)', 800.00, 18.00),
    (2, 'Logo design', 5000.00, 18.00),
    (2, 'Social media post design', 600.00, 12.00);
