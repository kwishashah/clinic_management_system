package com.neuro.db;

import com.neuro.exceptions.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection {

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

        File jarDir;
        try {
            jarDir = new File(DBConnection.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new DatabaseException("Cannot determine JAR location", e);
        }

        File propsFile = new File(jarDir, "db.properties");
        if (!propsFile.exists()) {
            throw new DatabaseException("db.properties not found at: " + propsFile.getAbsolutePath());
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(propsFile)) {
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Error loading db.properties", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL Driver not found", e);
        }

        try {
            connection = DriverManager.getConnection(url, user, pass);
            logger.info("Database connected");
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