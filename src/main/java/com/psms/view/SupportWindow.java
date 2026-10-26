package com.psms.view;

import com.psms.manager.SupportManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SupportWindow {

    private static final String BG_COLOR = "#0a1628";
    private static final String PANEL_COLOR = "#0f2040";
    private static final String ACCENT_COLOR = "#00BFFF";
    private static final String TEXT_COLOR = "#eaecef";
    private static final String TEXT_SECONDARY = "#7a8fa6";
    private static final String INPUT_BG = "#0c1a30";
    private static final String PANEL_BORDER = "#1a3a5c";
    private static final String BUY_COLOR = "#0ecb81";

    private final Stage stage;
    private final String username;
    private final Runnable onBack;

    public SupportWindow(Stage stage, String username, Runnable onBack) {
        this.stage = stage;
        this.username = username;
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
        Label title = new Label("🎧 Customer Support");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ACCENT_COLOR));

        headerRow.getChildren().addAll(backBtn, title);

        Label subtitle = new Label("Get help with trading, account issues, and more");
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web(TEXT_SECONDARY));

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab chatBotTab = new Tab("🤖 Chat Bot");
        chatBotTab.setContent(createChatBotPane());

        Tab faqTab = new Tab("❓ FAQ");
        faqTab.setContent(createFAQPane());

        Tab ticketTab = new Tab("📝 Submit Ticket");
        ticketTab.setContent(createTicketPane());

        tabPane.getTabs().addAll(chatBotTab, faqTab, ticketTab);

        root.getChildren().addAll(headerRow, subtitle, tabPane);

        SceneSwitcher.switchScene(stage, root, "🎧 Customer Support — PSX Simulator");
    }

    private VBox createChatBotPane() {
        VBox pane = new VBox(8);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label botLabel = new Label("🤖 PSX Support Bot");
        botLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        botLabel.setTextFill(Color.web(ACCENT_COLOR));

        Label botSubtitle = new Label("Ask me anything about the simulator! Type 'help' to see topics.");
        botSubtitle.setFont(Font.font("Segoe UI", 11));
        botSubtitle.setTextFill(Color.web(TEXT_SECONDARY));
        botSubtitle.setWrapText(true);

        TextArea botChatArea = new TextArea();
        botChatArea.setEditable(false);
        botChatArea.setWrapText(true);
        botChatArea.setFont(Font.font("Segoe UI", 12));
        botChatArea.setStyle("-fx-control-inner-background: " + PANEL_COLOR + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-color: " + PANEL_COLOR + ";" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;");
        VBox.setVgrow(botChatArea, Priority.ALWAYS);

        botChatArea.setText("🤖 Bot: Hello " + username + "! Welcome to PSX Simulator Support.\n" +
                "Type 'help' to see what I can assist with, or ask me a question!\n\n");

        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField botInput = new TextField();
        botInput.setPromptText("Ask a question...");
        botInput.setPrefHeight(40);
        botInput.setFont(Font.font("Segoe UI", 13));
        botInput.setStyle("-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 5 10;");
        HBox.setHgrow(botInput, Priority.ALWAYS);

        Button sendBtn = new Button("Ask");
        sendBtn.setPrefHeight(40);
        sendBtn.setPrefWidth(80);
        sendBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        sendBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;");
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle("-fx-background-color: #00A3D9;" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));

        sendBtn.setOnAction(e -> {
            String question = botInput.getText().trim();
            if (!question.isEmpty()) {
                botChatArea.appendText("👤 You: " + question + "\n\n");
                String response = SupportManager.getBotResponse(question);
                botChatArea.appendText("🤖 Bot: " + response + "\n\n");
                botInput.clear();
                botChatArea.setScrollTop(Double.MAX_VALUE);
            }
        });
        botInput.setOnAction(e -> sendBtn.fire());

        inputRow.getChildren().addAll(botInput, sendBtn);

        pane.getChildren().addAll(botLabel, botSubtitle, botChatArea, inputRow);
        return pane;
    }

    private ScrollPane createFAQPane() {
        VBox pane = new VBox(8);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label faqTitle = new Label("❓ Frequently Asked Questions");
        faqTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        faqTitle.setTextFill(Color.web(ACCENT_COLOR));

        String[][] faqs = {
                {"How do I buy stocks?",
                        "Select a stock from the Market list on the left side of the dashboard. Then enter the quantity you want to buy in the 'Place Order' panel and click the green 'BUY' button. Make sure you have sufficient balance."},
                {"How do I sell stocks?",
                        "Select the stock you want to sell, enter the quantity (must be equal to or less than your holdings), and click the red 'SELL' button. The proceeds will be added to your balance."},
                {"How do I deposit money?",
                        "Click the '💰 Wallet' button in the top toolbar. Select the 'Deposit' tab, enter the amount you want to deposit, and click 'Deposit Funds'. Your balance will increase immediately."},
                {"How do I withdraw money?",
                        "Click the '💰 Wallet' button, select the 'Withdraw' tab, enter the amount (cannot exceed your balance), and click 'Withdraw Funds'."},
                {"How do stock predictions work?",
                        "Click the '🔮 Predictions' button to see AI-powered predictions. The system analyzes price trends, momentum, moving averages, and news sentiment to predict whether a stock will go up or down. Note: these are simulated predictions for educational purposes."},
                {"How do I set price alerts?",
                        "In the right panel of the dashboard, find 'Price Alerts'. Select 'Above' or 'Below', enter a target price, and click '+'. You'll get a notification when the stock price crosses your target."},
                {"How do I use the chat?",
                        "Click the '💬 Chat' button in the toolbar to open the Trader Chat. You can send messages to other traders in real-time."},
                {"How is Profit/Loss calculated?",
                        "P/L = (Current Price - Average Buy Price) × Quantity for each stock. Your total P/L includes all holdings plus your cash balance minus your initial investment."},
                {"How do I export my data?",
                        "Click '📥 Export' in the top toolbar. You can save your portfolio and trade history as a CSV or TXT file."},
                {"Is this real money?",
                        "No! This is a simulator. All trading is done with virtual PKR. You start with 1,000,000 PKR. No real money is involved."}
        };

        pane.getChildren().add(faqTitle);

        for (String[] faq : faqs) {
            TitledPane tp = new TitledPane();
            tp.setText(faq[0]);
            tp.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

            Label answer = new Label(faq[1]);
            answer.setFont(Font.font("Segoe UI", 12));
            answer.setTextFill(Color.web(TEXT_COLOR));
            answer.setWrapText(true);
            answer.setPadding(new Insets(8));

            VBox contentBox = new VBox(answer);
            contentBox.setStyle("-fx-background-color: " + PANEL_COLOR + ";");
            tp.setContent(contentBox);
            tp.setExpanded(false);
            tp.setStyle("-fx-background-color: " + PANEL_COLOR + ";" +
                    "-fx-text-fill: " + TEXT_COLOR + ";");

            pane.getChildren().add(tp);
        }

        ScrollPane scroll = new ScrollPane(pane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG_COLOR + "; -fx-background-color: " + BG_COLOR + ";");
        return scroll;
    }

    private VBox createTicketPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Label ticketTitle = new Label("📝 Submit a Support Ticket");
        ticketTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        ticketTitle.setTextFill(Color.web(ACCENT_COLOR));

        Label ticketSubtitle = new Label("Can't find what you need? Submit a ticket and our team will help.");
        ticketSubtitle.setFont(Font.font("Segoe UI", 11));
        ticketSubtitle.setTextFill(Color.web(TEXT_SECONDARY));
        ticketSubtitle.setWrapText(true);

        Label subjectLabel = new Label("Subject");
        subjectLabel.setFont(Font.font("Segoe UI", 12));
        subjectLabel.setTextFill(Color.web(TEXT_SECONDARY));

        TextField subjectField = new TextField();
        subjectField.setPromptText("Brief description of your issue");
        subjectField.setPrefHeight(38);
        subjectField.setFont(Font.font("Segoe UI", 13));
        subjectField.setStyle(getInputStyle());

        Label messageLabel = new Label("Message");
        messageLabel.setFont(Font.font("Segoe UI", 12));
        messageLabel.setTextFill(Color.web(TEXT_SECONDARY));

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Describe your issue in detail...");
        messageArea.setWrapText(true);
        messageArea.setPrefHeight(150);
        messageArea.setFont(Font.font("Segoe UI", 13));
        messageArea.setStyle("-fx-control-inner-background: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;");

        Label status = new Label();
        status.setFont(Font.font("Segoe UI", 13));
        status.setWrapText(true);

        Button submitBtn = new Button("Submit Ticket");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setPrefHeight(42);
        submitBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        submitBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: #00A3D9;" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + ";" +
                "-fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-cursor: hand;"));

        submitBtn.setOnAction(e -> {
            String subject = subjectField.getText().trim();
            String message = messageArea.getText().trim();

            if (subject.isEmpty() || message.isEmpty()) {
                status.setText("❌ Please fill in both subject and message.");
                status.setTextFill(Color.web("#f6465d"));
                return;
            }

            SupportManager.submitTicket(username, subject, message);
            status.setText("✅ Ticket submitted successfully! Our team will review it shortly.");
            status.setTextFill(Color.web(BUY_COLOR));
            subjectField.clear();
            messageArea.clear();
        });

        pane.getChildren().addAll(ticketTitle, ticketSubtitle,
                subjectLabel, subjectField,
                messageLabel, messageArea,
                submitBtn, status);
        return pane;
    }

    private String getInputStyle() {
        return "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                "-fx-prompt-text-fill: #5e6673;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + PANEL_BORDER + ";" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 5 10;";
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
