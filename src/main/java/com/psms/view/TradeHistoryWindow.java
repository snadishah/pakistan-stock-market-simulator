package com.psms.view;

import com.psms.manager.TradeHistoryManager;
import com.psms.model.TradeRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class TradeHistoryWindow {

    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String PANEL_BORDER = "#1a3a5c";
    private static final String BUY_COLOR = "#0ecb81";
    private static final String SELL_COLOR = "#f6465d";

    private final Stage stage;
    private final TradeHistoryManager tradeHistory;
    private final Runnable onBack;

    public TradeHistoryWindow(Stage stage, TradeHistoryManager tradeHistory, Runnable onBack) {
        this.stage = stage;
        this.tradeHistory = tradeHistory;
        this.onBack = onBack;
    }

    public void show() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Back + Header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = createBackButton();
        Label title = new Label("📋 Trade History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(ACCENT_COLOR));

        headerRow.getChildren().addAll(backBtn, title);

        Label summary = new Label("Total Trades: " + tradeHistory.getTotalTrades());
        summary.setFont(Font.font("Consolas", 13));
        summary.setTextFill(Color.web(TEXT_SECONDARY));

        // Table
        TableView<TradeRecord> table = new TableView<>();
        table.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-table-cell-border-color: " + PANEL_BORDER + ";" +
                "-fx-control-inner-background: " + PANEL_COLOR + ";" +
                "-fx-control-inner-background-alt: " + BG_COLOR + ";");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<TradeRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTimestamp().substring(0, 10)));
        dateCol.setPrefWidth(100);

        TableColumn<TradeRecord, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTimestamp().substring(11)));
        timeCol.setPrefWidth(80);

        TableColumn<TradeRecord, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAction().name()));
        actionCol.setPrefWidth(70);
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(item.equals("BUY") ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
                    setFont(Font.font("Consolas", FontWeight.BOLD, 12));
                }
            }
        });

        TableColumn<TradeRecord, String> symCol = new TableColumn<>("Stock");
        symCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getSymbol()));
        symCol.setPrefWidth(80);

        TableColumn<TradeRecord, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        qtyCol.setPrefWidth(70);

        TableColumn<TradeRecord, String> priceCol = new TableColumn<>("Price (PKR)");
        priceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        priceCol.setPrefWidth(100);

        TableColumn<TradeRecord, String> totalCol = new TableColumn<>("Total (PKR)");
        totalCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%,.0f", data.getValue().getPrice() * data.getValue().getQuantity())));
        totalCol.setPrefWidth(100);

        table.getColumns().addAll(dateCol, timeCol, actionCol, symCol, qtyCol, priceCol, totalCol);

        ObservableList<TradeRecord> records = FXCollections.observableArrayList(tradeHistory.getHistory());
        FXCollections.reverse(records);
        table.setItems(records);

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        refreshBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 6; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            ObservableList<TradeRecord> updated = FXCollections.observableArrayList(tradeHistory.getHistory());
            FXCollections.reverse(updated);
            table.setItems(updated);
            summary.setText("Total Trades: " + tradeHistory.getTotalTrades());
        });

        root.getChildren().addAll(headerRow, summary, table, refreshBtn);

        SceneSwitcher.switchScene(stage, root, "📋 Trade History — PSX Simulator");
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
