package com.neuro.db;

import com.neuro.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection {

    private static final String PROPERTIES_FILE = "db.properties";

    private static final Logger logger =
            LoggerFactory.getLogger(DBConnection.class);

    private static Connection connection;

    public static synchronized Connection getConnection() {

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            logger.warn("Error checking connection state, will reconnect", e);
        }

        Properties props = new Properties();

        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (in == null) {
                throw new DatabaseException("db.properties not found in classpath");
            }

            props.load(in);

        } catch (IOException e) {
            throw new DatabaseException("Error loading db.properties", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        if (url == null || user == null) {
            throw new DatabaseException("Missing DB configuration in db.properties");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL Driver not found", e);
        }

        if (!url.contains("useSSL")) {
            if (url.contains("?")) {
                url += "&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            } else {
                url += "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }

        try {
            long start = System.currentTimeMillis();

            connection = DriverManager.getConnection(url, user, pass);

            logger.info(
                    "Database connection established in {} ms",
                    System.currentTimeMillis() - start
            );

            return connection;

        } catch (SQLException e) {
            throw new DatabaseException("Database connection failed", e);
        }
    }

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