package com.psms.chart;

import com.psms.model.Candle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Custom candlestick chart rendered on a JavaFX Canvas.
 * Features: OHLC candles, grid lines, price axis, dynamic scaling,
 * green (bullish) and red (bearish) candles, dark theme.
 */
public class CandlestickChart {

    // Colors matching Dark Navy + Electric Blue theme
    private static final Color BACKGROUND = Color.web("#0f2040");
    private static final Color GRID_COLOR = Color.web("#1a3a5c");
    private static final Color TEXT_COLOR = Color.web("#7a8fa6");
    private static final Color BULL_COLOR = Color.web("#0ecb81");
    private static final Color BEAR_COLOR = Color.web("#f6465d");
    private static final Color MA_COLOR = Color.web("#00BFFF");
    private static final Color AXIS_COLOR = Color.web("#1a3a5c");

    private final Canvas canvas;
    private final GraphicsContext gc;

    // Chart margins
    private static final double LEFT_MARGIN = 10;
    private static final double RIGHT_MARGIN = 70;
    private static final double TOP_MARGIN = 20;
    private static final double BOTTOM_MARGIN = 30;

    public CandlestickChart(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Renders the candlestick chart with the given data and optional moving average overlay.
     */
    public void render(List<Candle> candles, List<Double> movingAverage, String title) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Clear canvas with background
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, w, h);

        if (candles == null || candles.isEmpty()) {
            gc.setFill(TEXT_COLOR);
            gc.setFont(Font.font("Consolas", 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Waiting for candle data...", w / 2, h / 2);
            return;
        }

        // Calculate min/max for scaling
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        for (Candle c : candles) {
            minPrice = Math.min(minPrice, c.getLow());
            maxPrice = Math.max(maxPrice, c.getHigh());
        }

        // Add padding to price range
        double priceRange = maxPrice - minPrice;
        if (priceRange < 0.01) priceRange = 1.0;
        minPrice -= priceRange * 0.05;
        maxPrice += priceRange * 0.05;
        priceRange = maxPrice - minPrice;

        double chartWidth = w - LEFT_MARGIN - RIGHT_MARGIN;
        double chartHeight = h - TOP_MARGIN - BOTTOM_MARGIN;

        // Draw grid lines
        drawGrid(w, h, chartWidth, chartHeight, minPrice, maxPrice, priceRange);

        // Draw candles — thin bodies for a clean, professional look
        double spacing = chartWidth / candles.size();
        double candleWidth = Math.min(14, Math.max(2, spacing * 0.80));

        for (int i = 0; i < candles.size(); i++) {
            Candle c = candles.get(i);
            double x = LEFT_MARGIN + (i * spacing) + (spacing - candleWidth) / 2;

            // Scale prices to chart coordinates
            double openY = TOP_MARGIN + chartHeight * (1 - (c.getOpen() - minPrice) / priceRange);
            double closeY = TOP_MARGIN + chartHeight * (1 - (c.getClose() - minPrice) / priceRange);
            double highY = TOP_MARGIN + chartHeight * (1 - (c.getHigh() - minPrice) / priceRange);
            double lowY = TOP_MARGIN + chartHeight * (1 - (c.getLow() - minPrice) / priceRange);

            Color color = c.isBullish() ? BULL_COLOR : BEAR_COLOR;

            // Draw wick (thin high-low line)
            double wickX = x + candleWidth / 2;
            gc.setStroke(color);
            gc.setLineWidth(0.75);
            gc.strokeLine(wickX, highY, wickX, lowY);

            // Draw body (open-close rectangle)
            double bodyTop = Math.min(openY, closeY);
            double bodyHeight = Math.max(1, Math.abs(closeY - openY));

            gc.setFill(color);
            gc.fillRect(x, bodyTop, candleWidth, bodyHeight);
        }

        // Draw Moving Average overlay
        if (movingAverage != null && movingAverage.size() > 1) {
            gc.setStroke(MA_COLOR);
            gc.setLineWidth(1.5);

            int offset = candles.size() - movingAverage.size();
            for (int i = 1; i < movingAverage.size(); i++) {
                int ci = i + offset;
                int pi = ci - 1;
                if (pi < 0 || ci >= candles.size()) continue;

                double prevX = LEFT_MARGIN + (pi * spacing) + spacing / 2;
                double currX = LEFT_MARGIN + (ci * spacing) + spacing / 2;
                double prevY = TOP_MARGIN + chartHeight * (1 - (movingAverage.get(i - 1) - minPrice) / priceRange);
                double currY = TOP_MARGIN + chartHeight * (1 - (movingAverage.get(i) - minPrice) / priceRange);

                gc.strokeLine(prevX, prevY, currX, currY);
            }
        }

        // Draw title
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas", 12));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(title != null ? title : "Candlestick Chart", LEFT_MARGIN + 5, TOP_MARGIN - 5);

        // MA Legend
        if (movingAverage != null && !movingAverage.isEmpty()) {
            gc.setFill(MA_COLOR);
            gc.fillText("— MA(10)", LEFT_MARGIN + 200, TOP_MARGIN - 5);
        }
    }

    /**
     * Draws grid lines and price axis labels.
     */
    private void drawGrid(double w, double h, double chartWidth, double chartHeight,
                           double minPrice, double maxPrice, double priceRange) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);

        int gridLines = 8;
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Consolas", 10));
        gc.setTextAlign(TextAlignment.LEFT);

        for (int i = 0; i <= gridLines; i++) {
            double y = TOP_MARGIN + (i * chartHeight / gridLines);
            gc.strokeLine(LEFT_MARGIN, y, w - RIGHT_MARGIN, y);

            // Price label on right axis
            double price = maxPrice - (i * priceRange / gridLines);
            gc.fillText(String.format("%.2f", price), w - RIGHT_MARGIN + 5, y + 4);
        }

        // Right axis line
        gc.setStroke(AXIS_COLOR);
        gc.setLineWidth(1);
        gc.strokeLine(w - RIGHT_MARGIN, TOP_MARGIN, w - RIGHT_MARGIN, h - BOTTOM_MARGIN);

        // Bottom axis line
        gc.strokeLine(LEFT_MARGIN, h - BOTTOM_MARGIN, w - RIGHT_MARGIN, h - BOTTOM_MARGIN);
    }

    /**
     * Updates canvas size when parent resizes.
     */
    public void resize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
    }
}
