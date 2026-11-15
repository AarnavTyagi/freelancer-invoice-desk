package com.freelancedesk.dao;

import com.freelancedesk.model.BillableService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillableServiceDao implements Repository<BillableService, Integer> {

    @Override
    public BillableService save(BillableService service) throws SQLException {
        String sql = "INSERT INTO billable_service (freelancer_id, description, unit_rate, tax_percent) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, service.getFreelancerId());
            ps.setString(2, service.getDescription());
            ps.setBigDecimal(3, service.getUnitRate());
            ps.setBigDecimal(4, service.getTaxPercent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setId(keys.getInt(1));
                }
            }
        }
        return service;
    }

    @Override
    public Optional<BillableService> findById(Integer id) throws SQLException {
        List<BillableService> found = query("WHERE id = ?", id);
        return found.stream().findFirst();
    }

    @Override
    public List<BillableService> findAll() throws SQLException {
        return query("");
    }

    public List<BillableService> findByFreelancer(int freelancerId) throws SQLException {
        return query("WHERE freelancer_id = ?", freelancerId);
    }

    private List<BillableService> query(String where, Object... params) throws SQLException {
        String sql = "SELECT * FROM billable_service " + where + " ORDER BY id";
        List<BillableService> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new BillableService(rs.getInt("id"), rs.getInt("freelancer_id"),
                            rs.getString("description"), rs.getBigDecimal("unit_rate"),
                            rs.getBigDecimal("tax_percent")));
                }
            }
        }
        return result;
    }
}
