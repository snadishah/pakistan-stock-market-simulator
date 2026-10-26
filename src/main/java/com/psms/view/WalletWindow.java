package com.psms.view;

import java.util.List;

import com.psms.model.Portfolio;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class WalletWindow {

    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String INPUT_BG = "#0c1a30";
    private static final String PANEL_BORDER = "#1a3a5c";
    private static final String BUY_COLOR = "#0ecb81";
    private static final String SELL_COLOR = "#f6465d";

    private final Stage stage;
    private final Portfolio portfolio;
    private final Runnable onBack;
    private Label balanceLabel;
    private VBox transactionHistoryBox;

    public WalletWindow(Stage stage, Portfolio portfolio, Runnable onBack) {
        this.stage = stage;
        this.portfolio = portfolio;
        this.onBack = onBack;
    }

    public void show() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Back + Header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = createBackButton();
        Label title = new Label("💰 Wallet");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ACCENT_COLOR));

        headerRow.getChildren().addAll(backBtn, title);

        // Balance display card
        VBox balanceCard = new VBox(5);
        balanceCard.setPadding(new Insets(20));
        balanceCard.setAlignment(Pos.CENTER);
        balanceCard.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 12;");

        Label balanceTitle = new Label("Available Balance");
        balanceTitle.setFont(Font.font("Segoe UI", 13));
        balanceTitle.setTextFill(Color.web(TEXT_SECONDARY));

        balanceLabel = new Label(String.format("PKR %,.2f", portfolio.getBalance()));
        balanceLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 28));
        balanceLabel.setTextFill(Color.web(ACCENT_COLOR));

        balanceCard.getChildren().addAll(balanceTitle, balanceLabel);

        // Tabs for Deposit / Withdraw
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab depositTab = new Tab("💳 Deposit");
        depositTab.setContent(createDepositPane());

        Tab withdrawTab = new Tab("🏧 Withdraw");
        withdrawTab.setContent(createWithdrawPane());

        Tab historyTab = new Tab("📜 History");
        historyTab.setContent(createHistoryPane());

        tabPane.getTabs().addAll(depositTab, withdrawTab, historyTab);

        root.getChildren().addAll(headerRow, balanceCard, tabPane);

        SceneSwitcher.switchScene(stage, root, "💰 Wallet — PSX Simulator");
    }

    private VBox createDepositPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label instruction = new Label("Enter amount to deposit (PKR)");
        instruction.setFont(Font.font("Segoe UI", 13));
        instruction.setTextFill(Color.web(TEXT_COLOR));

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 100000");
        amountField.setPrefHeight(45);
        amountField.setFont(Font.font("Consolas", 16));
        amountField.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 8 12;");

        HBox quickAmounts = new HBox(8);
        quickAmounts.setAlignment(Pos.CENTER);
        int[] amounts = {10000, 50000, 100000, 500000};
        for (int amt : amounts) {
            Button btn = new Button(String.format("%,d", amt));
            btn.setPrefHeight(35);
            btn.setFont(Font.font("Consolas", 11));
            btn.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                    "-fx-text-fill: " + ACCENT_COLOR + ";" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: " + PANEL_BORDER + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #142a4a;" +
                    "-fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + ACCENT_COLOR + "; -fx-border-radius: 6; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                    "-fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + PANEL_BORDER + "; -fx-border-radius: 6; -fx-cursor: hand;"));
            btn.setOnAction(e -> amountField.setText(String.valueOf(amt)));
            quickAmounts.getChildren().add(btn);
        }

        Label status = new Label();
        status.setFont(Font.font("Segoe UI", 13));
        status.setWrapText(true);

        Button depositBtn = new Button("Deposit Funds");
        depositBtn.setMaxWidth(Double.MAX_VALUE);
        depositBtn.setPrefHeight(45);
        depositBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        depositBtn.setStyle("-fx-background-color: " + BUY_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;");
        depositBtn.setOnMouseEntered(e -> depositBtn.setStyle("-fx-background-color: #0ba36a;" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));
        depositBtn.setOnMouseExited(e -> depositBtn.setStyle("-fx-background-color: " + BUY_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));

        depositBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim().replace(",", ""));
                if (portfolio.deposit(amount)) {
                    status.setText("✅ Successfully deposited PKR " + String.format("%,.2f", amount));
                    status.setTextFill(Color.web(BUY_COLOR));
                    balanceLabel.setText(String.format("PKR %,.2f", portfolio.getBalance()));
                    amountField.clear();
                    refreshTransactionHistory();
                } else {
                    status.setText("❌ Invalid amount. Please enter a positive value.");
                    status.setTextFill(Color.web(SELL_COLOR));
                }
            } catch (NumberFormatException ex) {
                status.setText("❌ Please enter a valid number.");
                status.setTextFill(Color.web(SELL_COLOR));
            }
        });

        pane.getChildren().addAll(instruction, amountField, quickAmounts, depositBtn, status);
        return pane;
    }

    private VBox createWithdrawPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label instruction = new Label("Enter amount to withdraw (PKR)");
        instruction.setFont(Font.font("Segoe UI", 13));
        instruction.setTextFill(Color.web(TEXT_COLOR));

        Label availableLabel = new Label("Available: PKR " + String.format("%,.2f", portfolio.getBalance()));
        availableLabel.setFont(Font.font("Consolas", 12));
        availableLabel.setTextFill(Color.web(TEXT_SECONDARY));

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 50000");
        amountField.setPrefHeight(45);
        amountField.setFont(Font.font("Consolas", 16));
        amountField.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 8 12;");

        HBox quickPercent = new HBox(8);
        quickPercent.setAlignment(Pos.CENTER);
        int[] percents = {25, 50, 75, 100};
        for (int pct : percents) {
            Button btn = new Button(pct + "%");
            btn.setPrefHeight(35);
            btn.setFont(Font.font("Consolas", 11));
            btn.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                    "-fx-text-fill: " + ACCENT_COLOR + ";" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: " + PANEL_BORDER + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #142a4a;" +
                    "-fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + ACCENT_COLOR + "; -fx-border-radius: 6; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                    "-fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 6;" +
                    "-fx-border-color: " + PANEL_BORDER + "; -fx-border-radius: 6; -fx-cursor: hand;"));
            btn.setOnAction(e -> {
                double val = portfolio.getBalance() * pct / 100.0;
                amountField.setText(String.format("%.0f", val));
            });
            quickPercent.getChildren().add(btn);
        }

        Label status = new Label();
        status.setFont(Font.font("Segoe UI", 13));
        status.setWrapText(true);

        Button withdrawBtn = new Button("Withdraw Funds");
        withdrawBtn.setMaxWidth(Double.MAX_VALUE);
        withdrawBtn.setPrefHeight(45);
        withdrawBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        withdrawBtn.setStyle("-fx-background-color: " + SELL_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;");
        withdrawBtn.setOnMouseEntered(e -> withdrawBtn.setStyle("-fx-background-color: #d4394e;" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));
        withdrawBtn.setOnMouseExited(e -> withdrawBtn.setStyle("-fx-background-color: " + SELL_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));

        withdrawBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim().replace(",", ""));
                if (portfolio.withdraw(amount)) {
                    status.setText("✅ Successfully withdrew PKR " + String.format("%,.2f", amount));
                    status.setTextFill(Color.web(BUY_COLOR));
                    balanceLabel.setText(String.format("PKR %,.2f", portfolio.getBalance()));
                    availableLabel.setText("Available: PKR " + String.format("%,.2f", portfolio.getBalance()));
                    amountField.clear();
                    refreshTransactionHistory();
                } else if (amount > portfolio.getBalance()) {
                    status.setText("❌ Insufficient balance. Available: PKR " +
                            String.format("%,.2f", portfolio.getBalance()));
                    status.setTextFill(Color.web(SELL_COLOR));
                } else {
                    status.setText("❌ Invalid amount. Please enter a positive value.");
                    status.setTextFill(Color.web(SELL_COLOR));
                }
            } catch (NumberFormatException ex) {
                status.setText("❌ Please enter a valid number.");
                status.setTextFill(Color.web(SELL_COLOR));
            }
        });

        pane.getChildren().addAll(instruction, availableLabel, amountField, quickPercent, withdrawBtn, status);
        return pane;
    }

    private VBox createHistoryPane() {
        VBox pane = new VBox(8);
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label headerLabel = new Label("📜 Transaction History");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        headerLabel.setTextFill(Color.web(TEXT_COLOR));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        transactionHistoryBox = new VBox(3);
        scroll.setContent(transactionHistoryBox);

        pane.getChildren().addAll(headerLabel, scroll);
        refreshTransactionHistory();
        return pane;
    }

    private void refreshTransactionHistory() {
        if (transactionHistoryBox == null) return;
        transactionHistoryBox.getChildren().clear();

        List<String[]> txns = portfolio.getTransactions();
        if (txns.isEmpty()) {
            Label empty = new Label("No transactions yet");
            empty.setFont(Font.font("Segoe UI", 12));
            empty.setTextFill(Color.web(TEXT_SECONDARY));
            transactionHistoryBox.getChildren().add(empty);
            return;
        }

        for (int i = txns.size() - 1; i >= 0; i--) {
            String[] tx = txns.get(i);
            String type = tx[0];
            String amount = tx[1];
            String timestamp = tx[2];

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 10, 8, 10));
            row.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-background-radius: 6;");

            Label icon = new Label(type.equals("DEPOSIT") ? "💳" : "🏧");
            icon.setFont(Font.font(16));

            Label typeLabel = new Label(type);
            typeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            typeLabel.setTextFill(type.equals("DEPOSIT") ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
            typeLabel.setPrefWidth(90);

            Label amountLabel = new Label((type.equals("DEPOSIT") ? "+" : "-") + "PKR " + amount);
            amountLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            amountLabel.setTextFill(type.equals("DEPOSIT") ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label timeLabel = new Label(timestamp);
            timeLabel.setFont(Font.font("Consolas", 10));
            timeLabel.setTextFill(Color.web(TEXT_SECONDARY));

            row.getChildren().addAll(icon, typeLabel, amountLabel, spacer, timeLabel);
            transactionHistoryBox.getChildren().add(row);
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
        btn.setOnAction(e -> onBack.run());
        return btn;
    }
}
