package gui;

import database.AdoptionDAO;
import database.AdopterDAO;
import database.AnimalDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import model.Animal;
import model.User;
import model.Adoption;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RecordAdoptionController implements Initializable {

    @FXML
    private ComboBox<Animal> animalComboBox;

    @FXML
    private ComboBox<User> adopterComboBox;

    @FXML
    private DatePicker adoptionDatePicker;

    @FXML
    private Button recordAdoptionButton;

    @FXML
    private Button cancelButton;

    private AnimalDAO animalDAO;
    private AdoptionDAO adoptionDAO;
    private AdopterDAO adopterDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animalDAO = new AnimalDAO();
        adoptionDAO = new AdoptionDAO();
        adopterDAO = new AdopterDAO();

        loadAvailableAnimals();
        loadAdopters();
    }

    private void loadAvailableAnimals() {
        List<Animal> allAnimals = animalDAO.getAllAnimals();
        ObservableList<Animal> availableAnimalList = FXCollections.observableArrayList();
        if (allAnimals != null) {
            availableAnimalList.addAll(
                allAnimals.stream().filter(animal -> !animal.isAdopted()).collect(Collectors.toList())
            );
        }
        animalComboBox.setItems(availableAnimalList);
    }

    private void loadAdopters() {
        List<User> allAdopters = adopterDAO.getAllAdopters();
        ObservableList<User> adopterObservableList = FXCollections.observableArrayList();
        if (allAdopters != null) {
            adopterObservableList.addAll(allAdopters);
        }
        adopterComboBox.setItems(adopterObservableList);
    }

    @FXML
    private void handleRecordAdoption() {
        Animal selectedAnimal = animalComboBox.getValue();
        User selectedUserAsAdopter = adopterComboBox.getValue();
        LocalDate adoptionDate = adoptionDatePicker.getValue();

        if (selectedAnimal == null || selectedUserAsAdopter == null || adoptionDate == null) {
            showAlert(AlertType.ERROR, "Validation Error", "Please select an animal, an adopter, and an adoption date.");
            return;
        }

        if (selectedAnimal.isAdopted()) {
            showAlert(AlertType.WARNING, "Already Adopted", selectedAnimal.getName() + " has already been recorded as adopted. Please select another animal.");
            loadAvailableAnimals();
            animalComboBox.getSelectionModel().clearSelection();
            return;
        }

        Adoption newAdoption = new Adoption();
        newAdoption.setAnimalId(selectedAnimal.getAnimalId());
        newAdoption.setAdopterId(selectedUserAsAdopter.getUserId());
        newAdoption.setAdoptionDate(adoptionDate);
        newAdoption.setAnimalName(selectedAnimal.getName());
        newAdoption.setAdopterName(selectedUserAsAdopter.getName());

        boolean saved = adoptionDAO.saveAdoption(newAdoption);

        if (saved) {
            showAlert(AlertType.INFORMATION, "Success", "Adoption recorded successfully for " + selectedAnimal.getName() + " and " + selectedUserAsAdopter.getName() + "!");

            selectedAnimal.setAdopted(true);
            animalDAO.updateAnimal(selectedAnimal);

            loadAvailableAnimals();

            animalComboBox.getSelectionModel().clearSelection();
            adopterComboBox.getSelectionModel().clearSelection();
            adoptionDatePicker.setValue(null);

        } else {
            showAlert(AlertType.ERROR, "Database Error", "Failed to record the adoption in the database. Please check logs.");
        }
    }
    
    @FXML
    private void handleCancel() {
        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        if (recordAdoptionButton != null && recordAdoptionButton.getScene() != null) {
            Stage stage = (Stage) recordAdoptionButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        } else if (cancelButton != null && cancelButton.getScene() != null) {
             Stage stage = (Stage) cancelButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
