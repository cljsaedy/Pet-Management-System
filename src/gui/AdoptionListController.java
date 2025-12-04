package gui;

import database.AdopterDAO;
import database.AdoptionDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Adoption;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate; // <-- added
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdoptionListController implements Initializable {

    @FXML
    private TableView<Adoption> adoptionTableView;

    @FXML
    private TableColumn<Adoption, Integer> adoptionIdColumn;

    @FXML
    private TableColumn<Adoption, String> animalNameColumn;

    @FXML
    private TableColumn<Adoption, String> adopterNameColumn;

    @FXML
    private TableColumn<Adoption, LocalDate> adoptionDateColumn;

    @FXML
    private TableColumn<Adoption, Adoption> viewDetailsColumn;

    private AdoptionDAO adoptionDAO;
    private AdopterDAO adopterDAO;
    private ObservableList<Adoption> adoptionList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        adoptionDAO = new AdoptionDAO();
        adopterDAO = new AdopterDAO();

        adoptionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        animalNameColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getAnimal().getName())
        );
        adopterNameColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getAdopter().getName())
        );

        adoptionDateColumn.setCellValueFactory(new PropertyValueFactory<>("adoptionDate"));

        adoptionDateColumn.setCellFactory(column -> new TableCell<Adoption, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        viewDetailsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        viewDetailsColumn.setCellFactory(param -> new TableCell<Adoption, Adoption>() {
            private final Button viewDetailsButton = new Button("View Details");

            @Override
            protected void updateItem(Adoption adoption, boolean empty) {
                super.updateItem(adoption, empty);
                if (empty || adoption == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    viewDetailsButton.setOnAction(event -> 
                        handleViewAdopterDetails(adoption.getAdopter().getUserId())
                    );
                    setGraphic(viewDetailsButton);
                    setText(null);
                }
            }
        });

        loadAdoptions();
        adoptionTableView.setItems(adoptionList);
    }

    private void loadAdoptions() {
        adoptionList.clear();
        List<Adoption> allAdoptions = adoptionDAO.getAllAdoptionsWithDetails();
        adoptionList.addAll(allAdoptions);
    }

    private void handleViewAdopterDetails(int adopterId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AdopterDetailsForm.fxml"));
            Parent root = loader.load();

            AdopterDetailsController controller = loader.getController();
            User adopter = adopterDAO.getAdopter(adopterId);
            controller.setAdopter(adopter);

            Stage stage = new Stage();
            stage.setTitle("Adopter Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
