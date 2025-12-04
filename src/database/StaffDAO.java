package database;

import model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public void saveStaff(Staff staff) {
        String sql = "INSERT INTO staff (username, password, name, role, contact_info, shelter_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPassword());
            pstmt.setString(3, staff.getName());
            pstmt.setString(4, staff.getRole());
            pstmt.setString(5, staff.getContactInfo());
            pstmt.setInt(6, staff.getShelterId());
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                staff.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Staff getStaff(int id) {
        String sql = "SELECT id, username, password, name, role, contact_info, shelter_id FROM staff WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getInt("id"));
                staff.setUsername(rs.getString("username"));
                staff.setPassword(rs.getString("password"));
                staff.setName(rs.getString("name"));
                staff.setRole(rs.getString("role"));
                staff.setContactInfo(rs.getString("contact_info"));
                staff.setShelterId(rs.getInt("shelter_id"));
                return staff;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT id, username, password, name, role, contact_info, shelter_id FROM staff";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getInt("id"));
                staff.setUsername(rs.getString("username"));
                staff.setPassword(rs.getString("password"));
                staff.setName(rs.getString("name"));
                staff.setRole(rs.getString("role"));
                staff.setContactInfo(rs.getString("contact_info"));
                staff.setShelterId(rs.getInt("shelter_id"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    public void updateStaff(Staff staff) {
        String sql = "UPDATE staff SET username = ?, password = ?, name = ?, role = ?, contact_info = ?, shelter_id = ? WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getUsername());
            pstmt.setString(2, staff.getPassword());
            pstmt.setString(3, staff.getName());
            pstmt.setString(4, staff.getRole());
            pstmt.setString(5, staff.getContactInfo());
            pstmt.setInt(6, staff.getShelterId());
            pstmt.setInt(7, staff.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStaff(int id) {
        String sql = "DELETE FROM staff WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}