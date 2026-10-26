package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.psms.database.DatabaseHelper;

public class ReviewManager {

    public static void submitReview(String username, int rating, String comment) {
        String getUserIdQuery = "SELECT user_id FROM Users WHERE username = ?";
        String insertQuery = "INSERT INTO Reviews (user_id, rating, comment) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement getUserStmt = conn.prepareStatement(getUserIdQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            
            getUserStmt.setString(1, username);
            try (ResultSet rs = getUserStmt.executeQuery()) {
                if (rs.next()) {
                    insertStmt.setInt(1, rs.getInt("user_id"));
                    insertStmt.setInt(2, rating);
                    insertStmt.setString(3, comment);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) { }
    }

    public static double getAverageRating() {
        String query = "SELECT AVG(rating) as avg_rating FROM Reviews";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getDouble("avg_rating");
        } catch (SQLException e) { }
        return 0.0;
    }

    public static int getReviewCount() {
        String query = "SELECT COUNT(*) as count FROM Reviews";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt("count");
        } catch (SQLException e) { }
        return 0;
    }

    public static List<String[]> getAllReviews() {
        List<String[]> reviews = new ArrayList<>();
        // JOIN query to get the actual username instead of just the user_id integer
        String query = "SELECT u.username, r.rating, r.comment FROM Reviews r JOIN Users u ON r.user_id = u.user_id ORDER BY r.review_timestamp ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                reviews.add(new String[]{
                    rs.getString("username"), 
                    String.valueOf(rs.getInt("rating")), 
                    rs.getString("comment")
                });
            }
        } catch (SQLException e) { }
        return reviews;
    }

    public static String getStars(int rating) {
        return "★".repeat(Math.max(0, rating)) + "☆".repeat(Math.max(0, 5 - rating));
    }
}