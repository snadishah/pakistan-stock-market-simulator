package com.psms.model;

/**
 * Represents a price alert set by a user.
 * When the condition is met (price goes above or below threshold), a notification triggers.
 */
public class PriceAlert {

    public enum Condition { ABOVE, BELOW }

    private final String symbol;
    private final Condition condition;
    private final double threshold;
    private boolean triggered;

    public PriceAlert(String symbol, Condition condition, double threshold) {
        this.symbol = symbol;
        this.condition = condition;
        this.threshold = threshold;
        this.triggered = false;
    }

    /**
     * Checks if the alert condition is met.
     */
    public boolean check(double currentPrice) {
        if (triggered) return false;

        boolean met = false;
        if (condition == Condition.ABOVE && currentPrice > threshold) {
            met = true;
        } else if (condition == Condition.BELOW && currentPrice < threshold) {
            met = true;
        }

        if (met) {
            triggered = true;
        }
        return met;
    }

    // --- Getters ---
    public String getSymbol() { return symbol; }
    public Condition getCondition() { return condition; }
    public double getThreshold() { return threshold; }
    public boolean isTriggered() { return triggered; }

    /**
     * Serializes for file storage.
     */
    public String serialize() {
        return symbol + "|" + condition.name() + "|" + threshold + "|" + triggered;
    }

    /**
     * Deserializes from file storage.
     */
    public static PriceAlert deserialize(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 4) return null;
        try {
            PriceAlert alert = new PriceAlert(
                    parts[0],
                    Condition.valueOf(parts[1]),
                    Double.parseDouble(parts[2])
            );
            if (Boolean.parseBoolean(parts[3])) {
                alert.triggered = true;
            }
            return alert;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return symbol + " " + condition + " " + String.format("%.2f", threshold) +
                (triggered ? " (Triggered)" : " (Active)");
    }
}
