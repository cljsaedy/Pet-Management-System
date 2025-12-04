package gui;

import database.HealthRecordDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.HealthRecord;

import java.time.LocalDate;

public class HealthRecordController {

    @FXML
    private DatePicker recordDatePicker;
    @FXML
    private TextField diagnosisField;
    @FXML
    private TextArea treatmentArea;
    @FXML
    private TextArea notesArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private int currentAnimalId;
    private HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private AnimalDetailsController animalDetailsController;

    public void setCurrentAnimalId(int animalId) {
        this.currentAnimalId = animalId;
    }

    public void setAnimalDetailsController(AnimalDetailsController controller) {
        this.animalDetailsController = controller;
    }

    @FXML
    private void handleSaveHealthRecord() {
        LocalDate recordDate = recordDatePicker.getValue();
        String diagnosis = diagnosisField.getText();
        String treatment = treatmentArea.getText();
        String notes = notesArea.getText();

        if (recordDate == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please select a record date.");
            alert.showAndWait();
            return;
        }

        HealthRecord newRecord = new HealthRecord();
        newRecord.setAnimalId(currentAnimalId);
        newRecord.setRecordDate(recordDate);
        newRecord.setDiagnosis(diagnosis);
        newRecord.setTreatment(treatment);
        newRecord.setNotes(notes);

        boolean success = healthRecordDAO.addHealthRecord(newRecord);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Health Record Added");
            alert.setContentText("The new health record has been added successfully.");
            alert.showAndWait();

            if (animalDetailsController != null) {
                animalDetailsController.loadHealthRecords();
            }

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Failed to add the new health record to the database.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}