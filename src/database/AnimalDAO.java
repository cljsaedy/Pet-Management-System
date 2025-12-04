package database;

import model.Animal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {

    private final DatabaseConnector dbConnector = DatabaseConnector.getInstance();

    public void saveAnimal(Animal animal) {
        String sql = "INSERT INTO animals (name, species, age, gender, health_status, is_adopted, breed, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, animal.getName());
            preparedStatement.setString(2, animal.getSpecies());
            preparedStatement.setInt(3, animal.getAge());
            preparedStatement.setString(4, animal.getGender());
            preparedStatement.setString(5, animal.getHealthStatus());
            preparedStatement.setBoolean(6, animal.isAdopted());
            preparedStatement.setString(7, animal.getBreed());
            preparedStatement.setString(8, animal.getDescription());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                animal.setAnimalId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Animal getAnimal(int id) {
        String sql = "SELECT id, name, species, age, gender, health_status, is_adopted, breed, description FROM animals WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Animal animal = new Animal();
                animal.setAnimalId(resultSet.getInt("id"));
                animal.setName(resultSet.getString("name"));
                animal.setSpecies(resultSet.getString("species"));
                animal.setAge(resultSet.getInt("age"));
                animal.setGender(resultSet.getString("gender"));
                animal.setHealthStatus(resultSet.getString("health_status"));
                animal.setAdopted(resultSet.getBoolean("is_adopted"));
                animal.setBreed(resultSet.getString("breed"));
                animal.setDescription(resultSet.getString("description"));
                return animal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Animal> getAllAnimals() {
        return getAllAnimals(false);
    }

    public List<Animal> getAllAnimals(boolean includeAdopted) {
        List<Animal> animals = new ArrayList<>();
        String sql = "SELECT id, name, species, age, gender, health_status, is_adopted, breed, description FROM animals";
        if (!includeAdopted) {
            sql += " WHERE is_adopted = FALSE";
        }
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Animal animal = new Animal();
                animal.setAnimalId(resultSet.getInt("id"));
                animal.setName(resultSet.getString("name"));
                animal.setSpecies(resultSet.getString("species"));
                animal.setAge(resultSet.getInt("age"));
                animal.setGender(resultSet.getString("gender"));
                animal.setHealthStatus(resultSet.getString("health_status"));
                animal.setAdopted(resultSet.getBoolean("is_adopted"));
                animal.setBreed(resultSet.getString("breed"));
                animal.setDescription(resultSet.getString("description"));
                animals.add(animal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return animals;
    }

    public void updateAnimal(Animal animal) {
        String sql = "UPDATE animals SET name = ?, species = ?, age = ?, gender = ?, health_status = ?, is_adopted = ?, breed = ?, description = ? WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, animal.getName());
            preparedStatement.setString(2, animal.getSpecies());
            preparedStatement.setInt(3, animal.getAge());
            preparedStatement.setString(4, animal.getGender());
            preparedStatement.setString(5, animal.getHealthStatus());
            preparedStatement.setBoolean(6, animal.isAdopted());
            preparedStatement.setString(7, animal.getBreed());
            preparedStatement.setString(8, animal.getDescription());
            preparedStatement.setInt(9, animal.getAnimalId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAdopted(int animalId, boolean adoptedStatus) {
        String sql = "UPDATE animals SET is_adopted = ? WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, adoptedStatus);
            preparedStatement.setInt(2, animalId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteAnimal(int id) {
        String sql = "DELETE FROM animals WHERE id = ?";
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
