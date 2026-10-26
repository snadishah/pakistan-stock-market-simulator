package com.psms.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.psms.database.DatabaseHelper;

public class Market {
    private Map<String, Stock> stocks;

    public Market() {
        stocks = new HashMap<>();
        loadMarketFromDatabase();
    }

    /**
     * Pulls every single stock from the MySQL 'Stocks' table and adds it to the live market.
     */
    private void loadMarketFromDatabase() {
        String query = "SELECT symbol, company_name, current_price FROM Stocks";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                String name = rs.getString("company_name");
                double price = rs.getDouble("current_price");

                // Creates a new stock object using data from the database
                Stock stock = new Stock(symbol, name, price);
                stocks.put(symbol, stock);
            }
            System.out.println("✅ Market loaded successfully from Database! Total Stocks: " + stocks.size());

        } catch (SQLException e) {
            System.err.println("❌ Error loading market from DB: " + e.getMessage());
        }
    }

    public Set<String> getStockSymbols() {
        return stocks.keySet();
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    /**
     * Called every second by the Dashboard Timeline to fluctuate prices.
     */
    public void tick() {
        for (Stock stock : stocks.values()) {
            // This runs whatever price fluctuation math is inside your Stock class
            stock.tick(); 
        }
    }
}