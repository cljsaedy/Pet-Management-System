package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static MainSceneController mainControllerInstance;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen(); 
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/gui/LoginForm.fxml"));
            Parent loginRoot = loginLoader.load();

            Scene loginScene = new Scene(loginRoot);
            primaryStage.setTitle("FurEver Care Login");
            primaryStage.setScene(loginScene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MainSceneController getMainController() {
        return mainControllerInstance;
    }

    public static void setMainControllerInstance(MainSceneController controller) {
        mainControllerInstance = controller;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
