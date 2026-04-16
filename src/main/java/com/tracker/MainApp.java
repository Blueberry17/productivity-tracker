package com.tracker;

import com.tracker.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.initialise();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/tracker/views/MainView.fxml")
        );
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(
            getClass().getResource("/com/tracker/styles/app.css").toExternalForm()
        );
        stage.setTitle("Tracker");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
