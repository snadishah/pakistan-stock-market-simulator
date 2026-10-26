package com.psms.view;

import com.psms.model.Session;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Login and Registration view with Binance-style dark aesthetic.
 */
public class LoginView {

    private final Stage stage;
    private final Session session;
    private final Runnable onLoginSuccess;

    // Colors
    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String INPUT_BG = "#0c1a30";
    private static final String INPUT_BORDER = "#1a3a5c";
    private static final String ERROR_COLOR = "#f6465d";
    private static final String SUCCESS_COLOR = "#0ecb81";

    public LoginView(Stage stage, Session session, Runnable onLoginSuccess) {
        this.stage = stage;
        this.session = session;
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Shows the login screen.
     */
    public void show() {
        // Root container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Main card
        VBox card = new VBox(20);
        card.setMaxWidth(420);
        card.setMaxHeight(600);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + INPUT_BORDER + ";" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;"
        );

        // Drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(30);
        card.setEffect(shadow);

        // Logo / App Title
        Text logo = new Text("📊");
        logo.setFont(Font.font("Segoe UI Emoji", 48));

        Text appTitle = new Text("Pakistan Stock Exchange");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        appTitle.setFill(Color.web(ACCENT_COLOR));

        Text subtitle = new Text("Market Simulator");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        VBox titleBox = new VBox(5, logo, appTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // Tab selector for Login/Register
        HBox tabBar = new HBox();
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setSpacing(0);

        Button loginTab = createTabButton("Login", true);
        Button registerTab = createTabButton("Register", false);

        tabBar.getChildren().addAll(loginTab, registerTab);

        // Input fields
        Label usernameLabel = createLabel("Username");
        TextField usernameField = createTextField("Enter your username");

        Label passwordLabel = createLabel("Password");
        PasswordField passwordField = createPasswordField("Enter your password");

        // Confirm password (for registration, initially hidden)
        Label confirmLabel = createLabel("Confirm Password");
        confirmLabel.setVisible(false);
        confirmLabel.setManaged(false);
        PasswordField confirmField = createPasswordField("Confirm your password");
        confirmField.setVisible(false);
        confirmField.setManaged(false);

        // Status message
        Label statusLabel = new Label();
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(340);
        statusLabel.setAlignment(Pos.CENTER);

        // Submit button
        Button submitButton = new Button("Login");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setPrefHeight(45);
        submitButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        submitButton.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        // Hover effects for submit button
        submitButton.setOnMouseEntered(e -> submitButton.setStyle(
                "-fx-background-color: #00A3D9;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        ));
        submitButton.setOnMouseExited(e -> submitButton.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        ));

        // Tab switching logic
        final boolean[] isLoginMode = {true};

        loginTab.setOnAction(e -> {
            isLoginMode[0] = true;
            loginTab.setStyle(getTabStyle(true));
            registerTab.setStyle(getTabStyle(false));
            confirmLabel.setVisible(false);
            confirmLabel.setManaged(false);
            confirmField.setVisible(false);
            confirmField.setManaged(false);
            submitButton.setText("Login");
            statusLabel.setText("");
        });

        registerTab.setOnAction(e -> {
            isLoginMode[0] = false;
            registerTab.setStyle(getTabStyle(true));
            loginTab.setStyle(getTabStyle(false));
            confirmLabel.setVisible(true);
            confirmLabel.setManaged(true);
            confirmField.setVisible(true);
            confirmField.setManaged(true);
            submitButton.setText("Create Account");
            statusLabel.setText("");
        });

        // Submit action
        submitButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please fill in all fields");
                statusLabel.setTextFill(Color.web(ERROR_COLOR));
                return;
            }

            if (isLoginMode[0]) {
                // Login
                if (session.login(username, password)) {
                    statusLabel.setText("Login successful!");
                    statusLabel.setTextFill(Color.web(SUCCESS_COLOR));
                    onLoginSuccess.run();
                } else {
                    statusLabel.setText("Invalid username or password");
                    statusLabel.setTextFill(Color.web(ERROR_COLOR));
                    shakeNode(card);
                }
            } else {
                // Register
                String confirm = confirmField.getText();
                if (!password.equals(confirm)) {
                    statusLabel.setText("Passwords do not match");
                    statusLabel.setTextFill(Color.web(ERROR_COLOR));
                    return;
                }
                if (password.length() < 4) {
                    statusLabel.setText("Password must be at least 4 characters");
                    statusLabel.setTextFill(Color.web(ERROR_COLOR));
                    return;
                }

                if (session.register(username, password)) {
                    statusLabel.setText("Account created successfully!");
                    statusLabel.setTextFill(Color.web(SUCCESS_COLOR));
                    onLoginSuccess.run();
                } else {
                    statusLabel.setText("Username already exists");
                    statusLabel.setTextFill(Color.web(ERROR_COLOR));
                    shakeNode(card);
                }
            }
        });

        // Enter key to submit
        passwordField.setOnAction(e -> submitButton.fire());
        confirmField.setOnAction(e -> submitButton.fire());

        // Layout
        VBox form = new VBox(8,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                confirmLabel, confirmField
        );
        form.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                titleBox,
                tabBar,
                form,
                statusLabel,
                submitButton
        );

        root.getChildren().add(card);

        Scene scene = new Scene(root, 500, 650);
        stage.setScene(scene);
        stage.setTitle("Pakistan Stock Market Simulator — Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private Button createTabButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(170);
        btn.setPrefHeight(38);
        btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btn.setStyle(getTabStyle(active));
        return btn;
    }

    private String getTabStyle(boolean active) {
        if (active) {
            return "-fx-background-color: transparent;" +
                   "-fx-text-fill: " + ACCENT_COLOR + ";" +
                   "-fx-border-color: transparent transparent " + ACCENT_COLOR + " transparent;" +
                   "-fx-border-width: 0 0 2 0;" +
                   "-fx-cursor: hand;";
        } else {
            return "-fx-background-color: transparent;" +
                   "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                   "-fx-border-color: transparent transparent " + INPUT_BORDER + " transparent;" +
                   "-fx-border-width: 0 0 2 0;" +
                   "-fx-cursor: hand;";
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web(TEXT_SECONDARY));
        return label;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setFont(Font.font("Segoe UI", 14));
        field.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + INPUT_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8 12;"
        );
        // Focus style
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                field.setStyle(field.getStyle().replace(
                        "-fx-border-color: " + INPUT_BORDER,
                        "-fx-border-color: " + ACCENT_COLOR));
            } else {
                field.setStyle(field.getStyle().replace(
                        "-fx-border-color: " + ACCENT_COLOR,
                        "-fx-border-color: " + INPUT_BORDER));
            }
        });
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setFont(Font.font("Segoe UI", 14));
        field.setStyle(
                "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + INPUT_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8 12;"
        );
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                field.setStyle(field.getStyle().replace(
                        "-fx-border-color: " + INPUT_BORDER,
                        "-fx-border-color: " + ACCENT_COLOR));
            } else {
                field.setStyle(field.getStyle().replace(
                        "-fx-border-color: " + ACCENT_COLOR,
                        "-fx-border-color: " + INPUT_BORDER));
            }
        });
        return field;
    }

    /**
     * Simple shake animation for invalid login.
     */
    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition shake =
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }
}
