package database;

import model.HealthRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO {

    private final DatabaseConnector connector = DatabaseConnector.getInstance();

    public boolean addHealthRecord(HealthRecord record) {
        String sql = "INSERT INTO health_records (animal_id, record_date, diagnosis, treatment, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, record.getAnimalId());
            statement.setDate(2, Date.valueOf(record.getRecordDate()));
            statement.setString(3, record.getDiagnosis());
            statement.setString(4, record.getTreatment());
            statement.setString(5, record.getNotes());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HealthRecord> getHealthRecordsByAnimalId(int animalId) {
        List<HealthRecord> records = new ArrayList<>();
        String sql = "SELECT id, record_date, diagnosis, treatment, notes FROM health_records WHERE animal_id = ?";
        try (Connection connection = connector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, animalId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                HealthRecord record = new HealthRecord();
                record.setId(resultSet.getInt("id"));
                record.setAnimalId(animalId);
                record.setRecordDate(resultSet.getDate("record_date").toLocalDate());
                record.setDiagnosis(resultSet.getString("diagnosis"));
                record.setTreatment(resultSet.getString("treatment"));
                record.setNotes(resultSet.getString("notes"));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public HealthRecord getHealthRecordById(int id) {
        return null;
    }

    public boolean updateHealthRecord(HealthRecord record) {
        return false;
    }

    public boolean deleteHealthRecord(int id) {
        return false;
    }
}
