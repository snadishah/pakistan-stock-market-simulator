package com.psms.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.psms.database.DatabaseHelper;
import com.psms.model.Market;
import com.psms.model.PriceAlert;
import com.psms.model.Stock;

public class AlertManager {
    private int userId;

    public AlertManager(String username) {
        String query = "SELECT user_id FROM Users WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) this.userId = rs.getInt("user_id");
            }
        } catch (SQLException e) { }
    }

    public void addAlert(String symbol, PriceAlert.Condition condition, double threshold) {
        String query = "INSERT INTO Price_Alerts (user_id, stock_symbol, alert_condition, threshold_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            pstmt.setString(2, symbol);
            pstmt.setString(3, condition.name());
            pstmt.setDouble(4, threshold);
            pstmt.executeUpdate();
        } catch (SQLException e) { }
    }

    public List<PriceAlert> getActiveAlerts() {
        List<PriceAlert> alerts = new ArrayList<>();
        String query = "SELECT stock_symbol, alert_condition, threshold_price FROM Price_Alerts WHERE user_id = ? AND is_triggered = FALSE ORDER BY alert_id ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(new PriceAlert(
                        rs.getString("stock_symbol"),
                        PriceAlert.Condition.valueOf(rs.getString("alert_condition")),
                        rs.getDouble("threshold_price")
                    ));
                }
            }
        } catch (SQLException e) { }
        return alerts;
    }

    public void removeAlert(int index) {
        List<Integer> ids = new ArrayList<>();
        String getIdsQuery = "SELECT alert_id FROM Price_Alerts WHERE user_id = ? AND is_triggered = FALSE ORDER BY alert_id ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getIdsQuery)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("alert_id"));
            }
            if (index >= 0 && index < ids.size()) {
                String deleteQuery = "DELETE FROM Price_Alerts WHERE alert_id = ?";
                try (PreparedStatement dp = conn.prepareStatement(deleteQuery)) {
                    dp.setInt(1, ids.get(index));
                    dp.executeUpdate();
                }
            }
        } catch (SQLException e) { }
    }

    public List<String> checkAlerts(Market market) {
        List<String> triggeredMsgs = new ArrayList<>();
        String query = "SELECT alert_id, stock_symbol, alert_condition, threshold_price FROM Price_Alerts WHERE user_id = ? AND is_triggered = FALSE";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("alert_id");
                    String sym = rs.getString("stock_symbol");
                    String cond = rs.getString("alert_condition");
                    double thresh = rs.getDouble("threshold_price");
                    Stock s = market.getStock(sym);
                    
                    if (s != null) {
                        boolean triggered = (cond.equals("ABOVE") && s.getPrice() >= thresh) || 
                                            (cond.equals("BELOW") && s.getPrice() <= thresh);
                        if (triggered) {
                            triggeredMsgs.add("🚨 " + sym + " crossed " + thresh + " PKR!");
                            String updateQuery = "UPDATE Price_Alerts SET is_triggered = TRUE WHERE alert_id = ?";
                            try (PreparedStatement upStmt = conn.prepareStatement(updateQuery)) {
                                upStmt.setInt(1, id);
                                upStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) { }
        return triggeredMsgs;
    }
}