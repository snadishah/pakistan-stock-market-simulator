package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.psms.database.DatabaseHelper;

public class SupportManager {

    /**
     * Handles the logic for the Chat Bot tab in the Support Window.
     */
    public static String getBotResponse(String question) {
        question = question.toLowerCase();
        if (question.contains("buy")) return "To buy stocks, select a stock from the market list and use the 'Place Order' panel on the bottom left.";
        if (question.contains("sell")) return "To sell, select a stock you own and click the red 'SELL' button in the order panel.";
        if (question.contains("deposit") || question.contains("wallet")) return "Use the 'Wallet' tab at the top of the dashboard to deposit or withdraw virtual PKR.";
        if (question.contains("help")) return "I can answer questions about buying, selling, depositing, withdrawing, and navigating the simulator!";
        if (question.contains("alert")) return "You can set price alerts on the right panel. I'll notify you when the stock hits your target!";
        
        return "I'm a simple bot! Try asking me about 'buy', 'sell', 'deposit', or 'wallet'. For complex issues, please use the 'Submit Ticket' tab.";
    }

    /**
     * Takes the ticket from the UI, looks up the user's ID, and saves the ticket to the database.
     */
    public static void submitTicket(String username, String subject, String message) {
        String getUserIdQuery = "SELECT user_id FROM Users WHERE username = ?";
        String insertQuery = "INSERT INTO Support_Tickets (user_id, subject, message, status) VALUES (?, ?, ?, 'OPEN')";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement getUserStmt = conn.prepareStatement(getUserIdQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            
            // 1. Get the user's ID using their username
            getUserStmt.setString(1, username);
            try (ResultSet rs = getUserStmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    // 2. Insert the ticket into the database
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, subject);
                    insertStmt.setString(3, message);
                    insertStmt.executeUpdate();
                    System.out.println("✅ Support ticket saved to database!");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error submitting ticket to database: " + e.getMessage());
        }
    }
}