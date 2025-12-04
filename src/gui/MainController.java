package gui;

import database.AnimalDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Animal;
import util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TableView<Animal> animalTableView;

    @FXML
    private TableColumn<Animal, Integer> animalIdColumn;

    @FXML
    private TableColumn<Animal, String> animalNameColumn;

    @FXML
    private TableColumn<Animal, String> animalSpeciesColumn;

    @FXML
    private TableColumn<Animal, Integer> animalAgeColumn;

    @FXML
    private TableColumn<Animal, String> animalHealthColumn;

    @FXML
    private TableColumn<Animal, Boolean> animalAdoptedColumn;

    @FXML
    private TableColumn<Animal, Animal> actionColumn;

    @FXML
    private Button addAnimalButton;

    @FXML
    private Button registerAdopterButton;

    @FXML
    private Button recordAdoptionButton;

    @FXML
    private Button listAdoptionsButton;

    @FXML
    private Button exitButton;

    private AnimalDAO animalDAO;
    private ObservableList<Animal> animalList = FXCollections.observableArrayList();
    private static MainController instance;

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animalDAO = new AnimalDAO();

        animalIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        animalNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        animalSpeciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));
        animalAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        animalHealthColumn.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));
        animalAdoptedColumn.setCellValueFactory(new PropertyValueFactory<>("adopted"));

        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionColumn.setCellFactory(param -> new TableCell<Animal, Animal>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(updateButton, deleteButton);

            {
                buttonBox.setSpacing(5);
            }

            @Override
            protected void updateItem(Animal animal, boolean empty) {
                super.updateItem(animal, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    updateButton.setOnAction(event -> openUpdateAnimalForm(animal));
                    deleteButton.setOnAction(event -> handleDeleteAnimal(animal));
                    setGraphic(buttonBox);
                    setText(null);
                }
            }
        });

        loadAnimals();
        animalTableView.setItems(animalList);
        applyAuthorization();
    }

    private void applyAuthorization() {
        String role = SessionManager.getCurrentUserRole();
        if ("user".equals(role)) {
            addAnimalButton.setVisible(false);
            addAnimalButton.setManaged(false);
            registerAdopterButton.setVisible(false);
            registerAdopterButton.setManaged(false);
            recordAdoptionButton.setVisible(false);
            recordAdoptionButton.setManaged(false);
            actionColumn.setVisible(false);
        }
    }

    public void loadAnimals() {
        animalList.clear();
        List<Animal> allAnimals = animalDAO.getAllAnimals(true);
        animalList.addAll(allAnimals);
    }

    private void openUpdateAnimalForm(Animal animal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/UpdateAnimalForm.fxml"));
            Parent root = loader.load();
            UpdateAnimalController controller = loader.getController();
            controller.setAnimal(animal);

            Stage stage = new Stage();
            stage.setTitle("Update Animal");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAnimals();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteAnimal(Animal animal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete " + animal.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                animalDAO.deleteAnimal(animal.getAnimalId());
                loadAnimals();
            }
        });
    }

    @FXML
    private void handleAddAnimal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AddAnimalForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Animal");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterAdopter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/RegisterAdopterForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Register Adopter");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecordAdoption() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/RecordAdoptionForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Record Adoption");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleListAdoptions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/adoption_list.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Adoption History");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
