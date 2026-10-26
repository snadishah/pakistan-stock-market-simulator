package com.psms.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a single stock in the Pakistan Stock Exchange.
 * Maintains current price, price history, and candlestick data.
 */
public class Stock {

    private final String symbol;       // Stock ticker symbol
    private final String companyName;  // Full company name
    private final String sector;       // PSX sector, e.g. "Commercial Banks"
    private double price;              // Current market price
    private double previousClose;      // Previous closing price
    private double dayOpen;            // Opening price for current day
    private double dayHigh;            // Highest price of the day
    private double dayLow;             // Lowest today
    private double volume;             // Trading volume

    // Price history for charts
    private final List<Double> priceHistory;
    // Candlestick data for candlestick chart
    private final List<Candle> candles;

    // Current candle being built
    private double candleOpen;
    private double candleHigh;
    private double candleLow;
    private int tickCount;
    private static final int TICKS_PER_CANDLE = 5; // Generate candle every 5 ticks

    private final Random random = new Random();

    public Stock(String symbol, String companyName, double initialPrice) {
        this(symbol, companyName, "", initialPrice);
    }

    public Stock(String symbol, String companyName, String sector, double initialPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector == null ? "" : sector;
        this.price = initialPrice;
        this.previousClose = initialPrice;
        this.dayOpen = initialPrice;
        this.dayHigh = initialPrice;
        this.dayLow = initialPrice;
        this.volume = 0;
        this.priceHistory = new ArrayList<>();
        this.candles = new ArrayList<>();
        this.candleOpen = initialPrice;
        this.candleHigh = initialPrice;
        this.candleLow = initialPrice;
        this.tickCount = 0;

        // Seed some initial price history for charts
        double p = initialPrice;
        for (int i = 0; i < 200; i++) {
            double change = p * (random.nextGaussian() * 0.015);
            p = Math.max(1.0, p + change);
            priceHistory.add(p);

            // Generate initial candles
            if (i % TICKS_PER_CANDLE == 0 && i > 0) {
                double open = priceHistory.get(i - TICKS_PER_CANDLE);
                double close = p;
                double high = open, low = open;
                for (int j = i - TICKS_PER_CANDLE; j <= i && j < priceHistory.size(); j++) {
                    high = Math.max(high, priceHistory.get(j));
                    low = Math.min(low, priceHistory.get(j));
                }
                candles.add(new Candle(open, high, low, close));
            }
        }
        this.price = p;
    }

    /**
     * Simulates a price tick - called every second by the market.
     * Price changes follow a random walk with mean reversion.
     */
    public void tick() {
        // Random walk with slight mean reversion toward day open
        double volatility = 0.008 + random.nextDouble() * 0.012;  // 0.8% - 2% volatility
        double change = price * (random.nextGaussian() * volatility);

        // Slight mean reversion to prevent runaway prices
        double drift = (dayOpen - price) * 0.001;
        change += drift;

        price = Math.max(1.0, price + change);
        volume += random.nextInt(1000) + 100;

        // Update day stats
        dayHigh = Math.max(dayHigh, price);
        dayLow = Math.min(dayLow, price);

        // Add to price history
        priceHistory.add(price);
        if (priceHistory.size() > 200) {
            priceHistory.remove(0);
        }

        // Build candlestick data
        tickCount++;
        candleHigh = Math.max(candleHigh, price);
        candleLow = Math.min(candleLow, price);

        if (tickCount >= TICKS_PER_CANDLE) {
            candles.add(new Candle(candleOpen, candleHigh, candleLow, price));
            if (candles.size() > 120) {
                candles.remove(0);
            }
            // Reset for next candle
            candleOpen = price;
            candleHigh = price;
            candleLow = price;
            tickCount = 0;
        }
    }

    // --- Getters ---

    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public String getSector() { return sector; }
    public double getPrice() { return price; }
    public double getPreviousClose() { return previousClose; }
    public double getDayOpen() { return dayOpen; }
    public double getDayHigh() { return dayHigh; }
    public double getDayLow() { return dayLow; }
    public double getVolume() { return volume; }
    public List<Double> getPriceHistory() { return new ArrayList<>(priceHistory); }
    public List<Candle> getCandles() { return new ArrayList<>(candles); }

    /**
     * Returns percentage change from previous close.
     */
    public double getChangePercent() {
        if (previousClose == 0) return 0;
        return ((price - previousClose) / previousClose) * 100;
    }

    /**
     * Returns absolute change from previous close.
     */
    public double getChange() {
        return price - previousClose;
    }

    /**
     * Calculates Simple Moving Average for the given period.
     */
    public List<Double> getMovingAverage(int period) {
        List<Double> ma = new ArrayList<>();
        if (priceHistory.size() < period) return ma;

        for (int i = period - 1; i < priceHistory.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += priceHistory.get(j);
            }
            ma.add(sum / period);
        }
        return ma;
    }

    @Override
    public String toString() {
        return symbol + " - " + companyName;
    }
}
