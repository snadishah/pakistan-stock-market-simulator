/* 
package com.psms.view;

import java.util.List;

import com.psms.manager.StockPredictor;
import com.psms.model.Market;
import com.psms.model.Stock;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PredictionWindow {

    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String PANEL_BORDER = "#1a3a5c";
    private static final String BUY_COLOR = "#0ecb81";
    private static final String SELL_COLOR = "#f6465d";

    private final Stage stage;
    private final Market market;
    private final Runnable onBack;
    private VBox predictionsBox;
    private javafx.animation.Timeline refreshTimeline;

    public PredictionWindow(Stage stage, Market market, Runnable onBack) {
        this.stage = stage;
        this.market = market;
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
        Label title = new Label("🔮 AI Stock Predictions");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ACCENT_COLOR));

        headerRow.getChildren().addAll(backBtn, title);

        Label subtitle = new Label("Predictions based on price trends, momentum & market news sentiment");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web(TEXT_SECONDARY));
        subtitle.setWrapText(true);

        Label disclaimer = new Label("⚠️ Predictions are simulated for educational purposes only. Not financial advice.");
        disclaimer.setFont(Font.font("Segoe UI", 10));
        disclaimer.setTextFill(Color.web("#f0b90b"));
        disclaimer.setWrapText(true);
        disclaimer.setPadding(new Insets(5, 10, 5, 10));
        disclaimer.setStyle("-fx-background-color: #0a1a0a; -fx-background-radius: 6; -fx-border-color: #f0b90b44; -fx-border-radius: 6;");

        // Column headers
        HBox headers = new HBox();
        headers.setPadding(new Insets(8, 10, 8, 10));
        headers.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-background-radius: 6;");

        Label hStock = createHeaderLabel("Stock", 70);
        Label hPrice = createHeaderLabel("Price", 80);
        Label hChange = createHeaderLabel("Change", 70);
        Label hSentiment = createHeaderLabel("News", 72);
        Label hPrediction = createHeaderLabel("Prediction", 85);
        Label hConfidence = createHeaderLabel("Confidence", 80);
        Label hNews = createHeaderLabel("Latest News", 250);

        headers.getChildren().addAll(hStock, hPrice, hChange, hSentiment, hPrediction, hConfidence, hNews);

        // Scrollable predictions list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        predictionsBox = new VBox(2);
        scrollPane.setContent(predictionsBox);

        root.getChildren().addAll(headerRow, subtitle, disclaimer, headers, scrollPane);

        SceneSwitcher.switchScene(stage, root, "🔮 AI Predictions — PSX Simulator");

        // Initial update
        updatePredictions();

        // Auto-refresh every 3 seconds
        refreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> updatePredictions()));
        refreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopRefresh() {
        if (refreshTimeline != null) refreshTimeline.stop();
    }

    private void updatePredictions() {
        predictionsBox.getChildren().clear();

        List<String> symbols = (List<String>) market.getStockSymbols();
        for (String symbol : symbols) {
            Stock stock = market.getStock(symbol);
            if (stock == null) continue;

            StockPredictor.Prediction prediction = StockPredictor.predict(stock);
            HBox row = createPredictionRow(stock, prediction);
            predictionsBox.getChildren().add(row);
        }
    }

    private HBox createPredictionRow(Stock stock, StockPredictor.Prediction prediction) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-background-radius: 6;");

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #142a4a; -fx-background-radius: 6; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + PANEL_COLOR + "; -fx-background-radius: 6;"));

        Label symLabel = new Label(stock.getSymbol());
        symLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        symLabel.setTextFill(Color.web(TEXT_COLOR));
        symLabel.setPrefWidth(70);

        Label priceLabel = new Label(String.format("%.2f", stock.getPrice()));
        priceLabel.setFont(Font.font("Consolas", 12));
        priceLabel.setTextFill(Color.web(TEXT_COLOR));
        priceLabel.setPrefWidth(80);

        double change = stock.getChangePercent();
        Label changeLabel = new Label(String.format("%+.2f%%", change));
        changeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        changeLabel.setTextFill(change >= 0 ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
        changeLabel.setPrefWidth(70);

        Label sentimentLabel = new Label(prediction.getNewsSentiment());
        sentimentLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        sentimentLabel.setPrefWidth(72);
        switch (prediction.getNewsSentiment()) {
            case "Positive" -> sentimentLabel.setTextFill(Color.web(BUY_COLOR));
            case "Negative" -> sentimentLabel.setTextFill(Color.web(SELL_COLOR));
            default -> sentimentLabel.setTextFill(Color.web(TEXT_SECONDARY));
        }

        Label predLabel = new Label(prediction.getDirectionEmoji() + " " + prediction.getDirectionText());
        predLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        predLabel.setPrefWidth(85);
        predLabel.setPadding(new Insets(2, 6, 2, 6));
        switch (prediction.getDirection()) {
            case UP -> {
                predLabel.setTextFill(Color.web(BUY_COLOR));
                predLabel.setStyle("-fx-background-color: #0ecb8122; -fx-background-radius: 4;");
            }
            case DOWN -> {
                predLabel.setTextFill(Color.web(SELL_COLOR));
                predLabel.setStyle("-fx-background-color: #f6465d22; -fx-background-radius: 4;");
            }
            case NEUTRAL -> {
                predLabel.setTextFill(Color.web(TEXT_SECONDARY));
                predLabel.setStyle("-fx-background-color: #7a8fa622; -fx-background-radius: 4;");
            }
        }

        Label confLabel = new Label(String.format("%.0f%%", prediction.getConfidence() * 100));
        confLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        confLabel.setTextFill(Color.web(ACCENT_COLOR));
        confLabel.setPrefWidth(80);

        String newsText = StockPredictor.getLatestNews(stock.getSymbol());
        Label newsLabel = new Label(newsText);
        newsLabel.setFont(Font.font("Segoe UI", 10));
        newsLabel.setTextFill(Color.web(TEXT_SECONDARY));
        newsLabel.setWrapText(true);
        newsLabel.setPrefWidth(250);

        row.getChildren().addAll(symLabel, priceLabel, changeLabel, sentimentLabel, predLabel, confLabel, newsLabel);
        return row;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.web(TEXT_SECONDARY));
        label.setPrefWidth(width);
        return label;
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
*/