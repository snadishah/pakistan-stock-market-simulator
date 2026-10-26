package com.psms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single trade record (buy or sell transaction).
 */
public class TradeRecord {

    public enum Action { BUY, SELL }

    private final String symbol;
    private final Action action;
    private final int quantity;
    private final double price;
    private final String timestamp;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TradeRecord(String symbol, Action action, int quantity, double price) {
        this.symbol = symbol;
        this.action = action;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    public TradeRecord(String symbol, Action action, int quantity, double price, String timestamp) {
        this.symbol = symbol;
        this.action = action;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    // --- Getters ---
    public String getSymbol() { return symbol; }
    public Action getAction() { return action; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getTimestamp() { return timestamp; }
    public double getTotal() { return price * quantity; }

    /**
     * Serializes the record to a string for file storage.
     */
    public String serialize() {
        return symbol + "|" + action.name() + "|" + quantity + "|" + price + "|" + timestamp;
    }

    /**
     * Deserializes a record from a string.
     */
    public static TradeRecord deserialize(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5) return null;
        try {
            return new TradeRecord(
                    parts[0],
                    Action.valueOf(parts[1]),
                    Integer.parseInt(parts[2]),
                    Double.parseDouble(parts[3]),
                    parts[4]
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s x%d @ %.2f PKR", timestamp, action, symbol, quantity, price);
    }
}
