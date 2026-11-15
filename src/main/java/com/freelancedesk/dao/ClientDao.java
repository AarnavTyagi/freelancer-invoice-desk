package com.freelancedesk.dao;

import com.freelancedesk.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao implements Repository<Client, Integer> {

    @Override
    public Client save(Client client) throws SQLException {
        String sql = "INSERT INTO client (name, email, company) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getCompany());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getInt(1));
                }
            }
        }
        return client;
    }

    @Override
    public Optional<Client> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM client WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> result = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM client ORDER BY id")) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getInt("id"), rs.getString("name"),
                rs.getString("email"), rs.getString("company"));
    }
}
