# Pakistan Stock Market Simulator

A desktop trading simulator for the Pakistan Stock Exchange (PSX), built on a normalized MySQL backend. Users open an account, fund a wallet, and trade PSX-listed stocks against a live-ticking market with candlestick charts, price alerts and full trade history.

Built as a Database Management Systems project at NUST.

## Highlights

- **3NF relational schema.** 11 tables covering users, portfolios, holdings, transactions, trade history, watchlists, price alerts, chat, reviews and support tickets. They use composite keys, foreign-key cascades, `CHECK` constraints and covering indexes on the hot query paths. See [`database/schema.sql`](database/schema.sql).
- **Transactional trading.** Buys and sells update the wallet balance, holdings (with a weighted average buy price) and the transaction ledger together through JDBC prepared statements.
- **Live market.** Prices move every second using a Gaussian random walk and roll up into OHLC candles drawn on a custom JavaFX candlestick chart with a moving-average overlay.
- **Price alerts.** Above/below thresholds are evaluated on every tick and marked as triggered in the DB, so they fire only once.
- **Community features.** A global chat, app reviews with average rating, and support tickets.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| UI | JavaFX 21 |
| Database | MySQL 8 via JDBC (`mysql-connector-java`) |
| Build | Maven (`javafx-maven-plugin`) |

## Schema

```
Users ─┬─< Portfolios ─┬─< Holdings >── Stocks
       │               └─< Transactions
       ├─< Trade_History >───────────── Stocks
       ├─< Watchlists >──────────────── Stocks
       ├─< Price_Alerts >────────────── Stocks
       ├─< Chat_Messages
       ├─< Reviews
       └─< Support_Tickets
```

## Run it locally

Requirements: JDK 21+, Maven, and MySQL 8.

```bash
# 1. Create the database and seed PSX stocks
mysql -u root -p < database/schema.sql

# 2. Point the app at your database (defaults: localhost, root, empty password)
export PSMS_DB_USER=root
export PSMS_DB_PASSWORD=your_password
# export PSMS_DB_URL=jdbc:mysql://localhost:3306/psx_simulator

# 3. Launch
mvn javafx:run
```

## Project structure

```
src/main/java/com/psms/
├── Launcher.java / Main.java   # entry point
├── database/                   # JDBC connection helper
├── model/                      # Stock, Market, Portfolio, Session, Candle, TradeRecord, PriceAlert
├── manager/                    # Trade history, watchlist, alerts, chat, reviews, support
├── chart/                      # Custom JavaFX candlestick renderer
└── view/                       # Login, dashboard, wallet, history, chat and support windows
```
