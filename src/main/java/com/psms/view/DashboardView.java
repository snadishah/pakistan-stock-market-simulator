package com.psms.view;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psms.chart.CandlestickChart;
import com.psms.manager.AlertManager;
import com.psms.manager.ReviewManager;
import com.psms.manager.TradeHistoryManager;
import com.psms.manager.WatchlistManager;
import com.psms.model.Candle;
import com.psms.model.Market;
import com.psms.model.Portfolio;
import com.psms.model.PriceAlert;
import com.psms.model.Session;
import com.psms.model.Stock;
import com.psms.model.TradeRecord;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main dashboard view — Dark Navy + Electric Blue layout with trading features.
 * Layout: BorderPane with left panel (market + watchlist), center (charts),
 * right panel (profile + news + reviews), bottom (trading + portfolio).
 * Chat, History, Wallet, Support are in separate windows.
 */
public class DashboardView {

    private final Stage stage;
    private final Session session;
    private final Market market;
    private final Portfolio portfolio;
    private final TradeHistoryManager tradeHistory;
    private final WatchlistManager watchlist;
    private final AlertManager alertManager;

    // UI state
    private String selectedStock = "ENGRO";
    private String chartType = "Candlestick";
    private boolean darkTheme = true;

    // Colors — Dark Navy + Electric Blue theme
    private String BG_COLOR = "#0a1628";
    private String PANEL_COLOR = "#0f2040";
    private String PANEL_BORDER = "#1a3a5c";
    private String ACCENT_COLOR = "#00BFFF";
    private String TEXT_COLOR = "#eaecef";
    private String TEXT_SECONDARY = "#7a8fa6";
    private String BUY_COLOR = "#0ecb81";
    private String SELL_COLOR = "#f6465d";
    private String INPUT_BG = "#0c1a30";

    // Chart components
    private CandlestickChart candlestickChart;
    private Canvas lineChartCanvas;
    private Canvas barChartCanvas;
    private Canvas areaChartCanvas;
    private StackPane chartContainer;
    private Label chartTitleLabel;

    // UI components that need updating
    private VBox marketListBox;
    private VBox watchlistBox;
    private VBox portfolioBox;
    private VBox profileBox;
    private VBox newsBox;
    private Label balanceLabel;
    private Label totalValueLabel;
    private Label plLabel;
    private Label plPercentLabel;
    private VBox alertsBox;
    private Label selectedStockPrice;
    private Label selectedStockChange;
    private Label selectedStockName;
    private TextField quantityField;
    private Label aiSuggestionLabel;

    // Timeline for real-time updates
    private Timeline updateTimeline;

    public DashboardView(Stage stage, Session session) {
        this.stage = stage;
        this.session = session;
        this.market = new Market();
        this.portfolio = new Portfolio(session.getCurrentUserId());
        this.tradeHistory = new TradeHistoryManager(session.getCurrentUser());
        this.watchlist = new WatchlistManager(session.getCurrentUser());
        this.alertManager = new AlertManager(session.getCurrentUser());
    }

    /**
     * Shows the dashboard.
     */
    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Top bar
        root.setTop(createTopBar());

        // Left panel — Market list + Watchlist
        root.setLeft(createLeftPanel());

        // Center — Charts
        root.setCenter(createCenterPanel());

        // Right panel — Profile + News + Reviews
        root.setRight(createRightPanel());

        // Bottom — Trading + Portfolio + Chat
        root.setBottom(createBottomPanel());

        SceneSwitcher.switchScene(stage, root, "Pakistan Stock Market Simulator — Dashboard");
        // Start real-time updates
        startUpdates();

        // Handle cleanup on close
        stage.setOnCloseRequest(e -> {
            if (updateTimeline != null) updateTimeline.stop();
        });
    }

    // ═══════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════

    private HBox createTopBar() {
        HBox topBar = new HBox(8);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 15, 8, 15));
        topBar.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: transparent transparent " + PANEL_BORDER + " transparent;" +
                "-fx-border-width: 0 0 1 0;");

        // Logo
        Text logo = new Text("📊 PSX Simulator");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        logo.setFill(Color.web(ACCENT_COLOR));

        // Feature window buttons — each swaps the scene, back returns to dashboard
        Runnable backToDashboard = () -> {
            if (updateTimeline != null) updateTimeline.stop();
            new DashboardView(stage, session).show();
        };

        Button chatBtn = createSmallButton("💬 Chat");
        chatBtn.setOnAction(e -> {
            if (updateTimeline != null) updateTimeline.stop();
            new ChatWindow(stage, session.getCurrentUser(), backToDashboard).show();
        });

        Button historyBtn = createSmallButton("📋 History");
        historyBtn.setOnAction(e -> {
            if (updateTimeline != null) updateTimeline.stop();
            new TradeHistoryWindow(stage, tradeHistory, backToDashboard).show();
        });

        Button walletBtn = createSmallButton("💰 Wallet");
        walletBtn.setOnAction(e -> {
            if (updateTimeline != null) updateTimeline.stop();
            new WalletWindow(stage, portfolio, backToDashboard).show();
        });

        Button supportBtn = createSmallButton("🎧 Support");
        supportBtn.setOnAction(e -> {
            if (updateTimeline != null) updateTimeline.stop();
            new SupportWindow(stage, session.getCurrentUser(), backToDashboard).show();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User info
        Label userLabel = new Label("👤 " + session.getCurrentUser());
        userLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        userLabel.setTextFill(Color.web(TEXT_COLOR));

        balanceLabel = new Label("Balance: Loading...");
        balanceLabel.setFont(Font.font("Consolas", 13));
        balanceLabel.setTextFill(Color.web(ACCENT_COLOR));

        // Theme toggle
        Button themeBtn = createSmallButton(darkTheme ? "☀ Light" : "🌙 Dark");
        themeBtn.setOnAction(e -> toggleTheme(themeBtn));

        // Export button
        Button exportBtn = createSmallButton("📥 Export");
        exportBtn.setOnAction(e -> exportPortfolio());

        // Logout button
        Button logoutBtn = createSmallButton("🚪 Logout");
        logoutBtn.setStyle(logoutBtn.getStyle() + "-fx-text-fill: " + SELL_COLOR + ";");
        logoutBtn.setOnAction(e -> {
            if (updateTimeline != null) updateTimeline.stop();
            session.logout();
            new LoginView(stage, session, () -> new DashboardView(stage, session).show()).show();
        });

        topBar.getChildren().addAll(logo, chatBtn, historyBtn, walletBtn, supportBtn,
                spacer, userLabel, balanceLabel, themeBtn, exportBtn, logoutBtn);
        return topBar;
    }

    // ═══════════════════════════════════════════════
    // LEFT PANEL — Market List + Watchlist
    // ═══════════════════════════════════════════════

    private VBox createLeftPanel() {
        VBox left = new VBox(0);
        left.setPrefWidth(260);
        left.setMinWidth(240);
        left.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: transparent " + PANEL_BORDER + " transparent transparent;" +
                "-fx-border-width: 0 1 0 0;");

        // Search
        TextField search = new TextField();
        search.setPromptText("🔍 Search stocks...");
        search.setPrefHeight(35);
        search.setStyle(getInputStyle());
        VBox.setMargin(search, new Insets(8, 8, 4, 8));

        search.textProperty().addListener((obs, old, val) -> updateMarketList(val));

        // Market header
        HBox marketHeader = createSectionHeader("Market", "Price (PKR)");

        // Market list
        ScrollPane marketScroll = new ScrollPane();
        marketListBox = new VBox(0);
        marketScroll.setContent(marketListBox);
        marketScroll.setFitToWidth(true);
        marketScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        marketScroll.setPrefHeight(300);
        VBox.setVgrow(marketScroll, Priority.ALWAYS);

        // Watchlist header
        Label watchlistLabel = new Label("⭐ Watchlist");
        watchlistLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        watchlistLabel.setTextFill(Color.web(ACCENT_COLOR));
        watchlistLabel.setPadding(new Insets(10, 8, 5, 8));

        // Watchlist
        ScrollPane watchScroll = new ScrollPane();
        watchlistBox = new VBox(0);
        watchScroll.setContent(watchlistBox);
        watchScroll.setFitToWidth(true);
        watchScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        watchScroll.setPrefHeight(180);

        left.getChildren().addAll(search, marketHeader, marketScroll, watchlistLabel, watchScroll);
        return left;
    }

    // ═══════════════════════════════════════════════
    // CENTER PANEL — Charts
    // ═══════════════════════════════════════════════

    private VBox createCenterPanel() {
        VBox center = new VBox(0);
        center.setStyle("-fx-background-color: " + PANEL_COLOR + ";");

        // Stock info bar
        HBox infoBar = new HBox(20);
        infoBar.setAlignment(Pos.CENTER_LEFT);
        infoBar.setPadding(new Insets(10, 15, 10, 15));
        infoBar.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: transparent transparent " + PANEL_BORDER + " transparent;" +
                "-fx-border-width: 0 0 1 0;");

        selectedStockName = new Label("ENGRO - Engro Corporation");
        selectedStockName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        selectedStockName.setTextFill(Color.web(TEXT_COLOR));

        selectedStockPrice = new Label("0.00");
        selectedStockPrice.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
        selectedStockPrice.setTextFill(Color.web(BUY_COLOR));

        selectedStockChange = new Label("+0.00%");
        selectedStockChange.setFont(Font.font("Consolas", FontWeight.SEMI_BOLD, 14));
        selectedStockChange.setTextFill(Color.web(BUY_COLOR));

        // AI Suggestion
        aiSuggestionLabel = new Label("🤖 Analyzing...");
        aiSuggestionLabel.setFont(Font.font("Segoe UI", 11));
        aiSuggestionLabel.setTextFill(Color.web(ACCENT_COLOR));
        aiSuggestionLabel.setWrapText(true);
        aiSuggestionLabel.setMaxWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        infoBar.getChildren().addAll(selectedStockName, selectedStockPrice, selectedStockChange, spacer, aiSuggestionLabel);

        // Chart type buttons
        HBox chartButtons = new HBox(5);
        chartButtons.setAlignment(Pos.CENTER_LEFT);
        chartButtons.setPadding(new Insets(5, 15, 5, 15));
        chartButtons.setStyle("-fx-background-color: " + BG_COLOR + ";");

        String[] chartTypes = {"Candlestick", "Line", "Bar", "Area"};
        for (String type : chartTypes) {
            Button btn = new Button(type);
            btn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            btn.setPrefHeight(28);
            btn.setStyle(type.equals(chartType) ? getActiveChartBtnStyle() : getInactiveChartBtnStyle());
            btn.setOnAction(e -> {
                chartType = type;
                // Update button styles
                chartButtons.getChildren().forEach(n -> {
                    if (n instanceof Button b) {
                        b.setStyle(b.getText().equals(chartType) ? getActiveChartBtnStyle() : getInactiveChartBtnStyle());
                    }
                });
                updateChart();
            });
            chartButtons.getChildren().add(btn);
        }

        // Chart container
        chartContainer = new StackPane();
        chartContainer.setStyle("-fx-background-color: " + PANEL_COLOR + ";");
        VBox.setVgrow(chartContainer, Priority.ALWAYS);

        // Initialize charts
        candlestickChart = new CandlestickChart(700, 350);
        lineChartCanvas = new Canvas(700, 350);
        barChartCanvas = new Canvas(700, 350);
        areaChartCanvas = new Canvas(700, 350);

        chartContainer.getChildren().add(candlestickChart.getCanvas());

        // Make chart resize with container
        chartContainer.widthProperty().addListener((obs, old, val) -> {
            double w = val.doubleValue();
            candlestickChart.resize(w, chartContainer.getHeight());
            lineChartCanvas.setWidth(w);
            barChartCanvas.setWidth(w);
            areaChartCanvas.setWidth(w);
            updateChart();
        });
        chartContainer.heightProperty().addListener((obs, old, val) -> {
            double h = val.doubleValue();
            candlestickChart.resize(chartContainer.getWidth(), h);
            lineChartCanvas.setHeight(h);
            barChartCanvas.setHeight(h);
            areaChartCanvas.setHeight(h);
            updateChart();
        });

        center.getChildren().addAll(infoBar, chartButtons, chartContainer);
        return center;
    }

    // ═══════════════════════════════════════════════
    // RIGHT PANEL — Profile + News + Reviews
    // ═══════════════════════════════════════════════

    private VBox createRightPanel() {
        VBox right = new VBox(0);
        right.setPrefWidth(280);
        right.setMinWidth(260);
        right.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: transparent transparent transparent " + PANEL_BORDER + ";" +
                "-fx-border-width: 0 0 0 1;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Profile section
        profileBox = createProfileSection();
        content.getChildren().add(profileBox);

        // Price Alerts section
        content.getChildren().add(createAlertsSection());

        // News section
        newsBox = createNewsSection();
        content.getChildren().add(newsBox);

        // Reviews section
        content.getChildren().add(createReviewsSection());

        scrollPane.setContent(content);
        right.getChildren().add(scrollPane);
        return right;
    }

    // ═══════════════════════════════════════════════
    // BOTTOM PANEL — Trading + Portfolio + Chat
    // ═══════════════════════════════════════════════

    private HBox createBottomPanel() {
        HBox bottom = new HBox(0);
        bottom.setPrefHeight(250);
        bottom.setMinHeight(220);
        bottom.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: " + PANEL_BORDER + " transparent transparent transparent;" +
                "-fx-border-width: 1 0 0 0;");

        // Trading panel
        VBox trading = createTradingPanel();
        trading.setPrefWidth(350);
        trading.setMinWidth(320);

        // Portfolio panel (now takes remaining space)
        VBox portfolioPanel = createPortfolioPanel();
        HBox.setHgrow(portfolioPanel, Priority.ALWAYS);

        bottom.getChildren().addAll(trading, portfolioPanel);
        return bottom;
    }

    // ═══════════════════════════════════════════════
    // COMPONENT BUILDERS
    // ═══════════════════════════════════════════════

    private VBox createTradingPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: transparent " + PANEL_BORDER + " transparent transparent;" +
                "-fx-border-width: 0 1 0 0;");

        Label title = new Label("Place Order");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web(TEXT_COLOR));

        // Stock selection display
        Label stockLabel = new Label("Stock: " + selectedStock);
        stockLabel.setFont(Font.font("Consolas", 12));
        stockLabel.setTextFill(Color.web(ACCENT_COLOR));

        // Quantity
        Label qtyLabel = new Label("Quantity");
        qtyLabel.setFont(Font.font("Segoe UI", 11));
        qtyLabel.setTextFill(Color.web(TEXT_SECONDARY));

        quantityField = new TextField("10");
        quantityField.setPrefHeight(35);
        quantityField.setStyle(getInputStyle());

        // Current price display
        Label priceInfo = new Label("Market Price: Loading...");
        priceInfo.setFont(Font.font("Consolas", 11));
        priceInfo.setTextFill(Color.web(TEXT_SECONDARY));

        // Total cost display
        Label totalCost = new Label("Total: —");
        totalCost.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        totalCost.setTextFill(Color.web(TEXT_COLOR));

        quantityField.textProperty().addListener((obs, old, val) -> {
            try {
                int qty = Integer.parseInt(val.trim());
                Stock s = market.getStock(selectedStock);
                if (s != null) {
                    totalCost.setText(String.format("Total: %.2f PKR", s.getPrice() * qty));
                }
            } catch (NumberFormatException e) {
                totalCost.setText("Total: —");
            }
        });

        // Buy/Sell buttons
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        Button buyBtn = new Button("BUY");
        buyBtn.setPrefWidth(130);
        buyBtn.setPrefHeight(38);
        buyBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        buyBtn.setStyle("-fx-background-color: " + BUY_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
        buyBtn.setOnMouseEntered(e -> buyBtn.setStyle("-fx-background-color: #0ba36a; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));
        buyBtn.setOnMouseExited(e -> buyBtn.setStyle("-fx-background-color: " + BUY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));

        Button sellBtn = new Button("SELL");
        sellBtn.setPrefWidth(130);
        sellBtn.setPrefHeight(38);
        sellBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sellBtn.setStyle("-fx-background-color: " + SELL_COLOR + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;");
        sellBtn.setOnMouseEntered(e -> sellBtn.setStyle("-fx-background-color: #d4394e; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));
        sellBtn.setOnMouseExited(e -> sellBtn.setStyle("-fx-background-color: " + SELL_COLOR + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;"));

        // Trade status
        Label tradeStatus = new Label();
        tradeStatus.setFont(Font.font("Segoe UI", 11));
        tradeStatus.setWrapText(true);
        tradeStatus.setMaxWidth(280);

        buyBtn.setOnAction(e -> executeTrade(true, stockLabel, priceInfo, tradeStatus, totalCost));
        sellBtn.setOnAction(e -> executeTrade(false, stockLabel, priceInfo, tradeStatus, totalCost));

        buttons.getChildren().addAll(buyBtn, sellBtn);

        // Available balance
        Label availLabel = new Label("Available: " + String.format("%.0f PKR", portfolio.getBalance()));
        availLabel.setFont(Font.font("Consolas", 11));
        availLabel.setTextFill(Color.web(TEXT_SECONDARY));

        box.getChildren().addAll(title, stockLabel, qtyLabel, quantityField, priceInfo, totalCost, buttons, tradeStatus, availLabel);

        // Store references for updates
        box.setUserData(new Object[]{stockLabel, priceInfo, availLabel, totalCost});

        return box;
    }

    private void executeTrade(boolean isBuy, Label stockLabel, Label priceInfo, Label tradeStatus, Label totalCost) {
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                tradeStatus.setText("Invalid quantity");
                tradeStatus.setTextFill(Color.web(SELL_COLOR));
                return;
            }

            Stock stock = market.getStock(selectedStock);
            if (stock == null) return;

            if (isBuy) {
                if (portfolio.buy(selectedStock, qty, stock.getPrice())) {
                    tradeHistory.recordTrade(selectedStock, TradeRecord.Action.BUY, qty, stock.getPrice());
                    tradeStatus.setText(String.format("✅ Bought %d %s @ %.2f", qty, selectedStock, stock.getPrice()));
                    tradeStatus.setTextFill(Color.web(BUY_COLOR));
                } else {
                    tradeStatus.setText("❌ Insufficient balance");
                    tradeStatus.setTextFill(Color.web(SELL_COLOR));
                }
            } else {
                if (portfolio.sell(selectedStock, qty, stock.getPrice())) {
                    tradeHistory.recordTrade(selectedStock, TradeRecord.Action.SELL, qty, stock.getPrice());
                    tradeStatus.setText(String.format("✅ Sold %d %s @ %.2f", qty, selectedStock, stock.getPrice()));
                    tradeStatus.setTextFill(Color.web(BUY_COLOR));
                } else {
                    tradeStatus.setText("❌ Insufficient holdings");
                    tradeStatus.setTextFill(Color.web(SELL_COLOR));
                }
            }

            updatePortfolioDisplay();

        } catch (NumberFormatException ex) {
            tradeStatus.setText("Please enter a valid number");
            tradeStatus.setTextFill(Color.web(SELL_COLOR));
        }
    }

    private VBox createPortfolioPanel() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: transparent " + PANEL_BORDER + " transparent transparent;" +
                "-fx-border-width: 0 1 0 0;");

        Label title = new Label("📊 Portfolio");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web(TEXT_COLOR));

        // Analytics row
        HBox analytics = new HBox(15);
        analytics.setAlignment(Pos.CENTER_LEFT);

        totalValueLabel = new Label("Total: 0.00 PKR");
        totalValueLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        totalValueLabel.setTextFill(Color.web(ACCENT_COLOR));

        plLabel = new Label("P/L: 0.00");
        plLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));

        plPercentLabel = new Label("(0.00%)");
        plPercentLabel.setFont(Font.font("Consolas", 11));

        analytics.getChildren().addAll(totalValueLabel, plLabel, plPercentLabel);

        // Holdings list
        ScrollPane scroll = new ScrollPane();
        portfolioBox = new VBox(2);
        scroll.setContent(portfolioBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(3, 5, 3, 5));
        addPortfolioHeader(header);

        box.getChildren().addAll(title, analytics, header, scroll);
        return box;
    }

    private void addPortfolioHeader(HBox header) {
        Label sym = new Label("Stock");
        sym.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        sym.setTextFill(Color.web(TEXT_SECONDARY));
        sym.setPrefWidth(60);

        Label qty = new Label("Qty");
        qty.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        qty.setTextFill(Color.web(TEXT_SECONDARY));
        qty.setPrefWidth(40);

        Label avg = new Label("Avg");
        avg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        avg.setTextFill(Color.web(TEXT_SECONDARY));
        avg.setPrefWidth(65);

        Label cur = new Label("Current");
        cur.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        cur.setTextFill(Color.web(TEXT_SECONDARY));
        cur.setPrefWidth(65);

        Label pl = new Label("P/L");
        pl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        pl.setTextFill(Color.web(TEXT_SECONDARY));
        pl.setPrefWidth(80);

        header.getChildren().addAll(sym, qty, avg, cur, pl);
    }



    // ═══════════════════════════════════════════════
    // RIGHT PANEL SECTIONS
    // ═══════════════════════════════════════════════

    private VBox createProfileSection() {
        VBox box = createPanelSection("👤 Profile");

        Label user = new Label("User: " + session.getCurrentUser());
        user.setFont(Font.font("Consolas", 12));
        user.setTextFill(Color.web(TEXT_COLOR));

        Label bal = new Label("Balance: Loading...");
        bal.setFont(Font.font("Consolas", 12));
        bal.setTextFill(Color.web(ACCENT_COLOR));

        Label trades = new Label("Total Trades: " + tradeHistory.getTotalTrades());
        trades.setFont(Font.font("Consolas", 12));
        trades.setTextFill(Color.web(TEXT_COLOR));

        Label profitSummary = new Label("Profit: Calculating...");
        profitSummary.setFont(Font.font("Consolas", 12));
        profitSummary.setTextFill(Color.web(BUY_COLOR));

        box.getChildren().addAll(user, bal, trades, profitSummary);
        return box;
    }

    private VBox createAlertsSection() {
        VBox box = createPanelSection("🔔 Price Alerts");

        alertsBox = new VBox(3);

        // Add alert form
        HBox addRow = new HBox(5);
        addRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> condCombo = new ComboBox<>();
        condCombo.getItems().addAll("Above", "Below");
        condCombo.setValue("Above");
        condCombo.setPrefWidth(70);
        condCombo.setStyle("-fx-background-color: " + INPUT_BG + "; -fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 10;");

        TextField threshField = new TextField();
        threshField.setPromptText("Price");
        threshField.setPrefWidth(70);
        threshField.setPrefHeight(28);
        threshField.setStyle(getInputStyle() + "-fx-font-size: 10;");

        Button addAlertBtn = new Button("+");
        addAlertBtn.setPrefSize(28, 28);
        addAlertBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: #ffffff; -fx-background-radius: 4; -fx-font-weight: bold; -fx-cursor: hand;");
        addAlertBtn.setOnAction(e -> {
            try {
                double thresh = Double.parseDouble(threshField.getText().trim());
                PriceAlert.Condition cond = condCombo.getValue().equals("Above") ?
                        PriceAlert.Condition.ABOVE : PriceAlert.Condition.BELOW;
                alertManager.addAlert(selectedStock, cond, thresh);
                threshField.clear();
                updateAlertsDisplay();
            } catch (NumberFormatException ex) {
                // ignore
            }
        });

        addRow.getChildren().addAll(new Label(selectedStock) {{
            setTextFill(Color.web(ACCENT_COLOR));
            setFont(Font.font("Consolas", 10));
        }}, condCombo, threshField, addAlertBtn);

        box.getChildren().addAll(addRow, alertsBox);
        updateAlertsDisplay();
        return box;
    }

    private VBox createNewsSection() {
        VBox box = createPanelSection("📰 Market News");

        String[] headlines = {
                "🟢 PSX index gains 250 pts amid strong buying",
                "📊 ENGRO announces quarterly dividend of PKR 15/share",
                "🏦 HBL reports 18% growth in net profit",
                "⚡ Energy sector stocks rally on policy news",
                "📈 SYS hits 52-week high on IT export boom",
                "🔴 Oil prices dip; OGDC, PPL under slight pressure",
                "💰 Foreign investors show renewed interest in PKR bonds",
                "🏗️ LUCK Cement expansion project on track",
                "📱 TRG Pakistan to acquire AI startup"
        };

        for (String headline : headlines) {
            Label news = new Label(headline);
            news.setFont(Font.font("Segoe UI", 11));
            news.setTextFill(Color.web(TEXT_COLOR));
            news.setWrapText(true);
            news.setPadding(new Insets(3, 0, 3, 0));
            news.setStyle("-fx-border-color: transparent transparent " + PANEL_BORDER + " transparent;" +
                    "-fx-border-width: 0 0 1 0;");
            box.getChildren().add(news);
        }

        return box;
    }

    private VBox createReviewsSection() {
        VBox box = createPanelSection("⭐ App Reviews");

        // Rating display
        Label avgLabel = new Label("Average: Loading...");
        avgLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        avgLabel.setTextFill(Color.web(ACCENT_COLOR));

        // Submit review
        HBox ratingRow = new HBox(3);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        final int[] selectedRating = {5};

        for (int i = 1; i <= 5; i++) {
            final int star = i;
            Button starBtn = new Button("★");
            starBtn.setFont(Font.font(14));
            starBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR + "; -fx-cursor: hand; -fx-padding: 2;");
            starBtn.setOnAction(e -> {
                selectedRating[0] = star;
                // Update star colors
                int idx = 0;
                for (var node : ratingRow.getChildren()) {
                    idx++;
                    if (node instanceof Button b) {
                        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                                (idx <= star ? ACCENT_COLOR : TEXT_SECONDARY) + "; -fx-cursor: hand; -fx-padding: 2;");
                    }
                }
            });
            ratingRow.getChildren().add(starBtn);
        }

        TextField reviewField = new TextField();
        reviewField.setPromptText("Write a review...");
        reviewField.setPrefHeight(28);
        reviewField.setStyle(getInputStyle() + "-fx-font-size: 11;");

        Button submitReview = new Button("Submit");
        submitReview.setPrefHeight(28);
        submitReview.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        submitReview.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: #ffffff; -fx-background-radius: 4; -fx-cursor: hand;");

        Label reviewStatus = new Label();
        reviewStatus.setFont(Font.font("Segoe UI", 10));

        // Review list
        VBox reviewsList = new VBox(4);

        submitReview.setOnAction(e -> {
            String comment = reviewField.getText().trim();
            if (!comment.isEmpty()) {
                ReviewManager.submitReview(session.getCurrentUser(), selectedRating[0], comment);
                reviewField.clear();
                reviewStatus.setText("Review submitted!");
                reviewStatus.setTextFill(Color.web(BUY_COLOR));
                updateReviewsDisplay(avgLabel, reviewsList);
            }
        });

        box.getChildren().addAll(avgLabel, ratingRow, reviewField, submitReview, reviewStatus, reviewsList);
        updateReviewsDisplay(avgLabel, reviewsList);
        return box;
    }

    // ═══════════════════════════════════════════════
    // UPDATE METHODS
    // ═══════════════════════════════════════════════

    private void startUpdates() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // Tick market prices
            market.tick();

            // Update all UI components
            updateMarketList("");
            updateWatchlistDisplay();
            updateSelectedStockInfo();
            updateChart();
            updatePortfolioDisplay();
            updateBalanceDisplay();
            updateProfileSection();
            checkAlerts();
            updateAISuggestions();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        // Initial update
        updateMarketList("");
        updateWatchlistDisplay();
        updateSelectedStockInfo();
        updateChart();
        updatePortfolioDisplay();
        updateBalanceDisplay();
        updateProfileSection();
    }

    private void updateMarketList(String filter) {
        marketListBox.getChildren().clear();

        for (String symbol : market.getStockSymbols()) {
            Stock stock = market.getStock(symbol);
            if (filter != null && !filter.isEmpty() &&
                    !symbol.toLowerCase().contains(filter.toLowerCase()) &&
                    !stock.getCompanyName().toLowerCase().contains(filter.toLowerCase()) &&
                    !stock.getSector().toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            HBox row = createStockRow(stock);
            marketListBox.getChildren().add(row);
        }
    }

    private HBox createStockRow(Stock stock) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 8, 6, 8));

        boolean isSelected = stock.getSymbol().equals(selectedStock);
        boolean isWatchlisted = watchlist.contains(stock.getSymbol());

        row.setStyle("-fx-background-color: " + (isSelected ? "#142a4a" : "transparent") + ";" +
                "-fx-cursor: hand;");

        // Watchlist star
        Label star = new Label(isWatchlisted ? "⭐" : "☆");
        star.setFont(Font.font(12));
        star.setCursor(javafx.scene.Cursor.HAND);
        star.setOnMouseClicked(e -> {
            watchlist.toggle(stock.getSymbol());
            updateWatchlistDisplay();
            updateMarketList("");
            e.consume();
        });

        // Symbol
        Label symLabel = new Label(stock.getSymbol());
        symLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        symLabel.setTextFill(Color.web(TEXT_COLOR));
        symLabel.setPrefWidth(55);

        // Price
        Label priceLabel = new Label(String.format("%.2f", stock.getPrice()));
        priceLabel.setFont(Font.font("Consolas", 12));

        // Change
        double change = stock.getChangePercent();
        Label changeLabel = new Label(String.format("%+.2f%%", change));
        changeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 11));

        if (change >= 0) {
            priceLabel.setTextFill(Color.web(BUY_COLOR));
            changeLabel.setTextFill(Color.web(BUY_COLOR));
        } else {
            priceLabel.setTextFill(Color.web(SELL_COLOR));
            changeLabel.setTextFill(Color.web(SELL_COLOR));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(star, symLabel, spacer, priceLabel, changeLabel);

        // Click to select stock
        row.setOnMouseClicked(e -> selectStock(stock.getSymbol()));
        row.setOnMouseEntered(e -> {
            if (!stock.getSymbol().equals(selectedStock))
                row.setStyle("-fx-background-color: #142a4a; -fx-cursor: hand;");
        });
        row.setOnMouseExited(e -> {
            if (!stock.getSymbol().equals(selectedStock))
                row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        });

        return row;
    }

    private void selectStock(String symbol) {
        selectedStock = symbol;
        updateSelectedStockInfo();
        updateChart();
        updateMarketList("");

        // Update trading panel stock label
        VBox bottomBox = (VBox) ((HBox) ((BorderPane) stage.getScene().getRoot()).getBottom()).getChildren().get(0);
        Object userData = bottomBox.getUserData();
        if (userData instanceof Object[] refs) {
            ((Label) refs[0]).setText("Stock: " + selectedStock);
            Stock s = market.getStock(selectedStock);
            if (s != null) {
                ((Label) refs[1]).setText("Market Price: " + String.format("%.2f PKR", s.getPrice()));
                try {
                    int qty = Integer.parseInt(quantityField.getText().trim());
                    ((Label) refs[3]).setText(String.format("Total: %.2f PKR", s.getPrice() * qty));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    private void updateSelectedStockInfo() {
        Stock stock = market.getStock(selectedStock);
        if (stock == null) return;

        selectedStockName.setText(stock.getSymbol() + " — " + stock.getCompanyName()
                + (stock.getSector().isEmpty() ? "" : "  ·  " + stock.getSector()));
        selectedStockPrice.setText(String.format("%.2f PKR", stock.getPrice()));

        double change = stock.getChangePercent();
        selectedStockChange.setText(String.format("%+.2f (%.2f%%)", stock.getChange(), change));

        if (change >= 0) {
            selectedStockPrice.setTextFill(Color.web(BUY_COLOR));
            selectedStockChange.setTextFill(Color.web(BUY_COLOR));
        } else {
            selectedStockPrice.setTextFill(Color.web(SELL_COLOR));
            selectedStockChange.setTextFill(Color.web(SELL_COLOR));
        }
    }

    private void updateChart() {
        Stock stock = market.getStock(selectedStock);
        if (stock == null) return;

        chartContainer.getChildren().clear();

        switch (chartType) {
            case "Candlestick":
                List<Double> ma = calculateMovingAverageFromCandles(stock.getCandles(), 10);
                candlestickChart.render(stock.getCandles(), ma, stock.getSymbol() + " — Candlestick");
                chartContainer.getChildren().add(candlestickChart.getCanvas());
                break;
            case "Line":
                renderLineChart(stock);
                chartContainer.getChildren().add(lineChartCanvas);
                break;
            case "Bar":
                renderBarChart(stock);
                chartContainer.getChildren().add(barChartCanvas);
                break;
            case "Area":
                renderAreaChart(stock);
                chartContainer.getChildren().add(areaChartCanvas);
                break;
        }
    }

    private List<Double> calculateMovingAverageFromCandles(List<Candle> candles, int period) {
        List<Double> ma = new ArrayList<>();
        if (candles.size() < period) return ma;
        for (int i = period - 1; i < candles.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += candles.get(j).getClose();
            }
            ma.add(sum / period);
        }
        return ma;
    }

    private void renderLineChart(Stock stock) {
        List<Double> prices = stock.getPriceHistory();
        double w = lineChartCanvas.getWidth();
        double h = lineChartCanvas.getHeight();
        GraphicsContext gc = lineChartCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#0f2040"));
        gc.fillRect(0, 0, w, h);

        if (prices.isEmpty()) return;

        double minP = prices.stream().min(Double::compare).orElse(0.0) * 0.999;
        double maxP = prices.stream().max(Double::compare).orElse(1.0) * 1.001;
        double range = maxP - minP;
        if (range < 0.01) range = 1;

        double margin = 60;
        double chartW = w - margin - 20;
        double chartH = h - 50;

        // Grid
        gc.setStroke(Color.web("#1a3a5c"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 6; i++) {
            double y = 20 + (i * chartH / 6);
            gc.strokeLine(20, y, w - margin, y);
            double price = maxP - (i * range / 6);
            gc.setFill(Color.web("#7a8fa6"));
            gc.setFont(Font.font("Consolas", 9));
            gc.fillText(String.format("%.1f", price), w - margin + 5, y + 4);
        }

        // Line
        gc.setStroke(Color.web(ACCENT_COLOR));
        gc.setLineWidth(2);

        List<Double> ma = stock.getMovingAverage(10);

        for (int i = 1; i < prices.size(); i++) {
            double x1 = 20 + ((i - 1.0) / (prices.size() - 1)) * chartW;
            double y1 = 20 + chartH * (1 - (prices.get(i - 1) - minP) / range);
            double x2 = 20 + ((double) i / (prices.size() - 1)) * chartW;
            double y2 = 20 + chartH * (1 - (prices.get(i) - minP) / range);
            gc.strokeLine(x1, y1, x2, y2);
        }

        // MA overlay
        if (ma.size() > 1) {
            gc.setStroke(Color.web("#f6465d"));
            gc.setLineWidth(1);
            int offset = prices.size() - ma.size();
            for (int i = 1; i < ma.size(); i++) {
                int ii = i + offset;
                int pi = ii - 1;
                double x1 = 20 + ((double) pi / (prices.size() - 1)) * chartW;
                double y1 = 20 + chartH * (1 - (ma.get(i - 1) - minP) / range);
                double x2 = 20 + ((double) ii / (prices.size() - 1)) * chartW;
                double y2 = 20 + chartH * (1 - (ma.get(i) - minP) / range);
                gc.strokeLine(x1, y1, x2, y2);
            }
        }

        // Title
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", 12));
        gc.fillText(stock.getSymbol() + " — Line Chart", 25, 15);

        // Legend
        gc.setFill(Color.web(ACCENT_COLOR));
        gc.fillText("— Price", 200, 15);
        gc.setFill(Color.web("#f6465d"));
        gc.fillText("— MA(10)", 270, 15);
    }

    private void renderBarChart(Stock stock) {
        List<Double> prices = stock.getPriceHistory();
        double w = barChartCanvas.getWidth();
        double h = barChartCanvas.getHeight();
        GraphicsContext gc = barChartCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#0f2040"));
        gc.fillRect(0, 0, w, h);

        if (prices.size() < 2) return;

        double minP = prices.stream().min(Double::compare).orElse(0.0) * 0.999;
        double maxP = prices.stream().max(Double::compare).orElse(1.0) * 1.001;
        double range = maxP - minP;
        if (range < 0.01) range = 1;

        double margin = 60;
        double chartW = w - margin - 20;
        double chartH = h - 50;
        double barWidth = Math.max(2, chartW / prices.size() - 1);

        // Grid
        gc.setStroke(Color.web("#1a3a5c"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 6; i++) {
            double y = 20 + (i * chartH / 6);
            gc.strokeLine(20, y, w - margin, y);
            double price = maxP - (i * range / 6);
            gc.setFill(Color.web("#7a8fa6"));
            gc.setFont(Font.font("Consolas", 9));
            gc.fillText(String.format("%.1f", price), w - margin + 5, y + 4);
        }

        // Bars
        for (int i = 0; i < prices.size(); i++) {
            double x = 20 + (i * chartW / prices.size());
            double barH = chartH * (prices.get(i) - minP) / range;
            double y = 20 + chartH - barH;

            boolean up = i == 0 || prices.get(i) >= prices.get(i - 1);
            gc.setFill(up ? Color.web(BUY_COLOR + "cc") : Color.web(SELL_COLOR + "cc"));
            gc.fillRect(x, y, barWidth, barH);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", 12));
        gc.fillText(stock.getSymbol() + " — Bar Chart", 25, 15);
    }

    private void renderAreaChart(Stock stock) {
        List<Double> prices = stock.getPriceHistory();
        double w = areaChartCanvas.getWidth();
        double h = areaChartCanvas.getHeight();
        GraphicsContext gc = areaChartCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#0f2040"));
        gc.fillRect(0, 0, w, h);

        if (prices.size() < 2) return;

        double minP = prices.stream().min(Double::compare).orElse(0.0) * 0.999;
        double maxP = prices.stream().max(Double::compare).orElse(1.0) * 1.001;
        double range = maxP - minP;
        if (range < 0.01) range = 1;

        double margin = 60;
        double chartW = w - margin - 20;
        double chartH = h - 50;
        double baseline = 20 + chartH;

        // Grid
        gc.setStroke(Color.web("#1a3a5c"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 6; i++) {
            double y = 20 + (i * chartH / 6);
            gc.strokeLine(20, y, w - margin, y);
            double price = maxP - (i * range / 6);
            gc.setFill(Color.web("#7a8fa6"));
            gc.setFont(Font.font("Consolas", 9));
            gc.fillText(String.format("%.1f", price), w - margin + 5, y + 4);
        }

        // Area fill
        double[] xPoints = new double[prices.size() + 2];
        double[] yPoints = new double[prices.size() + 2];

        for (int i = 0; i < prices.size(); i++) {
            xPoints[i] = 20 + ((double) i / (prices.size() - 1)) * chartW;
            yPoints[i] = 20 + chartH * (1 - (prices.get(i) - minP) / range);
        }
        xPoints[prices.size()] = 20 + chartW;
        yPoints[prices.size()] = baseline;
        xPoints[prices.size() + 1] = 20;
        yPoints[prices.size() + 1] = baseline;

        // Gradient-like effect using alpha
        gc.setFill(Color.web(ACCENT_COLOR + "33"));
        gc.fillPolygon(xPoints, yPoints, xPoints.length);

        // Line on top
        gc.setStroke(Color.web(ACCENT_COLOR));
        gc.setLineWidth(2);
        for (int i = 1; i < prices.size(); i++) {
            gc.strokeLine(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", 12));
        gc.fillText(stock.getSymbol() + " — Area Chart", 25, 15);
    }

    private void updateWatchlistDisplay() {
        watchlistBox.getChildren().clear();
        List<String> wl = watchlist.getAll();
        if (wl.isEmpty()) {
            Label empty = new Label("  No stocks in watchlist");
            empty.setFont(Font.font("Segoe UI", 11));
            empty.setTextFill(Color.web(TEXT_SECONDARY));
            watchlistBox.getChildren().add(empty);
            return;
        }

        for (String symbol : wl) {
            Stock stock = market.getStock(symbol);
            if (stock != null) {
                HBox row = createStockRow(stock);
                watchlistBox.getChildren().add(row);
            }
        }
    }

    private void updatePortfolioDisplay() {
        portfolioBox.getChildren().clear();

        Map<String, Integer> holdings = portfolio.getHoldings();
        if (holdings.isEmpty()) {
            Label empty = new Label("  No stocks owned");
            empty.setFont(Font.font("Segoe UI", 11));
            empty.setTextFill(Color.web(TEXT_SECONDARY));
            portfolioBox.getChildren().add(empty);
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String sym = entry.getKey();
                int qty = entry.getValue();
                double avg = portfolio.getAvgBuyPrice(sym);
                Stock stock = market.getStock(sym);
                if (stock == null) continue;

                double currentPrice = stock.getPrice();
                double pl = (currentPrice - avg) * qty;

                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(3, 5, 3, 5));

                Label symL = new Label(sym);
                symL.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
                symL.setTextFill(Color.web(TEXT_COLOR));
                symL.setPrefWidth(60);

                Label qtyL = new Label(String.valueOf(qty));
                qtyL.setFont(Font.font("Consolas", 11));
                qtyL.setTextFill(Color.web(TEXT_COLOR));
                qtyL.setPrefWidth(40);

                Label avgL = new Label(String.format("%.1f", avg));
                avgL.setFont(Font.font("Consolas", 11));
                avgL.setTextFill(Color.web(TEXT_SECONDARY));
                avgL.setPrefWidth(65);

                Label curL = new Label(String.format("%.1f", currentPrice));
                curL.setFont(Font.font("Consolas", 11));
                curL.setTextFill(Color.web(TEXT_COLOR));
                curL.setPrefWidth(65);

                Label plL = new Label(String.format("%+.1f", pl));
                plL.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
                plL.setTextFill(pl >= 0 ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
                plL.setPrefWidth(80);

                row.getChildren().addAll(symL, qtyL, avgL, curL, plL);

                // Hover effect
                row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #142a4a;"));
                row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

                portfolioBox.getChildren().add(row);
            }
        }

        // Update analytics
        double totalVal = portfolio.getTotalValue(market);
        double totalPL = portfolio.getTotalProfitLoss(market);
        double plPercent = portfolio.getPercentageGainLoss(market);

        totalValueLabel.setText(String.format("Total: %,.0f PKR", totalVal));
        plLabel.setText(String.format("P/L: %+,.0f", totalPL));
        plLabel.setTextFill(totalPL >= 0 ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
        plPercentLabel.setText(String.format("(%+.2f%%)", plPercent));
        plPercentLabel.setTextFill(totalPL >= 0 ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText(String.format("Balance: %,.0f PKR", portfolio.getBalance()));
    }

    private void updateProfileSection() {
        if (profileBox == null || profileBox.getChildren().size() < 4) return;

        Label bal = (Label) profileBox.getChildren().get(2);
        bal.setText(String.format("Balance: %,.0f PKR", portfolio.getBalance()));

        Label trades = (Label) profileBox.getChildren().get(3);
        trades.setText("Total Trades: " + tradeHistory.getTotalTrades());

        Label profit = (Label) profileBox.getChildren().get(4);
        double pl = portfolio.getTotalProfitLoss(market);
        profit.setText(String.format("Profit: %+,.0f PKR", pl));
        profit.setTextFill(pl >= 0 ? Color.web(BUY_COLOR) : Color.web(SELL_COLOR));
    }



    private void updateAlertsDisplay() {
        alertsBox.getChildren().clear();
        List<PriceAlert> active = alertManager.getActiveAlerts();
        if (active.isEmpty()) {
            Label empty = new Label("No active alerts");
            empty.setFont(Font.font("Segoe UI", 10));
            empty.setTextFill(Color.web(TEXT_SECONDARY));
            alertsBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < active.size(); i++) {
                PriceAlert alert = active.get(i);
                HBox row = new HBox(5);
                row.setAlignment(Pos.CENTER_LEFT);

                Label desc = new Label(alert.toString());
                desc.setFont(Font.font("Consolas", 10));
                desc.setTextFill(Color.web(TEXT_COLOR));
                HBox.setHgrow(desc, Priority.ALWAYS);

                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SELL_COLOR + "; -fx-cursor: hand; -fx-font-size: 10;");
                final int idx = i;
                removeBtn.setOnAction(e -> {
                    alertManager.removeAlert(idx);
                    updateAlertsDisplay();
                });

                row.getChildren().addAll(desc, removeBtn);
                alertsBox.getChildren().add(row);
            }
        }
    }

    private void checkAlerts() {
        List<String> triggered = alertManager.checkAlerts(market);
        for (String msg : triggered) {
            // Show alert notification
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Price Alert");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.show();
            updateAlertsDisplay();
        }
    }

    private void updateReviewsDisplay(Label avgLabel, VBox reviewsList) {
        double avg = ReviewManager.getAverageRating();
        int count = ReviewManager.getReviewCount();
        avgLabel.setText(String.format("Rating: %s %.1f (%d reviews)",
                ReviewManager.getStars((int) Math.round(avg)), avg, count));

        reviewsList.getChildren().clear();
        List<String[]> reviews = ReviewManager.getAllReviews();
        int start = Math.max(0, reviews.size() - 5);  // Show last 5
        for (int i = reviews.size() - 1; i >= start; i--) {
            String[] r = reviews.get(i);
            VBox reviewItem = new VBox(2);
            reviewItem.setPadding(new Insets(4));
            reviewItem.setStyle("-fx-background-color: " + BG_COLOR + ";" +
                    "-fx-background-radius: 4;");

            Label userStars = new Label(r[0] + " " + ReviewManager.getStars(Integer.parseInt(r[1])));
            userStars.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            userStars.setTextFill(Color.web(ACCENT_COLOR));

            Label comment = new Label(r[2]);
            comment.setFont(Font.font("Segoe UI", 10));
            comment.setTextFill(Color.web(TEXT_COLOR));
            comment.setWrapText(true);

            reviewItem.getChildren().addAll(userStars, comment);
            reviewsList.getChildren().add(reviewItem);
        }
    }

    private void updateAISuggestions() {
        Stock stock = market.getStock(selectedStock);
        if (stock == null) return;

        double change = stock.getChangePercent();
        List<Double> prices = stock.getPriceHistory();
        String suggestion;

        if (prices.size() < 10) {
            suggestion = "🤖 Gathering data...";
        } else {
            // Simple trend analysis
            double recentAvg = prices.subList(prices.size() - 5, prices.size()).stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            double olderAvg = prices.subList(prices.size() - 10, prices.size() - 5).stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            boolean trendUp = recentAvg > olderAvg;

            int held = portfolio.getQuantity(selectedStock);

            if (trendUp && change > 0.5) {
                suggestion = "🤖 📈 Strong uptrend! Consider buying.";
            } else if (trendUp && change > 0) {
                suggestion = "🤖 📊 Slight upward momentum.";
            } else if (!trendUp && change < -0.5) {
                suggestion = held > 0 ? "🤖 📉 Downtrend. Consider taking profits." :
                        "🤖 📉 Bearish. Wait for reversal.";
            } else if (!trendUp && change < 0) {
                suggestion = "🤖 ⚠️ Slight dip. Monitor closely.";
            } else {
                suggestion = "🤖 ➡️ Sideways movement. Hold position.";
            }
        }
        aiSuggestionLabel.setText(suggestion);
    }

    // ═══════════════════════════════════════════════
    // THEME TOGGLE
    // ═══════════════════════════════════════════════

    private void toggleTheme(Button themeBtn) {
        darkTheme = !darkTheme;
        if (darkTheme) {
            BG_COLOR = "#0a1628";
            PANEL_COLOR = "#0f2040";
            PANEL_BORDER = "#1a3a5c";
            TEXT_COLOR = "#eaecef";
            TEXT_SECONDARY = "#7a8fa6";
            INPUT_BG = "#0c1a30";
            themeBtn.setText("☀ Light");
        } else {
            BG_COLOR = "#f5f5f5";
            PANEL_COLOR = "#ffffff";
            PANEL_BORDER = "#e0e0e0";
            TEXT_COLOR = "#1a1a2e";
            TEXT_SECONDARY = "#666666";
            INPUT_BG = "#f0f0f0";
            themeBtn.setText("🌙 Dark");
        }

        // Rebuild the scene with new colors
        show();
    }

    // ═══════════════════════════════════════════════
    // EXPORT
    // ═══════════════════════════════════════════════

    private void exportPortfolio() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Portfolio");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        chooser.setInitialFileName(session.getCurrentUser() + "_portfolio");

        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                boolean csv = file.getName().endsWith(".csv");
                String sep = csv ? "," : "\t";

                pw.println("Pakistan Stock Market Simulator — Portfolio Export");
                pw.println("User: " + session.getCurrentUser());
                pw.println("Balance: " + String.format("%.2f", portfolio.getBalance()));
                pw.println("Total Value: " + String.format("%.2f", portfolio.getTotalValue(market)));
                pw.println("P/L: " + String.format("%.2f", portfolio.getTotalProfitLoss(market)));
                pw.println();

                // Holdings
                pw.println("Stock" + sep + "Qty" + sep + "Avg Price" + sep + "Current Price" + sep + "P/L");
                for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
                    String sym = entry.getKey();
                    int qty = entry.getValue();
                    double avg = portfolio.getAvgBuyPrice(sym);
                    Stock s = market.getStock(sym);
                    double cur = s != null ? s.getPrice() : 0;
                    double pl = (cur - avg) * qty;
                    pw.printf("%s%s%d%s%.2f%s%.2f%s%.2f%n", sym, sep, qty, sep, avg, sep, cur, sep, pl);
                }

                pw.println();
                pw.println("Trade History");
                pw.println("Time" + sep + "Action" + sep + "Stock" + sep + "Qty" + sep + "Price");
                for (TradeRecord tr : tradeHistory.getHistory()) {
                    pw.printf("%s%s%s%s%s%s%d%s%.2f%n",
                            tr.getTimestamp(), sep, tr.getAction(), sep, tr.getSymbol(), sep,
                            tr.getQuantity(), sep, tr.getPrice());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Complete");
                alert.setHeaderText(null);
                alert.setContentText("Portfolio exported to " + file.getName());
                alert.show();

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setContentText("Failed to export: " + ex.getMessage());
                alert.show();
            }
        }
    }

    // ═══════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════

    private VBox createPanelSection(String title) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + BG_COLOR + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web(TEXT_COLOR));

        box.getChildren().add(titleLabel);
        return box;
    }

    private HBox createSectionHeader(String left, String right) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(6, 8, 6, 8));
        header.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label l = new Label(left);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setTextFill(Color.web(TEXT_SECONDARY));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label r = new Label(right);
        r.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        r.setTextFill(Color.web(TEXT_SECONDARY));

        header.getChildren().addAll(l, spacer, r);
        return header;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", 11));
        btn.setPrefHeight(30);
        btn.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #142a4a;" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + ACCENT_COLOR + ";" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"));
        return btn;
    }

    private String getInputStyle() {
        return "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 5 8;";
    }

    private String getActiveChartBtnStyle() {
        return "-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 12;";
    }

    private String getInactiveChartBtnStyle() {
        return "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 12;";
    }
}
