package gui;

import database.AnimalDAO;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Animal;
import model.User;
import util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainSceneController implements Initializable {

    @FXML
    private TableView<Animal> animalTableView;

    @FXML
    private TableColumn<Animal, Integer> animalIdColumn;

    @FXML
    private TableColumn<Animal, String> nameColumn;

    @FXML
    private TableColumn<Animal, String> speciesColumn;

    @FXML
    private TableColumn<Animal, String> breedColumn;

    @FXML
    private TableColumn<Animal, Integer> ageColumn;

    @FXML
    private TableColumn<Animal, String> genderColumn;

    @FXML
    private TableColumn<Animal, Boolean> adoptedColumn;

    @FXML
    private TableColumn<Animal, String> adoptionStatusColumn;

    @FXML
    private Button addAnimalButton;

    @FXML
    private Button adopterListButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button adoptionApplicationsButton;

    private AnimalDAO animalDAO = new AnimalDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Animal> animalList = FXCollections.observableArrayList();
    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser.getName() + " (" + loggedInUser.getRole() + ")");
        }
        updateUIBasedOnRole();
    }

    private void updateUIBasedOnRole() {
        boolean isStaffOrAdmin = loggedInUser != null && ("admin".equalsIgnoreCase(loggedInUser.getRole()) || "staff".equalsIgnoreCase(loggedInUser.getRole()));

        addAnimalButton.setVisible(isStaffOrAdmin);
        adopterListButton.setVisible(isStaffOrAdmin);
        adoptionApplicationsButton.setVisible(isStaffOrAdmin);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animalIdColumn.setCellValueFactory(new PropertyValueFactory<>("animalId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        speciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));
        breedColumn.setCellValueFactory(new PropertyValueFactory<>("breed"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        adoptionStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isAdopted() ? "Adopted" : "Not Adopted"));
        adoptionStatusColumn.setText("Adoption Status");

        loadAnimals();
        animalTableView.setItems(animalList);

        animalTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                Animal selectedAnimal = animalTableView.getSelectionModel().getSelectedItem();
                if (selectedAnimal != null) {
                    openAnimalDetailsDialog(selectedAnimal);
                }
            }
        });

        if (SessionManager.getCurrentUser() != null) {
            setLoggedInUser(SessionManager.getCurrentUser());
        } else {
            updateUIBasedOnRole();
        }
    }

    public TableView<Animal> getAnimalTableView() {
        return animalTableView;
    }

    public void loadAnimals() {
        animalList.clear();
        List<Animal> animals = animalDAO.getAllAnimals(true);
        animalList.addAll(animals);
    }

    public void refreshAnimalTable() {
        loadAnimals();
    }

    public List<User> getAdopters() {
        return userDAO.getAllUsers().stream()
                .filter(user -> "user".equalsIgnoreCase(user.getRole()))
                .collect(Collectors.toList());
    }

    public List<Animal> getAllAvailableAnimals() {
        return animalDAO.getAllAnimals(true).stream()
                .filter(animal -> !animal.isAdopted())
                .collect(Collectors.toList());
    }

    private void openAnimalDetailsDialog(Animal animal) {
        try {
            URL location = getClass().getClassLoader().getResource("gui/AnimalDetails.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find AnimalDetails.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            AnimalDetailsController controller = loader.getController();
            controller.setAnimal(animal);
            controller.setMainController(this);
            controller.setLoggedInUser(loggedInUser);

            Stage stage = new Stage();
            stage.setTitle("Animal Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAddAnimalDialog() {
        try {
            URL location = getClass().getClassLoader().getResource("gui/AddAnimalForm.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find AddAnimalForm.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            AddAnimalController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Animal");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadAnimals();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAdopterListDialog() {
        try {
            URL location = getClass().getClassLoader().getResource("gui/adopter_list.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find adopter_list.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            AdopterListController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Adopter List");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAdoptionApplicationsList() {
        try {
            URL location = getClass().getClassLoader().getResource("gui/AdoptionApplicationList.fxml");
            if (location == null) {
                System.err.println("Error: Cannot find AdoptionApplicationList.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            AdoptionApplicationListController controller = loader.getController();

            // THIS WAS MISSING
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Adoption Applications");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            controller.setStage(stage);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddAnimal() {
        openAddAnimalDialog();
    }

    @FXML
    private void handleAdopterList() {
        openAdopterListDialog();
    }

    @FXML
    private void handleAdoptionApplications() {
        openAdoptionApplicationsList();
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            URL loginViewUrl = getClass().getClassLoader().getResource("gui/loginform.fxml");
            if (loginViewUrl == null) {
                System.err.println("Error: Cannot find loginform.fxml");
                return;
            }
            Parent loginRoot = FXMLLoader.load(loginViewUrl);
            Scene loginScene = new Scene(loginRoot);
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("FurEverCare Login");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
