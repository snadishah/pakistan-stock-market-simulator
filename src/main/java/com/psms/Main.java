package com.psms;

import com.psms.model.Session;
import com.psms.view.DashboardView;
import com.psms.view.LoginView;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set global exception handler to catch any silent errors
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in thread: " + thread.getName());
            throwable.printStackTrace();
        });

        try {
            // Ensure data directory exists
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // Create session manager
            Session session = new Session();

            // Show login view first
            LoginView loginView = new LoginView(primaryStage, session, () -> {
                // On successful login, show dashboard
                DashboardView dashboard = new DashboardView(primaryStage, session);
                dashboard.show();
            });
            loginView.show();

            System.out.println("Application started successfully!");

        } catch (Exception e) {
            System.err.println("ERROR: Failed to start application:");
            e.printStackTrace();

            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        System.out.println("Launching Pakistan Stock Market Simulator...");
        launch(args);
    }
}
