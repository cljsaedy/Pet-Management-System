package database;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public User getUserByUsername(String username) {
        User user = null;
        String sql = "SELECT id, username, password, role, name, contact_info, address, email FROM users WHERE username = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUser(int id) {
        User user = null;
        String sql = "SELECT id, username, password, role, name, contact_info, address, email FROM users WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setName(resultSet.getString("name"));
        user.setContactInfo(resultSet.getString("contact_info"));
        user.setAddress(resultSet.getString("address"));
        user.setEmail(resultSet.getString("email"));
        return user;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role, name, contact_info, address, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatementUserFields(preparedStatement, user, 1);
            int rowsInserted = preparedStatement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, password, role, name, contact_info, address, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatementUserFields(preparedStatement, user, 1);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setPreparedStatementUserFields(PreparedStatement ps, User user, int startIndex) throws SQLException {
        ps.setString(startIndex, user.getUsername());
        ps.setString(startIndex + 1, user.getPassword());
        ps.setString(startIndex + 2, user.getRole());
        ps.setString(startIndex + 3, user.getName());
        ps.setString(startIndex + 4, user.getContactInfo());
        ps.setString(startIndex + 5, user.getAddress());
        ps.setString(startIndex + 6, user.getEmail());
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, role, name, contact_info, address, email FROM users";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                User user = mapResultSetToUser(resultSet);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, name = ?, contact_info = ?, address = ?, email = ? WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatementUserFields(preparedStatement, user, 1);
            preparedStatement.setInt(8, user.getUserId());
            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}