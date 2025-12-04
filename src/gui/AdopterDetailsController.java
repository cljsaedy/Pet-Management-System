package gui;

import database.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.util.Optional; // For Alert result

public class AdopterDetailsController {

    @FXML
    private Label idLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label contactLabel;
    @FXML
    private Label addressLabel;

    private User currentAdopter;
    private UserDAO userDAO = new UserDAO();
    private AdopterListController adopterListController; // To refresh the list after update/delete

    public void setAdopter(User adopter) {
        this.currentAdopter = adopter;
        if (adopter != null) {
            idLabel.setText(String.valueOf(adopter.getUserId()));
            nameLabel.setText(adopter.getName() != null ? adopter.getName() : "N/A");
            contactLabel.setText(adopter.getContactInfo() != null ? adopter.getContactInfo() : "N/A");
            addressLabel.setText(adopter.getAddress() != null ? adopter.getAddress() : "N/A");
        } else {
            clearDetails();
        }
    }

    public void setAdopterListController(AdopterListController controller) {
        this.adopterListController = controller;
    }

    private void clearDetails() {
        idLabel.setText("");
        nameLabel.setText("");
        contactLabel.setText("");
        addressLabel.setText("");
    }

    @FXML
    private void handleUpdateAdopter() {
        if (currentAdopter == null) {
            showAlert(Alert.AlertType.WARNING, "No Adopter Selected", "No adopter details to update.");
            return;
        }

        try {
            URL location = getClass().getClassLoader().getResource("gui/UpdateAdopterForm.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find UpdateAdopterForm.fxml. Check path.");
                showAlert(Alert.AlertType.ERROR, "FXML Error", "Cannot load the update adopter form.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            UpdateAdopterController updateController = loader.getController();
            updateController.setAdopter(currentAdopter); // Pass the current adopter to pre-fill the form
            updateController.setAdopterListController(adopterListController); // Pass for refreshing

            Stage stage = new Stage();
            stage.setTitle("Update Adopter: " + currentAdopter.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (nameLabel.getScene() != null && nameLabel.getScene().getWindow() != null) {
                stage.initOwner(nameLabel.getScene().getWindow());
            }
            stage.showAndWait();

            // After the update dialog is closed, refresh the details displayed in this dialog
            // and the main adopter list (the list refresh is handled by UpdateAdopterController if successful)
            User refreshedAdopter = userDAO.getUser(currentAdopter.getUserId());
            if (refreshedAdopter != null) {
                setAdopter(refreshedAdopter); // Update details in this view
            } else {
                // Adopter might have been deleted in another way, or DB error
                showAlert(Alert.AlertType.INFORMATION, "Adopter Not Found", "The adopter details could not be reloaded. They may have been deleted.");
                closeCurrentWindow();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load the update adopter form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAdopter() {
        if (currentAdopter == null) {
            showAlert(Alert.AlertType.WARNING, "No Adopter Selected", "No adopter selected for deletion.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Adopter");
        confirmAlert.setHeaderText("Confirm Delete: " + currentAdopter.getName());
        confirmAlert.setContentText("Are you sure you want to permanently delete this adopter (" + currentAdopter.getName() + ")? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = userDAO.deleteUser(currentAdopter.getUserId());
            if (deleted) {
                showAlert(Alert.AlertType.INFORMATION, "Deletion Successful", "Adopter " + currentAdopter.getName() + " has been deleted.");
                if (adopterListController != null) {
                    adopterListController.refreshAdopterList(); // Refresh the main list
                }
                closeCurrentWindow(); // Close this details window
            } else {
                showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete adopter " + currentAdopter.getName() + ". They might have associated records (e.g., adoptions, applications) or the user was already deleted.");
            }
        }
    }

    private void closeCurrentWindow() {
        if (nameLabel != null && nameLabel.getScene() != null && nameLabel.getScene().getWindow() != null) {
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
    
    // If you add a dedicated "Close" button to adopter_details.fxml
    @FXML
    private void handleCloseDetails() {
        closeCurrentWindow();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}