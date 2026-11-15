package com.freelancedesk.dao;

import com.freelancedesk.model.Freelancer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FreelancerDao implements Repository<Freelancer, Integer> {

    @Override
    public Freelancer save(Freelancer freelancer) throws SQLException {
        String sql = "INSERT INTO freelancer (name, email, profession) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, freelancer.getName());
            ps.setString(2, freelancer.getEmail());
            ps.setString(3, freelancer.getProfession());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    freelancer.setId(keys.getInt(1));
                }
            }
        }
        return freelancer;
    }

    @Override
    public Optional<Freelancer> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM freelancer WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Freelancer> findAll() throws SQLException {
        List<Freelancer> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM freelancer ORDER BY id")) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    private Freelancer map(ResultSet rs) throws SQLException {
        return new Freelancer(rs.getInt("id"), rs.getString("name"),
                rs.getString("email"), rs.getString("profession"));
    }
}
