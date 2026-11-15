package com.freelancedesk.dao;

import com.freelancedesk.model.Invoice;
import com.freelancedesk.model.InvoiceLine;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDao implements Repository<Invoice, Integer> {

    private static final String BASE_SELECT =
            "SELECT i.*, COALESCE(SUM(p.amount), 0) AS paid "
                    + "FROM invoice i LEFT JOIN payment p ON p.invoice_id = i.id ";
    private static final String GROUP_ORDER = " GROUP BY i.id ORDER BY i.id";

    /** Saves the invoice and all its lines as one transaction. */
    @Override
    public Invoice save(Invoice invoice) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                insertHeader(con, invoice);
                insertLines(con, invoice);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return invoice;
    }

    @Override
    public Optional<Invoice> findById(Integer id) throws SQLException {
        return query("WHERE i.id = ?", id).stream().findFirst();
    }

    public Optional<Invoice> findByNumber(String invoiceNumber) throws SQLException {
        return query("WHERE i.invoice_number = ?", invoiceNumber).stream().findFirst();
    }

    @Override
    public List<Invoice> findAll() throws SQLException {
        return query("");
    }

    public int countByPrefix(String prefix) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoice WHERE invoice_number LIKE ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void insertHeader(Connection con, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoice (invoice_number, freelancer_id, client_id, issue_date, due_date) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceNumber());
            ps.setInt(2, invoice.getFreelancerId());
            ps.setInt(3, invoice.getClientId());
            ps.setDate(4, Date.valueOf(invoice.getIssueDate()));
            ps.setDate(5, Date.valueOf(invoice.getDueDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setId(keys.getInt(1));
                }
            }
        }
    }

    private void insertLines(Connection con, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoice_line (invoice_id, description, quantity, unit_rate, tax_percent) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (InvoiceLine line : invoice.getLines()) {
                ps.setInt(1, invoice.getId());
                ps.setString(2, line.getDescription());
                ps.setInt(3, line.getQuantity());
                ps.setBigDecimal(4, line.getUnitRate());
                ps.setBigDecimal(5, line.getTaxPercent());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Invoice> query(String where, Object... params) throws SQLException {
        List<Invoice> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(BASE_SELECT + where + GROUP_ORDER)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    result.add(new Invoice(id, rs.getString("invoice_number"),
                            rs.getInt("freelancer_id"), rs.getInt("client_id"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("due_date").toLocalDate(),
                            loadLines(con, id), rs.getBigDecimal("paid")));
                }
            }
        }
        return result;
    }

    private List<InvoiceLine> loadLines(Connection con, int invoiceId) throws SQLException {
        String sql = "SELECT * FROM invoice_line WHERE invoice_id = ? ORDER BY id";
        List<InvoiceLine> lines = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal rate = rs.getBigDecimal("unit_rate");
                    BigDecimal tax = rs.getBigDecimal("tax_percent");
                    lines.add(new InvoiceLine(rs.getString("description"),
                            rs.getInt("quantity"), rate, tax));
                }
            }
        }
        return lines;
    }
}
