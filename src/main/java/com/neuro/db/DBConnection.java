/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.db;

import com.neuro.exceptions.DatabaseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application-wide JDBC connection holder.
 *
 * <p>Loads database credentials from a {@code db.properties} file located at
 * {@code ${user.home}/.neuro/config/db.properties}, opens a single {@link Connection} the first
 * time it is requested, and caches it for subsequent callers. All access is synchronized so this
 * class is safe to use from multiple threads.
 */
public final class DBConnection {
    private static final Logger logger = LogManager.getLogger(DBConnection.class);

    private DBConnection() {}

    private static Connection connection;

    /**
     * Returns the shared {@link Connection}, opening one if none exists or the cached one is
     * closed. Database URL / username / password are read from {@code db.properties}.
     *
     * @return a live JDBC connection
     * @throws com.neuro.exceptions.DatabaseException if the properties file is missing, the JDBC
     *     driver cannot be loaded, or the connection attempt fails
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            logger.warn("Error checking connection state, will reconnect", e);
        }
        Path configPath = Paths.get(System.getProperty("user.home"), ".neuro", "config", "db.properties");
        File propsFile = configPath.toFile();
        if (!propsFile.exists()) {
            throw new DatabaseException("db.properties not found at: " + propsFile.getAbsolutePath());
        }
        Properties props = new Properties();
        try {
            List<String> lines = Files.readAllLines(propsFile.toPath());
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("=", 2);
                props.put(parts[0].trim(), (parts.length > 1) ? parts[1].trim() : "");
            }
        } catch (IOException e) {
            throw new DatabaseException("Error loading db.properties", e);
        }
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.debug("JDBC driver loaded");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL Driver not found", e);
        }
        String effectiveUrl = ensureCreateDatabaseIfMissing(url);
        try {
            connection = DriverManager.getConnection(effectiveUrl, user, pass);
            logger.info("Database connected url={}", effectiveUrl);
            initSchema(connection);
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Database connection failed for url=" + effectiveUrl, e);
        }
    }

    /**
     * Appends {@code createDatabaseIfNotExist=true} (MySQL Connector/J extension) to the JDBC URL
     * if it is not already present, so the schema is auto-created on first run.
     */
    private static String ensureCreateDatabaseIfMissing(String url) {
        if (url == null || url.contains("createDatabaseIfNotExist")) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + "createDatabaseIfNotExist=true";
    }

    /**
     * Runs the bundled {@code schema.sql} (CREATE TABLE IF NOT EXISTS ...) so a fresh install gets
     * a usable database on first launch. Silently no-ops if the resource is absent.
     */
    private static void initSchema(Connection con) {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                logger.warn("schema.sql not found on classpath; skipping schema bootstrap");
                return;
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement stmt = con.createStatement()) {
                for (String raw : script.split(";")) {
                    String sql = stripSqlComments(raw).trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    stmt.execute(sql);
                }
            }
            logger.info("Schema bootstrap complete");
        } catch (IOException | SQLException e) {
            throw new DatabaseException("Schema bootstrap failed", e);
        }
    }

    private static String stripSqlComments(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        for (String line : sql.split("\\R")) {
            String trimmed = line.stripLeading();
            if (trimmed.startsWith("--")) {
                continue;
            }
            out.append(line).append('\n');
        }
        return out.toString();
    }

    /**
     * Closes the cached connection if one is open. Safe to call multiple times. Intended to be
     * wired into a JVM shutdown hook.
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
            connection = null;
        }
    }
}
