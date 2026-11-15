package com.freelancedesk.dao;

import com.freelancedesk.model.Payment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDao implements Repository<Payment, Integer> {

    @Override
    public Payment save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payment (invoice_id, amount, payment_date, method) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getInvoiceId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setDate(3, Date.valueOf(payment.getPaymentDate()));
            ps.setString(4, payment.getMethod());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        }
        return payment;
    }

    @Override
    public Optional<Payment> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM payment WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        List<Payment> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM payment ORDER BY id")) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    private Payment map(ResultSet rs) throws SQLException {
        return new Payment(rs.getInt("id"), rs.getInt("invoice_id"), rs.getBigDecimal("amount"),
                rs.getDate("payment_date").toLocalDate(), rs.getString("method"));
    }
}
