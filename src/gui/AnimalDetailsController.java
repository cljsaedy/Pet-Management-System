package gui;

import database.AnimalDAO;
import database.AdoptionApplicationDAO;
import database.HealthRecordDAO;
import database.AdoptionDAO;
import database.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextArea; // Ensure this import is present
import javafx.stage.Modality;
import javafx.stage.Stage; // Ensure this import is present
import model.Animal;
import model.Adoption;
import model.AdoptionApplication;
import model.HealthRecord;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

public class AnimalDetailsController implements Initializable {

    @FXML
    private Label animalNameLabel;
    @FXML
    private Label animalSpeciesLabel;
    @FXML
    private Label animalBreedLabel;
    @FXML
    private Label animalAgeLabel;
    @FXML
    private Label animalGenderLabel;
    @FXML
    private Label healthStatusLabel;
    @FXML
    private TextArea descriptionLabel; 
    @FXML
    private Label adopterInfoLabel;

    @FXML
    private TableView<HealthRecord> healthRecordsTableView;
    @FXML
    private TableColumn<HealthRecord, String> recordDateColumn;
    @FXML
    private TableColumn<HealthRecord, String> diagnosisColumn;
    @FXML
    private TableColumn<HealthRecord, String> treatmentColumn;
    @FXML
    private TableColumn<HealthRecord, String> notesColumn;

    @FXML
    private Button addHealthRecordButton;
    @FXML
    private Button applyForAdoptionButton;
    @FXML
    private Button updateAnimalButton;
    @FXML
    private Button deleteAnimalButton;

    private Animal currentAnimal;
    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final AdoptionApplicationDAO applicationDAO = new AdoptionApplicationDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final UserDAO userDAO = new UserDAO();

    private ObservableList<HealthRecord> healthRecordsList = FXCollections.observableArrayList();
    private MainSceneController mainController;
    private UserDashboardController userDashboardController;
    private User loggedInUser;

    public void setAnimal(Animal animal) {
        this.currentAnimal = animal;
        populateDetails();
        loadHealthRecords();
        if (animal != null) {
            loadAndDisplayAdopterInfo(animal.getAnimalId());
        } else {
             if (adopterInfoLabel != null) {
                adopterInfoLabel.setText("");
                adopterInfoLabel.setVisible(false);
            }
        }
        updateButtonVisibility();
    }

    public void setMainController(MainSceneController mainController) {
        this.mainController = mainController;
        this.userDashboardController = null;
    }

    public void setUserDashboardController(UserDashboardController userDashboardController) {
        this.userDashboardController = userDashboardController;
        this.mainController = null;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (currentAnimal == null) {
            if (applyForAdoptionButton != null) { applyForAdoptionButton.setVisible(false); applyForAdoptionButton.setDisable(true); }
            if (addHealthRecordButton != null) { addHealthRecordButton.setVisible(false); addHealthRecordButton.setDisable(true); }
            if (updateAnimalButton != null) { updateAnimalButton.setVisible(false); updateAnimalButton.setDisable(true); }
            if (deleteAnimalButton != null) { deleteAnimalButton.setVisible(false); deleteAnimalButton.setDisable(true); }
            return;
        }

        boolean isStaffOrAdmin = loggedInUser != null && ("admin".equalsIgnoreCase(loggedInUser.getRole()) || "staff".equalsIgnoreCase(loggedInUser.getRole()));
        boolean isAdmin = loggedInUser != null && "admin".equalsIgnoreCase(loggedInUser.getRole());
        boolean isUserRole = loggedInUser != null && "user".equalsIgnoreCase(loggedInUser.getRole());

        if (addHealthRecordButton != null) {
            addHealthRecordButton.setVisible(isStaffOrAdmin);
            addHealthRecordButton.setDisable(!isStaffOrAdmin);
        }
        if (updateAnimalButton != null) {
            updateAnimalButton.setVisible(isStaffOrAdmin);
            updateAnimalButton.setDisable(!isStaffOrAdmin);
        }
        if (deleteAnimalButton != null) {
            deleteAnimalButton.setVisible(isAdmin);
            deleteAnimalButton.setDisable(!isAdmin);
        }
        if (applyForAdoptionButton != null) {
            applyForAdoptionButton.setVisible(isUserRole && !currentAnimal.isAdopted());
            applyForAdoptionButton.setDisable(!(isUserRole && !currentAnimal.isAdopted()));
        }
    }

    private void populateDetails() {
        if (currentAnimal != null) {
            animalNameLabel.setText(currentAnimal.getName() != null ? currentAnimal.getName() : "N/A");
            animalSpeciesLabel.setText(currentAnimal.getSpecies() != null ? currentAnimal.getSpecies() : "N/A");
            animalBreedLabel.setText(currentAnimal.getBreed() != null ? currentAnimal.getBreed() : "N/A");
            animalAgeLabel.setText(String.valueOf(currentAnimal.getAge()));
            animalGenderLabel.setText(currentAnimal.getGender() != null ? currentAnimal.getGender() : "N/A");
            healthStatusLabel.setText(currentAnimal.getHealthStatus() != null ? currentAnimal.getHealthStatus() : "N/A");
            if (descriptionLabel != null) { 
                 descriptionLabel.setText(currentAnimal.getDescription() != null ? currentAnimal.getDescription() : "No description available.");
            }
        } else {
            clearDetails();
        }
    }

    private void clearDetails() {
        animalNameLabel.setText("");
        animalSpeciesLabel.setText("");
        animalBreedLabel.setText("");
        animalAgeLabel.setText("");
        animalGenderLabel.setText("");
        healthStatusLabel.setText("");
        if(descriptionLabel != null) descriptionLabel.setText("");
        if (adopterInfoLabel != null) {
            adopterInfoLabel.setText("");
            adopterInfoLabel.setVisible(false);
        }
        healthRecordsList.clear();
    }

    private void loadAndDisplayAdopterInfo(int animalId) {
        if (adopterInfoLabel == null) {
            System.err.println("Developer note: adopterInfoLabel is null. Check FXML fx:id.");
            return;
        }

        Adoption adoption = adoptionDAO.getAdoptionByAnimalId(animalId);

        if (adoption != null && adoption.getAdopterId() > 0) {
            User adopter = userDAO.getUser(adoption.getAdopterId());
            if (adopter != null && adopter.getName() != null && !adopter.getName().isEmpty()) {
                adopterInfoLabel.setText("Status: Adopted by " + adopter.getName());
            } else {
                adopterInfoLabel.setText("Status: Adopted (Adopter details unavailable)");
            }
            adopterInfoLabel.setVisible(true);
            if(currentAnimal != null) currentAnimal.setAdopted(true);
        } else {
            adopterInfoLabel.setText("Status: Available for Adoption");
            adopterInfoLabel.setVisible(true);
            if(currentAnimal != null) currentAnimal.setAdopted(false);
        }
        updateButtonVisibility();
    }

    public void loadHealthRecords() {
        healthRecordsList.clear();
        if (currentAnimal != null) {
            List<HealthRecord> records = healthRecordDAO.getHealthRecordsByAnimalId(currentAnimal.getAnimalId());
            if (records != null) {
                healthRecordsList.addAll(records);
            }
        }
        healthRecordsTableView.setItems(healthRecordsList);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recordDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getRecordDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getRecordDate().format(DateTimeFormatter.ISO_DATE));
            } else {
                return new SimpleStringProperty("");
            }
        });
        diagnosisColumn.setCellValueFactory(cellData -> cellData.getValue() != null ? new SimpleStringProperty(cellData.getValue().getDiagnosis()) : new SimpleStringProperty(""));
        treatmentColumn.setCellValueFactory(cellData -> cellData.getValue() != null ? new SimpleStringProperty(cellData.getValue().getTreatment()) : new SimpleStringProperty(""));
        notesColumn.setCellValueFactory(cellData -> cellData.getValue() != null ? new SimpleStringProperty(cellData.getValue().getNotes()) : new SimpleStringProperty(""));
        healthRecordsTableView.setItems(healthRecordsList);

        if (applyForAdoptionButton != null) { applyForAdoptionButton.setVisible(false); applyForAdoptionButton.setDisable(true); }
        if (addHealthRecordButton != null) { addHealthRecordButton.setVisible(false); addHealthRecordButton.setDisable(true); }
        if (updateAnimalButton != null) { updateAnimalButton.setVisible(false); updateAnimalButton.setDisable(true); }
        if (deleteAnimalButton != null) { deleteAnimalButton.setVisible(false); deleteAnimalButton.setDisable(true); }
        if (adopterInfoLabel != null) { adopterInfoLabel.setText(""); adopterInfoLabel.setVisible(false); }
    }

    @FXML
    private void handleAddHealthRecord() {
        if (currentAnimal == null) return;
        try {
            URL location = getClass().getClassLoader().getResource("gui/HealthRecordForm.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find HealthRecordForm.fxml");
                showAlert("FXML Error", "Cannot load health record form.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            HealthRecordController controller = loader.getController();
            controller.setCurrentAnimalId(currentAnimal.getAnimalId());
            controller.setAnimalDetailsController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Health Record for " + currentAnimal.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (addHealthRecordButton != null && addHealthRecordButton.getScene() != null && addHealthRecordButton.getScene().getWindow() != null) {
                stage.initOwner(addHealthRecordButton.getScene().getWindow());
            }
            stage.showAndWait();
            loadHealthRecords();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Load Error", "Could not load the health record form: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyForAdoption() {
        if (currentAnimal == null || loggedInUser == null) return;

        if ("user".equalsIgnoreCase(loggedInUser.getRole())) {
            if (currentAnimal.isAdopted()) {
                showAlert("Already Adopted", currentAnimal.getName() + " has already been adopted.");
                return;
            }
            try {
                URL location = getClass().getClassLoader().getResource("gui/AdoptionApplicationForm.fxml");
                if (location == null) {
                    System.err.println("Error: Cannot find AdoptionApplicationForm.fxml");
                    showAlert("FXML Error", "Cannot load adoption application form.");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(location);
                Parent root = loader.load();

                AdoptionApplicationController controller = loader.getController();
                controller.setAnimalId(currentAnimal.getAnimalId());
                controller.setAdopterId(loggedInUser.getUserId());
                controller.setAnimalDetailsController(this);

                Stage stage = new Stage();
                stage.setTitle("Adoption Application for " + currentAnimal.getName());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                 if (applyForAdoptionButton != null && applyForAdoptionButton.getScene() != null && applyForAdoptionButton.getScene().getWindow() != null) {
                    stage.initOwner(applyForAdoptionButton.getScene().getWindow());
                }
                stage.showAndWait();
                 if (userDashboardController != null) {
                 }
                 loadAndDisplayAdopterInfo(currentAnimal.getAnimalId());

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Load Error", "Could not load the adoption application form: " + e.getMessage());
            }
        } else {
            showAlert("Access Denied", "You must be logged in as a regular user to apply for adoption.");
        }
    }

    @FXML
    private void handleUpdateAnimal() {
        if (currentAnimal == null ) {
            System.err.println("UpdateAnimal: currentAnimal is null.");
            showAlert("Error", "Cannot update: Animal data is not available.");
            return;
        }
        try {
            URL location = getClass().getClassLoader().getResource("gui/UpdateAnimalForm.fxml");
             if (location == null) {
                System.err.println("Error: Cannot find UpdateAnimalForm.fxml");
                showAlert("FXML Error", "Cannot load the update animal form.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            UpdateAnimalController controller = loader.getController();
            controller.setAnimal(currentAnimal);
            if (mainController != null) {
                controller.setMainController(mainController);
            }

            Stage stage = new Stage();
            stage.setTitle("Update Animal: " + currentAnimal.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (updateAnimalButton != null && updateAnimalButton.getScene() != null && updateAnimalButton.getScene().getWindow() != null) {
                stage.initOwner(updateAnimalButton.getScene().getWindow());
            }
            stage.showAndWait();

            if (mainController != null) {
                mainController.refreshAnimalTable();
            }
            if (userDashboardController != null) {
            }

            Animal refreshedAnimal = animalDAO.getAnimal(currentAnimal.getAnimalId());
            if (refreshedAnimal != null) {
                setAnimal(refreshedAnimal);
            } else {
                System.err.println("Animal with ID " + currentAnimal.getAnimalId() + " not found after update attempt. Closing details window.");
                showAlert("Data Sync Error", "Animal details could not be refreshed. The animal may no longer exist.");
                closeCurrentWindow(); 
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Load Error", "Could not load the update animal form: " + e.getMessage());
        } catch (Exception e) {
             e.printStackTrace();
            showAlert("Unexpected Error", "An unexpected error occurred during update: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAnimal() {
        if (currentAnimal == null) {
            showAlert("Delete Error", "No animal selected to delete.");
            return;
        }
        if (mainController == null && userDashboardController == null) {
            showAlert("Error", "Cannot determine context for refreshing list after deletion.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Animal: " + currentAnimal.getName());
        confirmAlert.setContentText("Are you sure you want to permanently delete " + currentAnimal.getName() + "? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = animalDAO.deleteAnimal(currentAnimal.getAnimalId());
                if (deleted) {
                    showAlert("Success", "Animal '" + currentAnimal.getName() + "' deleted successfully.");
                    if (mainController != null) {
                        mainController.refreshAnimalTable();
                    }
                    closeCurrentWindow(); 
                } else {
                    showAlert("Failure", "Failed to delete animal '" + currentAnimal.getName() + "'. It might have associated records (e.g., adoptions, applications) that prevent deletion, or it was already deleted.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while deleting the animal: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) { 
        Alert alert = new Alert(Alert.AlertType.INFORMATION); 
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeCurrentWindow() {
        if (animalNameLabel != null && animalNameLabel.getScene() != null && animalNameLabel.getScene().getWindow() != null) {
            Stage stage = (Stage) animalNameLabel.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        } else {
            System.err.println("Could not find a node to close the current window from AnimalDetailsController.");
        }
    }

    @FXML
    private void handleCloseWindowAction() { 
        closeCurrentWindow();
    }
}