package gui;

import database.AdoptionApplicationDAO;
import database.UserDAO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.AdoptionApplication;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdoptionApplicationListController implements Initializable {

    @FXML
    private TableView<AdoptionApplication> applicationsTableView;
    @FXML
    private TableColumn<AdoptionApplication, String> animalNameColumn;
    @FXML
    private TableColumn<AdoptionApplication, String> applicantNameColumn;
    @FXML
    private TableColumn<AdoptionApplication, String> reasonColumn;
    @FXML
    private TableColumn<AdoptionApplication, Boolean> hasPetsColumn;
    @FXML
    private TableColumn<AdoptionApplication, String> livingSituationColumn;
    @FXML
    private TableColumn<AdoptionApplication, String> contactPreferenceColumn;
    @FXML
    private TableColumn<AdoptionApplication, String> applicationDateColumn;
    @FXML
    private Label titleLabel;

    private final AdoptionApplicationDAO applicationDAO = new AdoptionApplicationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<AdoptionApplication> applicationList = FXCollections.observableArrayList();

    private Stage stage;
    private MainSceneController mainController;
    private AdopterListController adopterListController;

    public void setStage(Stage stage) {
        this.stage = stage;
        if (this.stage != null && applicationsTableView != null) {
            applicationsTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if (this.stage != null && this.stage.getScene() != null) {
                    this.stage.sizeToScene();
                }
            });
            if (this.stage.getScene() != null) {
                this.stage.sizeToScene();
            }
        }
    }

    public void setMainController(MainSceneController mainController) {
        this.mainController = mainController;
    }

    public void setAdopterListController(AdopterListController adopterListController) {
        this.adopterListController = adopterListController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (titleLabel != null) {
            titleLabel.setAlignment(Pos.CENTER);
            titleLabel.setMaxWidth(Double.MAX_VALUE);
        }

        animalNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAnimalName()));
        applicantNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApplicantName()));
        reasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReasonForAdoption()));
        livingSituationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLivingSituation()));
        contactPreferenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactPreference()));

        applicationDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getApplicationDate() != null) {
                try {
                    return new SimpleStringProperty(cellData.getValue().getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    System.err.println("Error formatting application date: " + e.getMessage());
                    return new SimpleStringProperty("Invalid Date");
                }
            } else {
                return new SimpleStringProperty("");
            }
        });

        hasPetsColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isHasOtherPets()));
        hasPetsColumn.setCellFactory(new Callback<TableColumn<AdoptionApplication, Boolean>, TableCell<AdoptionApplication, Boolean>>() {
            @Override
            public TableCell<AdoptionApplication, Boolean> call(TableColumn<AdoptionApplication, Boolean> column) {
                return new TableCell<AdoptionApplication, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item ? "Yes" : "No");
                        }
                    }
                };
            }
        });

        loadAdoptionApplications();
        applicationsTableView.setItems(applicationList);

        applicationsTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                AdoptionApplication selectedApplication = applicationsTableView.getSelectionModel().getSelectedItem();
                if (selectedApplication != null) {
                    openApproveAdoptionDialog(selectedApplication);
                }
            }
        });
    }

    private void openApproveAdoptionDialog(AdoptionApplication application) {
        try {
            URL location = getClass().getClassLoader().getResource("gui/ApproveAdoptionForm.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find ApproveAdoptionForm.fxml. Check path in resources.");
                Alert alert = new Alert(Alert.AlertType.ERROR, "Critical Error: Cannot find the approval form FXML (ApproveAdoptionForm.fxml). Please check application resources.");
                alert.setTitle("FXML Load Error");
                alert.showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            ApproveAdoptionController approveController = loader.getController();
            User applicantUser = userDAO.getUser(application.getUserId());

            if (applicantUser != null) {
                approveController.setMainController(mainController);
                approveController.setApplicationListController(this);
                if (this.adopterListController != null) {
                    approveController.setAdopterListController(this.adopterListController);
                }
                approveController.setAdopterUser(applicantUser);
                approveController.setCurrentApplication(application);

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Process Adoption Application");
                dialogStage.setScene(new Scene(root));
                dialogStage.initModality(Modality.APPLICATION_MODAL);

                if (this.stage != null) {
                    dialogStage.initOwner(this.stage);
                } else if (applicationsTableView.getScene() != null && applicationsTableView.getScene().getWindow() != null) {
                    dialogStage.initOwner(applicationsTableView.getScene().getWindow());
                }

                dialogStage.showAndWait();
                refreshApplicationList();
            } else {
                System.err.println("Error: Could not find user details for user ID: " + application.getUserId());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User Not Found");
                alert.setHeaderText("Cannot Process Application");
                alert.setContentText("The user associated with this application (User ID: " + application.getUserId() + ") could not be found in the database. The application cannot be processed.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading the approval form: " + e.getMessage());
            alert.setTitle("FXML Load Error");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "An unexpected error occurred while opening the approval form: " + e.getMessage());
            alert.setTitle("Unexpected Error");
            alert.showAndWait();
        }
    }

    public void loadAdoptionApplications() {
        applicationList.clear();
        List<AdoptionApplication> applications = applicationDAO.getAllAdoptionApplications();
        if (applications != null) {
            applicationList.addAll(applications);
        } else {
            System.err.println("Warning: getAllAdoptionApplications() returned null. No applications loaded.");
        }
    }

    public void refreshApplicationList() {
        loadAdoptionApplications();
    }
}
