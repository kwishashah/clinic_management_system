package com.neuro.db;

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


    public static Connection getConnection() throws SQLException {

        Properties props = new Properties();

        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            logger.debug("Loading database properties");

            if (in == null) {

                logger.error("db.properties not found");

                throw new RuntimeException(
                        "db.properties not found in classpath"
                );
            }

            props.load(in);

            logger.info("Database properties loaded successfully");

        } catch (IOException e) {

            logger.error(
                    "Error loading db.properties",
                    e
            );

            throw new RuntimeException(
                    "Error loading db.properties",
                    e
            );
        }


        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");


        if (url == null || user == null) {

            logger.error(
                    "Missing DB configuration values"
            );

            throw new RuntimeException(
                    "Missing DB configuration in db.properties"
            );
        }


        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            logger.debug("MySQL driver loaded successfully");

        } catch (ClassNotFoundException e) {

            logger.error(
                    "MySQL Driver not found",
                    e
            );

            throw new RuntimeException(
                    "MySQL Driver not found",
                    e
            );
        }


        if (!url.contains("useSSL")) {

            if (url.contains("?")) {
                url +=
                        "&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            } else {
                url +=
                        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }
        }


        try {

            logger.debug(
                    "Attempting database connection..."
            );

            long start = System.currentTimeMillis();

            Connection con =
                    DriverManager.getConnection(
                            url,
                            user,
                            pass
                    );

            logger.info(
                    "Database connection established in {} ms",
                    System.currentTimeMillis() - start
            );

            return con;

        } catch (SQLException e) {

            logger.error(
                    "Database connection failed",
                    e
            );

            throw e;
        }
    }
}