package com.psms.model; // Ensure this matches your actual package

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.psms.database.DatabaseHelper;

public class Session {
    private static Session instance;
    
    private String currentUser;
    private int currentUserId; // ADD THIS: Crucial for linking to the Portfolios table!
    private boolean loggedIn;

    // Keep your existing private constructor and getInstance() method here...

    public boolean login(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        // We select the user_id alongside the username to store it in the session
        String query = "SELECT user_id, username FROM Users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Set the ? placeholders
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, a matching user was found
                if (rs.next()) {
                    this.currentUserId = rs.getInt("user_id"); // Save the DB ID
                    this.currentUser = rs.getString("username");
                    this.loggedIn = true;
                    
                    System.out.println("Database Login successful for: " + this.currentUser);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database login error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
        this.currentUserId = -1;
        this.loggedIn = false;
    }

    public boolean register(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        String insertUserQuery = "INSERT INTO Users (username, password) VALUES (?, ?)";
        String insertPortfolioQuery = "INSERT INTO Portfolios (user_id) VALUES (?)";

        // 1. Connect to database and prepare statement (requesting generated keys to get the new user_id)
        try (Connection conn = DatabaseHelper.getConnection();
             java.sql.PreparedStatement userStmt = conn.prepareStatement(insertUserQuery, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            
            // 2. Execute the user insert
            int rowsAffected = userStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // 3. Grab the auto-generated user_id from MySQL
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newUserId = generatedKeys.getInt(1);
                        
                        // 4. Create their starting Portfolio using that new ID
                        try (java.sql.PreparedStatement portStmt = conn.prepareStatement(insertPortfolioQuery)) {
                            portStmt.setInt(1, newUserId);
                            portStmt.executeUpdate();
                        }
                        
                        // 5. Automatically log them in after successful registration
                        this.currentUserId = newUserId;
                        this.currentUser = username;
                        this.loggedIn = true;
                        
                        System.out.println("Database Registration successful for: " + username);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // If we hit an error here, it usually means the username already exists (UNIQUE constraint)
            System.err.println("Registration failed (Username might already exist): " + e.getMessage());
        }
        return false;
    }

    // --- GETTERS ---
    public String getCurrentUser() { return currentUser; }
    public int getCurrentUserId() { return currentUserId; } // Make sure to add this getter!
    public boolean isLoggedIn() { return loggedIn; }
}