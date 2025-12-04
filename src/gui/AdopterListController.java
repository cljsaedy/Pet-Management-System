package gui;

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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Adoption;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdopterListController implements Initializable {

    @FXML
    private TableView<User> adopterTableView;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> contactColumn;
    @FXML
    private TableColumn<User, String> addressColumn;
    @FXML
    private TableColumn<User, String> adoptedAnimalColumn;

    private MainSceneController mainController;
    private ObservableList<User> adopterList = FXCollections.observableArrayList();
    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final UserDAO userDAO = new UserDAO();

    public void setMainController(MainSceneController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        adoptedAnimalColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user != null && user.getUserId() > 0) {
                Adoption adoption = adoptionDAO.getAdoptionByAdopterId(user.getUserId());
                if (adoption != null && adoption.getAnimalName() != null) {
                    return new SimpleStringProperty(adoption.getAnimalName());
                } else {
                    return new SimpleStringProperty("None / Pending");
                }
            }
            return new SimpleStringProperty("N/A");
        });

        adopterTableView.setItems(adopterList);
        loadAdopters();

        adopterTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                User selectedUser = adopterTableView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    openAdopterDetailsDialog(selectedUser);
                }
            }
        });
    }

    private void loadAdopters() {
        adopterList.clear();
        List<User> allSystemUsers = userDAO.getAllUsers();
        if (allSystemUsers != null) {
            List<User> potentialAdopters = allSystemUsers.stream()
                                             .filter(user -> "user".equalsIgnoreCase(user.getRole()))
                                             .collect(Collectors.toList());
            adopterList.addAll(potentialAdopters);
        } else {
            System.err.println("Warning: UserDAO.getAllUsers() returned null.");
        }
    }

    private void openAdopterDetailsDialog(User adopter) {
        try {
            URL location = getClass().getClassLoader().getResource("gui/adopter_details.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find adopter_details.fxml. Check path.");
                showAlert("FXML Error", "Cannot load adopter details form.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            AdopterDetailsController detailsController = loader.getController();
            detailsController.setAdopter(adopter);
            detailsController.setAdopterListController(this);

            Stage stage = new Stage();
            stage.setTitle("Adopter Details: " + adopter.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            if (adopterTableView.getScene() != null && adopterTableView.getScene().getWindow() != null) {
                stage.initOwner(adopterTableView.getScene().getWindow());
            }
            stage.showAndWait();
            refreshAdopterList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Load Error", "Could not load the adopter details screen: " + e.getMessage());
        }
    }

    public void refreshAdopterList() {
        loadAdopters();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}