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
     * Pulls every single stock (with its sector) from MySQL and adds it to the live market.
     */
    private void loadMarketFromDatabase() {
        String query = "SELECT s.symbol, s.company_name, s.current_price, sec.sector_name " +
                       "FROM Stocks s JOIN Sectors sec ON s.sector_id = sec.sector_id";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                String name = rs.getString("company_name");
                double price = rs.getDouble("current_price");
                String sector = rs.getString("sector_name");

                // Creates a new stock object using data from the database
                Stock stock = new Stock(symbol, name, sector, price);
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