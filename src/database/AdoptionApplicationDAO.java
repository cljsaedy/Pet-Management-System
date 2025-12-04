package database;

import model.AdoptionApplication;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdoptionApplicationDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public void saveAdoptionApplication(AdoptionApplication application) {
        String sql = "INSERT INTO adoption_applications (animal_id, user_id, reason_for_adoption, has_other_pets, living_situation, contact_preference) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, application.getAnimalId());
            preparedStatement.setInt(2, application.getUserId());
            preparedStatement.setString(3, application.getReasonForAdoption());
            preparedStatement.setBoolean(4, application.isHasOtherPets());
            preparedStatement.setString(5, application.getLivingSituation());
            preparedStatement.setString(6, application.getContactPreference());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AdoptionApplication> getAllAdoptionApplications() {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "SELECT aa.id, aa.animal_id, aa.user_id, aa.reason_for_adoption, aa.has_other_pets, aa.living_situation, aa.contact_preference, aa.application_date, a.name AS animal_name, u.name AS applicant_name " +
                     "FROM adoption_applications aa " +
                     "JOIN animals a ON aa.animal_id = a.id " +
                     "JOIN users u ON aa.user_id = u.id";
        try (Connection connection = dbConnector.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                AdoptionApplication application = new AdoptionApplication();
                application.setApplicationId(resultSet.getInt("id"));
                application.setAnimalId(resultSet.getInt("animal_id"));
                application.setUserId(resultSet.getInt("user_id"));
                application.setReasonForAdoption(resultSet.getString("reason_for_adoption"));
                application.setHasOtherPets(resultSet.getBoolean("has_other_pets"));
                application.setLivingSituation(resultSet.getString("living_situation"));
                application.setContactPreference(resultSet.getString("contact_preference"));
                application.setApplicationDate(resultSet.getTimestamp("application_date").toLocalDateTime());
                application.setAnimalName(resultSet.getString("animal_name"));
                application.setApplicantName(resultSet.getString("applicant_name"));
                applications.add(application);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    public boolean deleteAdoptionApplication(int applicationId) {
        String sql = "DELETE FROM adoption_applications WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, applicationId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
