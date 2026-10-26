package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.psms.database.DatabaseHelper;

public class WatchlistManager {
    private int userId;

    public WatchlistManager(String username) {
        String query = "SELECT user_id FROM Users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) this.userId = rs.getInt("user_id");
            }
        } catch (SQLException e) { }
    }

    public List<String> getAll() {
        List<String> list = new ArrayList<>();
        String query = "SELECT stock_symbol FROM Watchlists WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(rs.getString("stock_symbol"));
            }
        } catch (SQLException e) { }
        return list;
    }

    public boolean contains(String symbol) {
        String query = "SELECT 1 FROM Watchlists WHERE user_id = ? AND stock_symbol = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            pstmt.setString(2, symbol);
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { }
        return false;
    }

    public void toggle(String symbol) {
        if (contains(symbol)) {
            String query = "DELETE FROM Watchlists WHERE user_id = ? AND stock_symbol = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, this.userId);
                pstmt.setString(2, symbol);
                pstmt.executeUpdate();
            } catch (SQLException e) { }
        } else {
            String query = "INSERT INTO Watchlists (user_id, stock_symbol) VALUES (?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, this.userId);
                pstmt.setString(2, symbol);
                pstmt.executeUpdate();
            } catch (SQLException e) { }
        }
    }
}