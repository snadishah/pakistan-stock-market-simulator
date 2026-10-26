package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.psms.database.DatabaseHelper;

public class ChatManager {

    /**
     * Inserts a new chat message into the database.
     */
    public static void sendMessage(String username, String messageText) {
        String getUserIdQuery = "SELECT user_id FROM Users WHERE username = ?";
        String insertQuery = "INSERT INTO Chat_Messages (user_id, message_text) VALUES (?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement getUserStmt = conn.prepareStatement(getUserIdQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            
            // 1. Find the user's ID
            getUserStmt.setString(1, username);
            try (ResultSet rs = getUserStmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    // 2. Insert the message
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, messageText);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Fetches all chat messages, JOINING with the Users table to get the sender's username.
     * Returns a List of String arrays: [username, message_text, timestamp]
     */
    public static List<String[]> getMessages() {
        List<String[]> messages = new ArrayList<>();
        // INNER JOIN to match the user_id to their actual username
        String query = "SELECT u.username, c.message_text, c.message_time " +
                       "FROM Chat_Messages c " +
                       "JOIN Users u ON c.user_id = u.user_id " +
                       "ORDER BY c.message_time ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                messages.add(new String[]{
                    rs.getString("username"),
                    rs.getString("message_text"),
                    rs.getString("message_time")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
        }
        return messages;
    }
}