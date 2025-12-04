package database;

import model.Adoption;
import model.Animal;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdoptionDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public boolean saveAdoption(Adoption adoption) {
        String sql = "INSERT INTO adoptions (animal_id, adopter_id, adoption_date, animal_name, adopter_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, adoption.getAnimalId());
            preparedStatement.setInt(2, adoption.getAdopterId());
            preparedStatement.setDate(3, Date.valueOf(adoption.getAdoptionDate()));
            preparedStatement.setString(4, adoption.getAnimalName());
            preparedStatement.setString(5, adoption.getAdopterName());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        adoption.setAdoptionId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error saving adoption: " + e.getMessage());
            return false;
        }
    }

    public Adoption getAdoptionByAdopterId(int adopterId) {
        Adoption adoption = null;
        String sql = "SELECT " +
                "a.id AS adoption_id, " +
                "a.animal_id, " +
                "a.adopter_id, " +
                "a.adoption_date, " +
                "a.animal_name AS stored_animal_name, " +
                "a.adopter_name AS stored_adopter_name, " +
                "an.name AS current_animal_name, " +
                "u.name AS current_adopter_name " +
                "FROM adoptions a " +
                "LEFT JOIN animals an ON a.animal_id = an.id " +
                "LEFT JOIN users u ON a.adopter_id = u.id " +
                "WHERE a.adopter_id = ? " +
                "ORDER BY a.adoption_date DESC LIMIT 1";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, adopterId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                adoption = new Adoption();
                adoption.setAdoptionId(resultSet.getInt("adoption_id"));
                adoption.setAnimalId(resultSet.getInt("animal_id"));
                adoption.setAdopterId(resultSet.getInt("adopter_id"));
                adoption.setAdoptionDate(resultSet.getDate("adoption_date").toLocalDate());
                adoption.setAnimalName(resultSet.getString("current_animal_name") != null ? resultSet.getString("current_animal_name") : resultSet.getString("stored_animal_name"));
                adoption.setAdopterName(resultSet.getString("current_adopter_name") != null ? resultSet.getString("current_adopter_name") : resultSet.getString("stored_adopter_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adoption;
    }

    public Adoption getAdoptionByAnimalId(int animalId) {
        Adoption adoption = null;
        String sql = "SELECT " +
                "a.id AS adoption_id, " +
                "a.animal_id, " +
                "a.adopter_id, " +
                "a.adoption_date, " +
                "a.animal_name AS stored_animal_name, " +
                "a.adopter_name AS stored_adopter_name, " +
                "an.name AS current_animal_name, " +
                "u.name AS current_adopter_name " +
                "FROM adoptions a " +
                "LEFT JOIN animals an ON a.animal_id = an.id " +
                "LEFT JOIN users u ON a.adopter_id = u.id " +
                "WHERE a.animal_id = ?";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, animalId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                adoption = new Adoption();
                adoption.setAdoptionId(resultSet.getInt("adoption_id"));
                adoption.setAnimalId(resultSet.getInt("animal_id"));
                adoption.setAdopterId(resultSet.getInt("adopter_id"));
                adoption.setAdoptionDate(resultSet.getDate("adoption_date").toLocalDate());
                adoption.setAnimalName(resultSet.getString("current_animal_name") != null ? resultSet.getString("current_animal_name") : resultSet.getString("stored_animal_name"));
                adoption.setAdopterName(resultSet.getString("current_adopter_name") != null ? resultSet.getString("current_adopter_name") : resultSet.getString("stored_adopter_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adoption;
    }

    public boolean deleteAdoptionByAnimalId(int animalId) {
        String sql = "DELETE FROM adoptions WHERE animal_id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, animalId);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Adoption> getAllAdoptionsWithDetails() {
        List<Adoption> adoptions = new ArrayList<>();
        String sql = "SELECT " +
                "a.id AS adoption_id, " +
                "a.adoption_date, " +
                "a.animal_id, " +
                "a.adopter_id, " +
                "COALESCE(an.name, a.animal_name) AS final_animal_name, " +
                "COALESCE(u.name, a.adopter_name) AS final_adopter_name, " +
                "u.contact_info AS adopter_contact, " +
                "u.address AS adopter_address " +
                "FROM adoptions a " +
                "LEFT JOIN animals an ON a.animal_id = an.id " +
                "LEFT JOIN users u ON a.adopter_id = u.id";

        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Adoption adoption = new Adoption();
                adoption.setAdoptionId(resultSet.getInt("adoption_id"));
                adoption.setAdoptionDate(resultSet.getDate("adoption_date").toLocalDate());
                adoption.setAnimalId(resultSet.getInt("animal_id"));
                adoption.setAdopterId(resultSet.getInt("adopter_id"));
                adoption.setAnimalName(resultSet.getString("final_animal_name"));
                adoption.setAdopterName(resultSet.getString("final_adopter_name"));
                adoptions.add(adoption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adoptions;
    }
}
