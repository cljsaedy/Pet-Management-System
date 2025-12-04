package gui;

import database.AdoptionDAO;
import database.AdoptionApplicationDAO;
import database.AnimalDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.Animal;
import model.Adoption;
import model.User;
import model.AdoptionApplication;

import java.time.LocalDate;
import java.util.Optional;

public class ApproveAdoptionController {

    @FXML
    private Label adopterNameLabel;

    @FXML
    private Label animalNameDisplayLabel;

    @FXML
    private TextArea reasonForAdoptionArea;

    private User adopterUser;
    private AdoptionApplication currentApplication;
    private Animal animalInApplication;

    private MainSceneController mainController;
    private AdopterListController adopterListController;
    private AdoptionApplicationListController applicationListController;

    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();
    private final AdoptionApplicationDAO applicationDAO = new AdoptionApplicationDAO();

    public void setAdopterUser(User user) {
        this.adopterUser = user;
        if (this.adopterUser != null) {
            adopterNameLabel.setText("Applicant: " + this.adopterUser.getName());
        } else {
            adopterNameLabel.setText("Applicant: Unknown");
        }
    }

    public void setCurrentApplication(AdoptionApplication application) {
        this.currentApplication = application;
        if (this.currentApplication != null) {
            reasonForAdoptionArea.setText(this.currentApplication.getReasonForAdoption());
            reasonForAdoptionArea.setEditable(false);
            reasonForAdoptionArea.setWrapText(true);

            this.animalInApplication = animalDAO.getAnimal(currentApplication.getAnimalId());
            if (this.animalInApplication != null) {
                animalNameDisplayLabel.setText("Animal in Application: " + this.animalInApplication.getName() + " (ID: " + this.animalInApplication.getAnimalId() + ")");
            } else {
                animalNameDisplayLabel.setText("Animal in Application: Details not found (ID: " + currentApplication.getAnimalId() + ")");
            }
        } else {
            reasonForAdoptionArea.setText("No application data available.");
            animalNameDisplayLabel.setText("Animal in Application: N/A");
        }
    }

    public void setMainController(MainSceneController mainController) {
        this.mainController = mainController;
    }

    public void setAdopterListController(AdopterListController controller) {
        this.adopterListController = controller;
    }

    public void setApplicationListController(AdoptionApplicationListController controller) {
        this.applicationListController = controller;
    }

    @FXML
    private void handleApprove() {
        if (adopterUser != null && adopterUser.getUserId() > 0 && currentApplication != null && animalInApplication != null) {
            if (animalInApplication.isAdopted()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Already Adopted");
                alert.setHeaderText(null);
                alert.setContentText(animalInApplication.getName() + " has already been marked as adopted.");
                alert.showAndWait();
                return;
            }

            int adopterIdForAdoption = adopterUser.getUserId();

            Adoption adoption = new Adoption();
            adoption.setAnimalId(animalInApplication.getAnimalId());
            adoption.setAdopterId(adopterIdForAdoption);
            adoption.setAdoptionDate(LocalDate.now());
            adoption.setAnimalName(animalInApplication.getName());
            adoption.setAdopterName(adopterUser.getName());

            boolean adoptionSaved = adoptionDAO.saveAdoption(adoption);

            if (adoptionSaved) {
                animalInApplication.setAdopted(true);
                animalDAO.updateAnimal(animalInApplication);

                if (mainController != null) mainController.refreshAnimalTable();
                if (adopterListController != null) adopterListController.refreshAdopterList();
                if (applicationListController != null) applicationListController.refreshApplicationList();

                applicationDAO.deleteAdoptionApplication(currentApplication.getApplicationId());
                if (applicationListController != null) applicationListController.refreshApplicationList();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Adoption Approved");
                alert.setContentText("Adoption for " + adopterUser.getName() + " and " + animalInApplication.getName() + " has been recorded.");
                alert.showAndWait();

                closeWindow();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to Save Adoption");
                alert.setContentText("Could not save the adoption record for user: " + adopterUser.getName());
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            if (adopterUser == null || adopterUser.getUserId() <= 0) {
                alert.setContentText("Adopter information is missing or invalid.");
            } else if (currentApplication == null) {
                alert.setContentText("Application details are missing.");
            } else if (animalInApplication == null) {
                alert.setContentText("Animal details for this application could not be found.");
            } else {
                alert.setContentText("Required information is missing to approve adoption.");
            }
            alert.showAndWait();
        }
    }

    @FXML
    private void handleReject() {
        if (currentApplication != null && adopterUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reject Application");
            alert.setHeaderText("Confirm Rejection");
            alert.setContentText("Are you sure you want to reject the application from " + adopterUser.getName() + " for " + (animalInApplication != null ? animalInApplication.getName() : "the animal") + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (applicationDAO.deleteAdoptionApplication(currentApplication.getApplicationId())) {
                    if (applicationListController != null) applicationListController.refreshApplicationList();
                    closeWindow();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to Reject Application");
                    errorAlert.setContentText("There was an error deleting the application for " + adopterUser.getName() + ".");
                    errorAlert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("No application selected or adopter details missing.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        if (adopterNameLabel != null && adopterNameLabel.getScene() != null) {
            Stage stage = (Stage) adopterNameLabel.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
