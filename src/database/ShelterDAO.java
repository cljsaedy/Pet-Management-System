package database;

import model.Shelter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShelterDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public void saveShelter(Shelter shelter) {
        String sql = "INSERT INTO shelters (name, location, capacity) VALUES (?, ?, ?)";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, shelter.getName());
            pstmt.setString(2, shelter.getLocation());
            pstmt.setInt(3, shelter.getCapacity());
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                shelter.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Shelter getShelter(int id) {
        String sql = "SELECT id, name, location, capacity FROM shelters WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Shelter shelter = new Shelter();
                shelter.setId(rs.getInt("id"));
                shelter.setName(rs.getString("name"));
                shelter.setLocation(rs.getString("location"));
                shelter.setCapacity(rs.getInt("capacity"));
                return shelter;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Shelter> getAllShelters() {
        List<Shelter> shelters = new ArrayList<>();
        String sql = "SELECT id, name, location, capacity FROM shelters";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Shelter shelter = new Shelter();
                shelter.setId(rs.getInt("id"));
                shelter.setName(rs.getString("name"));
                shelter.setLocation(rs.getString("location"));
                shelter.setCapacity(rs.getInt("capacity"));
                shelters.add(shelter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shelters;
    }

    public void updateShelter(Shelter shelter) {
        String sql = "UPDATE shelters SET name = ?, location = ?, capacity = ? WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, shelter.getName());
            pstmt.setString(2, shelter.getLocation());
            pstmt.setInt(3, shelter.getCapacity());
            pstmt.setInt(4, shelter.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteShelter(int id) {
        String sql = "DELETE FROM shelters WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}