package com.psms.view;

import com.psms.manager.ChatManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChatWindow {

    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String INPUT_BG = "#0c1a30";
    private static final String PANEL_BORDER = "#1a3a5c";

    private final Stage stage;
    private final String username;
    private final Runnable onBack;
    private TextArea chatArea;
    private javafx.animation.Timeline refreshTimeline;

    public ChatWindow(Stage stage, String username, Runnable onBack) {
        this.stage = stage;
        this.username = username;
        this.onBack = onBack;
    }

    public void show() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Back button + Header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = createBackButton();
        Label title = new Label("💬 Trader Chat");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(ACCENT_COLOR));

        headerRow.getChildren().addAll(backBtn, title);

        Label subtitle = new Label("Chat with fellow traders in real-time");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web(TEXT_SECONDARY));

        // Chat display
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setFont(Font.font("Consolas", 12));
        chatArea.setStyle("-fx-control-inner-background: " + PANEL_COLOR + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;");
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Online users indicator
        Label onlineLabel = new Label("🟢 Online as: " + username);
        onlineLabel.setFont(Font.font("Segoe UI", 11));
        onlineLabel.setTextFill(Color.web("#0ecb81"));

        // Input area
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField chatInput = new TextField();
        chatInput.setPromptText("Type a message...");
        chatInput.setPrefHeight(40);
        chatInput.setFont(Font.font("Segoe UI", 13));
        chatInput.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 5 10;");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        Button sendBtn = new Button("Send 📤");
        sendBtn.setPrefHeight(40);
        sendBtn.setPrefWidth(100);
        sendBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        sendBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;");
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle("-fx-background-color: #00A3D9;" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));

        sendBtn.setOnAction(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                ChatManager.sendMessage(username, msg); // Fixed method call
                chatInput.clear();
                updateChat();
            }
        });
        chatInput.setOnAction(e -> sendBtn.fire());

        inputRow.getChildren().addAll(chatInput, sendBtn);

        root.getChildren().addAll(headerRow, subtitle, onlineLabel, chatArea, inputRow);

        SceneSwitcher.switchScene(stage, root, "💬 Trader Chat — PSX Simulator");

        // Initial load
        updateChat();

        // Auto-refresh every 2 seconds
        refreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> updateChat()));
        refreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopRefresh() {
        if (refreshTimeline != null) refreshTimeline.stop();
    }

    private void updateChat() {
        java.util.List<String[]> messages = ChatManager.getMessages(); // Fixed method call
        StringBuilder sb = new StringBuilder();
        for (String[] msg : messages) {
            String time = msg[2].length() > 11 ? msg[2].substring(11) : msg[2];
            sb.append("[").append(time).append("] ");
            sb.append(msg[0]).append(": ").append(msg[1]).append("\n");
        }
        String newContent = sb.toString();
        if (!newContent.equals(chatArea.getText())) {
            chatArea.setText(newContent);
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    private Button createBackButton() {
        Button btn = new Button("← Back");
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setPrefHeight(32);
        btn.setStyle("-fx-background-color: " + PANEL_BORDER + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PANEL_BORDER + ";" +
                "-fx-text-fill: " + TEXT_COLOR + "; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnAction(e -> {
            stopRefresh();
            onBack.run();
        });
        return btn;
    }
}