package gui;

import database.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality; 
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

import java.io.IOException;
import java.net.URL; 

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessage;


    private UserDAO userDAO = new UserDAO();

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setText("Please enter both username and password.");
            return;
        }

        User user = userDAO.getUserByUsername(username);

        if (user != null && password.equals(user.getPassword())) { 
            SessionManager.setCurrentUser(user);
            errorMessage.setText(""); 
            loadDashboard(event);
        } else {
            errorMessage.setText("Invalid username or password.");
        }
    }

    @FXML
    protected void handleCancelLogin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void handleRegister(ActionEvent event) {
        try {
            URL registerFormUrl = getClass().getResource("/gui/RegisterForm.fxml");
            if (registerFormUrl == null) {
                errorMessage.setText("Error: Cannot find RegisterForm.fxml");
                System.err.println("Error: Cannot find /gui/RegisterForm.fxml. Check path and build.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(registerFormUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("User Registration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            
            stage.showAndWait(); 

        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.setText("Failed to load registration form: " + e.getMessage());
        }
    }

    private void loadDashboard(ActionEvent event) {
        String role = SessionManager.getCurrentUserRole();
        Node sourceNode = (Node) event.getSource();
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        Parent root;
        FXMLLoader loader;
        String fxmlPath = null;
        String dashboardTitle = "FurEver Care";

        try {
            if ("admin".equalsIgnoreCase(role) || "staff".equalsIgnoreCase(role)) {
                fxmlPath = "/gui/MainScene.fxml";
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                MainSceneController controller = loader.getController();
                controller.setLoggedInUser(SessionManager.getCurrentUser());
                dashboardTitle = "FurEver Care - Admin/Staff Dashboard";

            } else if ("user".equalsIgnoreCase(role)) {
                fxmlPath = "/gui/UserDashboard.fxml";
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                root = loader.load();
                UserDashboardController controller = loader.getController();
                controller.setLoggedInUser(SessionManager.getCurrentUser());
                dashboardTitle = "FurEver Care - Find Your Companion";
            } else {
                errorMessage.setText("Unknown user role: " + (role != null ? role : "null"));
                return;
            }

            if (root == null) {
                errorMessage.setText("Failed to load dashboard FXML: " + fxmlPath);
                System.err.println("Error: FXML file not found or failed to load: " + fxmlPath);
                return;
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(dashboardTitle);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.setText("Failed to load dashboard: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setText("An unexpected error occurred while loading dashboard.");
        }
    }
}
