package gui;

import database.AnimalDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Animal;

public class AddAnimalController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField speciesField;

    @FXML
    private TextField breedField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField healthField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private AnimalDAO animalDAO = new AnimalDAO();
    private MainSceneController mainController;

    public void setMainController(MainSceneController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleSaveAnimal() {
        String name = nameField.getText();
        String species = speciesField.getText();
        String breed = breedField.getText();
        String healthStatus = healthField.getText();
        String description = descriptionField.getText();
        String gender = genderComboBox.getValue();

        int age;
        try {
            age = Integer.parseInt(ageField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid age.");
            return;
        }

        if (name.isEmpty() || species.isEmpty() || gender == null || gender.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, Species, and Gender cannot be empty.");
            return;
        }

        Animal newAnimal = new Animal();
        newAnimal.setName(name);
        newAnimal.setSpecies(species);
        newAnimal.setBreed(breed);
        newAnimal.setAge(age);
        newAnimal.setGender(gender);
        newAnimal.setHealthStatus(healthStatus);
        newAnimal.setDescription(description);
        newAnimal.setAdopted(false);

        animalDAO.saveAnimal(newAnimal);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Animal added successfully!");
        closeStage();
        if (mainController != null) {
            mainController.loadAnimals();
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
