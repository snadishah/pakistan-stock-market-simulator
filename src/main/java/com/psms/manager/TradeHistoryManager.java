package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.psms.database.DatabaseHelper;
import com.psms.model.TradeRecord;

public class TradeHistoryManager {
    private int userId;

    public TradeHistoryManager(String username) {
        String query = "SELECT user_id FROM Users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) this.userId = rs.getInt("user_id");
            }
        } catch (SQLException e) { }
    }

    public void recordTrade(String symbol, TradeRecord.Action action, int quantity, double price) {
        String query = "INSERT INTO Trade_History (user_id, stock_symbol, action, quantity, execution_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            pstmt.setString(2, symbol);
            pstmt.setString(3, action.name());
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.executeUpdate();
        } catch (SQLException e) { }
    }

    public int getTotalTrades() {
        String query = "SELECT COUNT(*) AS count FROM Trade_History WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException e) { }
        return 0;
    }

    public List<TradeRecord> getHistory() {
        List<TradeRecord> list = new ArrayList<>();
        String query = "SELECT stock_symbol, action, quantity, execution_price FROM Trade_History WHERE user_id = ? ORDER BY trade_timestamp DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TradeRecord(
                        rs.getString("stock_symbol"),
                        TradeRecord.Action.valueOf(rs.getString("action")),
                        rs.getInt("quantity"),
                        rs.getDouble("execution_price")
                    ));
                }
            }
        } catch (SQLException e) { }
        return list;
    }
}