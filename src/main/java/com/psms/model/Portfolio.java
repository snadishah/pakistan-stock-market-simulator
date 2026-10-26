package com.psms.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.psms.database.DatabaseHelper;

public class Portfolio {
    private int userId;
    private int portfolioId;

    public Portfolio(int userId) {
        this.userId = userId;
        String query = "SELECT portfolio_id FROM Portfolios WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) this.portfolioId = rs.getInt("portfolio_id");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing portfolio: " + e.getMessage());
        }
    }

    public double getBalance() {
        String query = "SELECT current_balance FROM Portfolios WHERE portfolio_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.portfolioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("current_balance");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching balance: " + e.getMessage());
        }
        return 0.0;
    }

    // ==========================================
    // DEPOSIT & WITHDRAW (From Wallet)
    // ==========================================

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        String updateQuery = "UPDATE Portfolios SET current_balance = current_balance + ? WHERE portfolio_id = ?";
        String insertTxQuery = "INSERT INTO Transactions (portfolio_id, transaction_type, amount) VALUES (?, 'DEPOSIT', ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertTxQuery)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, this.portfolioId);
                updateStmt.executeUpdate();

                insertStmt.setInt(1, this.portfolioId);
                insertStmt.setDouble(2, amount);
                insertStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { }
        return false;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > getBalance()) return false;
        String updateQuery = "UPDATE Portfolios SET current_balance = current_balance - ? WHERE portfolio_id = ?";
        String insertTxQuery = "INSERT INTO Transactions (portfolio_id, transaction_type, amount) VALUES (?, 'WITHDRAW', ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertTxQuery)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, this.portfolioId);
                updateStmt.executeUpdate();

                insertStmt.setInt(1, this.portfolioId);
                insertStmt.setDouble(2, amount);
                insertStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { }
        return false;
    }

    public List<String[]> getTransactions() {
        List<String[]> transactions = new ArrayList<>();
        String query = "SELECT transaction_type, amount, transaction_timestamp FROM Transactions WHERE portfolio_id = ? ORDER BY transaction_timestamp ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.portfolioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("transaction_type");
                    String amount = String.format("%.2f", rs.getDouble("amount"));
                    String timestamp = rs.getString("transaction_timestamp");
                    transactions.add(new String[]{type, amount, timestamp});
                }
            }
        } catch (SQLException e) { }
        return transactions;
    }

    // ==========================================
    // MARKET TRADING LOGIC
    // ==========================================

    public boolean buy(String symbol, int qty, double price) {
        double totalCost = qty * price;
        if (getBalance() < totalCost) return false;

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Deduct balance
                String updateBal = "UPDATE Portfolios SET current_balance = current_balance - ? WHERE portfolio_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateBal)) {
                    pstmt.setDouble(1, totalCost);
                    pstmt.setInt(2, this.portfolioId);
                    pstmt.executeUpdate();
                }

                // 2. Update or Insert Holding
                String checkHolding = "SELECT quantity, average_buy_price FROM Holdings WHERE portfolio_id = ? AND stock_symbol = ?";
                boolean holdingExists = false;
                int oldQty = 0;
                double oldAvg = 0;
                
                try (PreparedStatement pstmt = conn.prepareStatement(checkHolding)) {
                    pstmt.setInt(1, this.portfolioId);
                    pstmt.setString(2, symbol);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            holdingExists = true;
                            oldQty = rs.getInt("quantity");
                            oldAvg = rs.getDouble("average_buy_price");
                        }
                    }
                }

                if (holdingExists) {
                    int newQty = oldQty + qty;
                    double newAvg = ((oldQty * oldAvg) + (qty * price)) / newQty;
                    String updateHolding = "UPDATE Holdings SET quantity = ?, average_buy_price = ? WHERE portfolio_id = ? AND stock_symbol = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateHolding)) {
                        pstmt.setInt(1, newQty);
                        pstmt.setDouble(2, newAvg);
                        pstmt.setInt(3, this.portfolioId);
                        pstmt.setString(4, symbol);
                        pstmt.executeUpdate();
                    }
                } else {
                    String insertHolding = "INSERT INTO Holdings (portfolio_id, stock_symbol, quantity, average_buy_price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertHolding)) {
                        pstmt.setInt(1, this.portfolioId);
                        pstmt.setString(2, symbol);
                        pstmt.setInt(3, qty);
                        pstmt.setDouble(4, price);
                        pstmt.executeUpdate();
                    }
                }

                // 3. Log the purchase
                String insertTxQuery = "INSERT INTO Transactions (portfolio_id, transaction_type, amount) VALUES (?, 'BUY', ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertTxQuery)) {
                    insertStmt.setInt(1, this.portfolioId);
                    insertStmt.setDouble(2, totalCost);
                    insertStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { }
        return false;
    }

    public boolean sell(String symbol, int qty, double price) {
        int currentQty = getQuantity(symbol);
        if (currentQty < qty) return false;

        double totalRevenue = qty * price;

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Add to balance
                String updateBal = "UPDATE Portfolios SET current_balance = current_balance + ? WHERE portfolio_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateBal)) {
                    pstmt.setDouble(1, totalRevenue);
                    pstmt.setInt(2, this.portfolioId);
                    pstmt.executeUpdate();
                }

                // 2. Update Holding (or delete if 0)
                int newQty = currentQty - qty;
                if (newQty == 0) {
                    String delHolding = "DELETE FROM Holdings WHERE portfolio_id = ? AND stock_symbol = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(delHolding)) {
                        pstmt.setInt(1, this.portfolioId);
                        pstmt.setString(2, symbol);
                        pstmt.executeUpdate();
                    }
                } else {
                    String updateHolding = "UPDATE Holdings SET quantity = ? WHERE portfolio_id = ? AND stock_symbol = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateHolding)) {
                        pstmt.setInt(1, newQty);
                        pstmt.setInt(2, this.portfolioId);
                        pstmt.setString(3, symbol);
                        pstmt.executeUpdate();
                    }
                }

                // 3. Log the sale
                String insertTxQuery = "INSERT INTO Transactions (portfolio_id, transaction_type, amount) VALUES (?, 'SELL', ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertTxQuery)) {
                    insertStmt.setInt(1, this.portfolioId);
                    insertStmt.setDouble(2, totalRevenue);
                    insertStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { }
        return false;
    }

    // ==========================================
    // DASHBOARD ANALYTICS
    // ==========================================

    public Map<String, Integer> getHoldings() {
        Map<String, Integer> map = new HashMap<>();
        String query = "SELECT stock_symbol, quantity FROM Holdings WHERE portfolio_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.portfolioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("stock_symbol"), rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) { }
        return map;
    }

    public int getQuantity(String symbol) {
        String query = "SELECT quantity FROM Holdings WHERE portfolio_id = ? AND stock_symbol = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.portfolioId);
            pstmt.setString(2, symbol);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("quantity");
            }
        } catch (SQLException e) { }
        return 0;
    }

    public double getAvgBuyPrice(String symbol) {
        String query = "SELECT average_buy_price FROM Holdings WHERE portfolio_id = ? AND stock_symbol = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, this.portfolioId);
            pstmt.setString(2, symbol);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("average_buy_price");
            }
        } catch (SQLException e) { }
        return 0.0;
    }

    public double getTotalValue(Market market) {
        double total = getBalance();
        Map<String, Integer> holdings = getHoldings();
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            Stock s = market.getStock(entry.getKey());
            if (s != null) {
                total += s.getPrice() * entry.getValue();
            }
        }
        return total;
    }

    public double getTotalProfitLoss(Market market) {
        return getTotalValue(market) - 1000000.0; // Assuming 1M initial balance
    }

    public double getPercentageGainLoss(Market market) {
        return (getTotalProfitLoss(market) / 1000000.0) * 100.0;
    }
}