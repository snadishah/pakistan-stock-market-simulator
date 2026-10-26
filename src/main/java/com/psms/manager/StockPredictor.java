/* 
package com.psms.manager;

import com.psms.model.Stock;
import com.psms.model.Market;

import java.util.*;


 // Prediction engine for stocks.
 
public class StockPredictor {

    public enum Direction { UP, DOWN, NEUTRAL }

    public static class Prediction {
        private final Direction direction;
        private final double confidence;
        private final String reasoning;
        private final String newsSentiment;

        public Prediction(Direction direction, double confidence, String reasoning, String newsSentiment) {
            this.direction = direction;
            this.confidence = confidence;
            this.reasoning = reasoning;
            this.newsSentiment = newsSentiment;
        }

        public Direction getDirection() { return direction; }
        public double getConfidence() { return confidence; }
        public String getReasoning() { return reasoning; }
        public String getNewsSentiment() { return newsSentiment; }

        public String getDirectionEmoji() {
            return switch (direction) {
                case UP -> "📈";
                case DOWN -> "📉";
                case NEUTRAL -> "➡️";
            };
        }

        public String getDirectionText() {
            return switch (direction) {
                case UP -> "BULLISH";
                case DOWN -> "BEARISH";
                case NEUTRAL -> "NEUTRAL";
            };
        }
    }

    private static final Map<String, String[]> STOCK_NEWS = new LinkedHashMap<>();
    private static final Random random = new Random();

    static {
        STOCK_NEWS.put("ENGRO", new String[]{
                "Engro announces quarterly dividend of PKR 15/share",
                "Engro Corp expands into renewable energy sector",
                "Strong earnings beat expectations for Engro"
        });
        STOCK_NEWS.put("OGDC", new String[]{
                "Oil prices dip; OGDC under slight pressure",
                "OGDC discovers new gas reserves in Sindh",
                "Global crude outlook uncertain for OGDC"
        });
        STOCK_NEWS.put("HBL", new String[]{
                "HBL reports 18% growth in net profit",
                "HBL expands digital banking services",
                "Banking sector faces regulatory tightening"
        });
        STOCK_NEWS.put("LUCK", new String[]{
                "LUCK Cement expansion project on track",
                "Infrastructure spending boosts cement demand",
                "Rising input costs pressure cement margins"
        });
        STOCK_NEWS.put("PPL", new String[]{
                "PPL production declines in Q3 report",
                "Gas pricing revision benefits PPL outlook",
                "Exploration activities yield mixed results"
        });
        STOCK_NEWS.put("PSO", new String[]{
                "PSO benefits from fuel price adjustments",
                "Government delays circular debt payment to PSO",
                "PSO market share remains dominant"
        });
        STOCK_NEWS.put("FFC", new String[]{
                "Fertilizer demand surges during Rabi season",
                "FFC maintains strong dividend yield",
                "Urea price cap concerns weigh on FFC"
        });
        STOCK_NEWS.put("HUBC", new String[]{
                "Hub Power capacity payments remain stable",
                "HUBC announces coal plant efficiency upgrades",
                "Energy sector policy changes create uncertainty"
        });
        STOCK_NEWS.put("UBL", new String[]{
                "UBL international operations show growth",
                "Banking sector liquidity remains healthy",
                "UBL provisions rise on NPL concerns"
        });
        STOCK_NEWS.put("MCB", new String[]{
                "MCB Bank reports steady loan growth",
                "MCB digital wallet launch gains traction",
                "Interest rate changes impact MCB spreads"
        });
        STOCK_NEWS.put("EFERT", new String[]{
                "Engro Fertilizers output hits record levels",
                "Subsidy concerns cloud fertilizer sector",
                "EFERT benefits from improved gas supply"
        });
        STOCK_NEWS.put("BAHL", new String[]{
                "Bank Al Habib deposits grow 12% YoY",
                "BAHL expands branch network in Punjab",
                "Competition pressures BAHL margins"
        });
        STOCK_NEWS.put("MEBL", new String[]{
                "Meezan Bank leads Islamic banking growth",
                "MEBL assets cross PKR 2 trillion milestone",
                "Rising rates benefit Meezan Bank spreads"
        });
        STOCK_NEWS.put("SYS", new String[]{
                "Systems Limited hits 52-week high on IT export boom",
                "SYS wins major government digitization contract",
                "IT sector valuation concerns emerge"
        });
        STOCK_NEWS.put("TRG", new String[]{
                "TRG Pakistan to acquire AI startup",
                "TRG subsidiary Ibex revenue grows 15%",
                "Market skepticism over TRG valuation"
        });
    }

    public static Prediction predict(Stock stock) {
        List<Double> prices = stock.getPriceHistory();

        if (prices.size() < 15) {
            return new Prediction(Direction.NEUTRAL, 0.3, "Insufficient data for analysis", "Pending");
        }

        double score = 0;
        StringBuilder reasons = new StringBuilder();

        // 1. Short-term trend (last 5 vs previous 5)
        double recentAvg = prices.subList(prices.size() - 5, prices.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
        double olderAvg = prices.subList(prices.size() - 10, prices.size() - 5).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);

        double trendStrength = (recentAvg - olderAvg) / olderAvg * 100;
        if (trendStrength > 0.3) {
            score += 2;
            reasons.append("Upward trend (+").append(String.format("%.1f%%", trendStrength)).append("). ");
        } else if (trendStrength < -0.3) {
            score -= 2;
            reasons.append("Downward trend (").append(String.format("%.1f%%", trendStrength)).append("). ");
        } else {
            reasons.append("Flat trend. ");
        }

        // 2. Momentum (last 3 prices)
        double p1 = prices.get(prices.size() - 3);
        double p2 = prices.get(prices.size() - 2);
        double p3 = prices.get(prices.size() - 1);
        boolean accelerating = (p3 - p2) > (p2 - p1);

        if (p3 > p2 && p2 > p1) {
            score += 1.5;
            reasons.append("Positive momentum. ");
            if (accelerating) {
                score += 0.5;
                reasons.append("Accelerating gains. ");
            }
        } else if (p3 < p2 && p2 < p1) {
            score -= 1.5;
            reasons.append("Negative momentum. ");
            if (!accelerating) {
                score -= 0.5;
                reasons.append("Accelerating losses. ");
            }
        }

        // 3. Price vs Moving Average (10-period)
        List<Double> ma = stock.getMovingAverage(10);
        if (!ma.isEmpty()) {
            double currentMA = ma.get(ma.size() - 1);
            double currentPrice = stock.getPrice();
            if (currentPrice > currentMA * 1.005) {
                score += 1;
                reasons.append("Above MA(10). ");
            } else if (currentPrice < currentMA * 0.995) {
                score -= 1;
                reasons.append("Below MA(10). ");
            }
        }

        // 4. Volatility check
        double volatility = calculateVolatility(prices.subList(prices.size() - 10, prices.size()));
        if (volatility > 2.0) {
            reasons.append("High volatility (caution). ");
            score *= 0.8;
        }

        // 5. News sentiment (simulated)
        String sentiment = getNewsSentiment(stock.getSymbol());
        switch (sentiment) {
            case "Positive" -> { score += 1.5; reasons.append("Positive news sentiment. "); }
            case "Negative" -> { score -= 1.5; reasons.append("Negative news sentiment. "); }
            default -> reasons.append("Mixed news. ");
        }

        // Determine direction and confidence
        Direction direction;
        double confidence;

        if (score > 1.5) {
            direction = Direction.UP;
            confidence = Math.min(0.95, 0.5 + score * 0.07);
        } else if (score < -1.5) {
            direction = Direction.DOWN;
            confidence = Math.min(0.95, 0.5 + Math.abs(score) * 0.07);
        } else {
            direction = Direction.NEUTRAL;
            confidence = 0.4 + Math.abs(score) * 0.05;
        }

        return new Prediction(direction, confidence, reasons.toString().trim(), sentiment);
    }

    public static String getLatestNews(String symbol) {
        String[] news = STOCK_NEWS.get(symbol);
        if (news == null || news.length == 0) return "No news available";
        return news[random.nextInt(news.length)];
    }

    private static String getNewsSentiment(String symbol) {
        String[] news = STOCK_NEWS.get(symbol);
        if (news == null) return "Neutral";

        // Rotate sentiment based on time to create dynamic changes
        long timeFactor = System.currentTimeMillis() / 10000;
        int index = (int) ((timeFactor + symbol.hashCode()) % 3);
        // First headline is typically positive, second mixed, third negative
        return switch (index) {
            case 0 -> "Positive";
            case 1 -> "Neutral";
            case 2 -> "Negative";
            default -> "Neutral";
        };
    }

    private static double calculateVolatility(List<Double> prices) {
        if (prices.size() < 2) return 0;
        double mean = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = prices.stream()
                .mapToDouble(p -> Math.pow((p - mean) / mean * 100, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }
}
*/