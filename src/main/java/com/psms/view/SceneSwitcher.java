package com.psms.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    public static void switchScene(Stage stage, Parent root, String title) {
        // 1. Set the new title
        stage.setTitle(title);

        // 2. UNLOCK THE WINDOW (Keeps the maximize button working after login)
        stage.setResizable(true);
        stage.setMinWidth(1200);
        stage.setMinHeight(700);

        // 3. The Secret to Zero-Flicker: Root Swapping!
        if (stage.getScene() != null) {
            // If the window is already open, just silently swap the content inside.
            // This prevents the OS from shrinking and re-maximizing the window!
            stage.getScene().setRoot(root);
        } else {
            // Fallback: If this is the very first time opening the dashboard from login
            double width = stage.getWidth();
            double height = stage.getHeight();

            if (Double.isNaN(width) || width < 1000) width = 1400;
            if (Double.isNaN(height) || height < 700) height = 850;

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
        }

        stage.show();
    }
}