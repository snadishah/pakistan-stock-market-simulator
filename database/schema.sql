-- Pakistan Stock Market Simulator — MySQL 8 schema (3NF)
-- Run:  mysql -u root -p < database/schema.sql

DROP DATABASE IF EXISTS psx_simulator;
CREATE DATABASE psx_simulator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE psx_simulator;

-- ── Accounts ────────────────────────────────────────────────
CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Portfolios (
    portfolio_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL UNIQUE,
    current_balance  DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (current_balance >= 0),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ── Market ──────────────────────────────────────────────────
CREATE TABLE Stocks (
    symbol         VARCHAR(10)   PRIMARY KEY,
    company_name   VARCHAR(100)  NOT NULL,
    current_price  DECIMAL(12,2) NOT NULL CHECK (current_price > 0)
);

-- ── Trading ─────────────────────────────────────────────────
CREATE TABLE Holdings (
    portfolio_id       INT           NOT NULL,
    stock_symbol       VARCHAR(10)   NOT NULL,
    quantity           INT           NOT NULL CHECK (quantity > 0),
    average_buy_price  DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (portfolio_id, stock_symbol),
    FOREIGN KEY (portfolio_id) REFERENCES Portfolios(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES Stocks(symbol)
);

CREATE TABLE Transactions (
    transaction_id         INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id           INT NOT NULL,
    transaction_type       ENUM('DEPOSIT','WITHDRAW','BUY','SELL') NOT NULL,
    amount                 DECIMAL(15,2) NOT NULL,
    transaction_timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolios(portfolio_id) ON DELETE CASCADE,
    INDEX idx_tx_portfolio_time (portfolio_id, transaction_timestamp)
);

CREATE TABLE Trade_History (
    trade_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    stock_symbol     VARCHAR(10) NOT NULL,
    action           ENUM('BUY','SELL') NOT NULL,
    quantity         INT NOT NULL CHECK (quantity > 0),
    execution_price  DECIMAL(12,2) NOT NULL,
    trade_timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES Stocks(symbol),
    INDEX idx_trade_user_time (user_id, trade_timestamp)
);

-- ── Engagement ──────────────────────────────────────────────
CREATE TABLE Watchlists (
    user_id       INT NOT NULL,
    stock_symbol  VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, stock_symbol),
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES Stocks(symbol)
);

CREATE TABLE Price_Alerts (
    alert_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    stock_symbol     VARCHAR(10) NOT NULL,
    alert_condition  ENUM('ABOVE','BELOW') NOT NULL,
    threshold_price  DECIMAL(12,2) NOT NULL,
    is_triggered     BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES Stocks(symbol),
    INDEX idx_alert_active (user_id, is_triggered)
);

CREATE TABLE Chat_Messages (
    message_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    message_text  TEXT NOT NULL,
    message_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE Reviews (
    review_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id           INT NOT NULL,
    rating            TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment           TEXT,
    review_timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE Support_Tickets (
    ticket_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    subject     VARCHAR(150) NOT NULL,
    message     TEXT NOT NULL,
    status      ENUM('OPEN','IN_PROGRESS','CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ── Seed: PSX-listed companies (sample starting prices, PKR) ─
INSERT INTO Stocks (symbol, company_name, current_price) VALUES
    ('TRG',   'TRG Pakistan Ltd',                    55.00),
    ('SYS',   'Systems Ltd',                        420.00),
    ('OGDC',  'Oil & Gas Development Company',      210.00),
    ('PPL',   'Pakistan Petroleum Ltd',             170.00),
    ('HBL',   'Habib Bank Ltd',                     150.00),
    ('MCB',   'MCB Bank Ltd',                       230.00),
    ('MEBL',  'Meezan Bank Ltd',                    250.00),
    ('LUCK',  'Lucky Cement Ltd',                   900.00),
    ('ENGRO', 'Engro Corporation',                  330.00),
    ('FFC',   'Fauji Fertilizer Company',           330.00),
    ('HUBC',  'Hub Power Company',                  140.00),
    ('PSO',   'Pakistan State Oil',                 380.00);
