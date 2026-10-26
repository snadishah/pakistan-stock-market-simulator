package com.psms.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    // Configure via environment variables; defaults match a local dev MySQL install.
    private static final String URL = env("PSMS_DB_URL", "jdbc:mysql://localhost:3306/psx_simulator");
    private static final String USER = env("PSMS_DB_USER", "root");
    private static final String PASSWORD = env("PSMS_DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? fallback : value;
    }
}
