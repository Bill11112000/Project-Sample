package com.pdm.connect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class dbconnect {

    private static final String CONFIG_FILE = "db.properties";

    private dbconnect() {
    }

    // Returns a NEW connection. Callers close it with try-with-resources.
    public static Connection getConnection() throws SQLException {
        Properties settings = loadSettings();

        String url = settings.getProperty("db.url");
        String user = settings.getProperty("db.user");
        String password = settings.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new SQLException("db.properties must define db.url, db.user and db.password");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadSettings() throws SQLException {
        Properties settings = new Properties();
        Path configPath = Path.of(CONFIG_FILE).toAbsolutePath();

        try (InputStream input = Files.newInputStream(configPath)) {
            settings.load(input);
        } catch (IOException ex) {
            throw new SQLException("Cannot read " + configPath
                    + " (current working directory: " + Path.of("").toAbsolutePath() + ")", ex);
        }
        return settings;
    }
}
