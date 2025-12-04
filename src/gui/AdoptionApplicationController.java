package gui;

import database.AdoptionApplicationDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.AdoptionApplication;

public class AdoptionApplicationController {

    @FXML
    private TextArea reasonTextArea;
    @FXML
    private RadioButton yesPetsRadio;
    @FXML
    private RadioButton noPetsRadio;
    @FXML
    private ToggleGroup hasPetsGroup;
    @FXML
    private ComboBox<String> livingSituationCombo;
    @FXML
    private ComboBox<String> contactPreferenceCombo;
    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;

    private int animalId;
    private int adopterId;
    private AnimalDetailsController animalDetailsController;
    private AdoptionApplicationDAO applicationDAO = new AdoptionApplicationDAO();

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public void setAdopterId(int adopterId) {
        this.adopterId = adopterId;
    }

    public void setAnimalDetailsController(AnimalDetailsController controller) {
        this.animalDetailsController = controller;
    }

    @FXML
    private void handleSubmitApplication() {
        String reason = reasonTextArea.getText();
        boolean hasPets = yesPetsRadio.isSelected();
        String livingSituation = livingSituationCombo.getValue();
        String contactPreference = contactPreferenceCombo.getValue();

        if (reason == null || reason.isEmpty() || livingSituation == null || livingSituation.isEmpty() || contactPreference == null || contactPreference.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all the fields.");
            alert.showAndWait();
            return;
        }

        AdoptionApplication application = new AdoptionApplication();
        application.setAnimalId(animalId);
        application.setUserId(adopterId);
        application.setReasonForAdoption(reason);
        application.setHasOtherPets(hasPets);
        application.setLivingSituation(livingSituation);
        application.setContactPreference(contactPreference);

        applicationDAO.saveAdoptionApplication(application);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Your adoption application has been submitted.");
        alert.showAndWait();

        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
        if (animalDetailsController != null) {
            animalDetailsController.loadHealthRecords();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
