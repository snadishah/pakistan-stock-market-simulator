package com.psms.model;

/**
 * Represents a single candlestick with OHLC (Open, High, Low, Close) data.
 * Used for rendering candlestick charts.
 */
public class Candle {

    private final double open;
    private final double high;
    private final double low;
    private final double close;

    public Candle(double open, double high, double low, double close) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }

    /**
     * Returns true if the candle is bullish (close >= open).
     */
    public boolean isBullish() {
        return close >= open;
    }

    /**
     * Returns the body height (absolute difference between open and close).
     */
    public double getBodyHeight() {
        return Math.abs(close - open);
    }

    /**
     * Returns the upper shadow height.
     */
    public double getUpperShadow() {
        return high - Math.max(open, close);
    }

    /**
     * Returns the lower shadow height.
     */
    public double getLowerShadow() {
        return Math.min(open, close) - low;
    }

    @Override
    public String toString() {
        return String.format("Candle[O=%.2f H=%.2f L=%.2f C=%.2f %s]",
                open, high, low, close, isBullish() ? "BULL" : "BEAR");
    }
}
