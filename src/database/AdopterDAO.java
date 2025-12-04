package database;

import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class AdopterDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public User getAdopter(int id) {
        String sql = "SELECT id, username, password, name, contact_info, address FROM adopters WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User adopter = new User();
                adopter.setUserId(resultSet.getInt("id"));
                adopter.setUsername(resultSet.getString("username"));
                adopter.setPassword(resultSet.getString("password"));
                adopter.setName(resultSet.getString("name"));
                adopter.setContactInfo(resultSet.getString("contact_info"));
                adopter.setAddress(resultSet.getString("address"));
                return adopter;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveAdopter(User adopter) {
        String sql = "INSERT INTO adopters (username, password, name, contact_info, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, adopter.getUsername());
            preparedStatement.setString(2, adopter.getPassword());
            preparedStatement.setString(3, adopter.getName());
            preparedStatement.setString(4, adopter.getContactInfo());
            preparedStatement.setString(5, adopter.getAddress());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getAdopterIdByUsername(String username) {
        String sql = "SELECT id FROM adopters WHERE username = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteAdopter(int adopterId) {
        String sql = "DELETE FROM adopters WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, adopterId);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllAdopters() {
        List<User> adopters = new ArrayList<>();
        String sql = "SELECT id, username, password, name, contact_info, address FROM adopters";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                User adopter = new User();
                adopter.setUserId(resultSet.getInt("id"));
                adopter.setUsername(resultSet.getString("username"));
                adopter.setPassword(resultSet.getString("password"));
                adopter.setName(resultSet.getString("name"));
                adopter.setContactInfo(resultSet.getString("contact_info"));
                adopter.setAddress(resultSet.getString("address"));
                adopters.add(adopter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adopters;
    }
}
