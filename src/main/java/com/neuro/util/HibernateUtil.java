package com.neuro.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            // Load Hibernate configuration
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            // Load database properties
            Properties dbProperties = new Properties();

            try (InputStream inputStream =
                         HibernateUtil.class.getClassLoader()
                                 .getResourceAsStream("db.properties")) {

                if (inputStream == null) {
                    throw new RuntimeException("db.properties not found");
                }

                dbProperties.load(inputStream);
            }

            // Override Hibernate properties from db.properties
            configuration.setProperty(
                    "hibernate.connection.url",
                    dbProperties.getProperty("db.url"));

            configuration.setProperty(
                    "hibernate.connection.username",
                    dbProperties.getProperty("db.username"));

            configuration.setProperty(
                    "hibernate.connection.password",
                    dbProperties.getProperty("db.password"));

            configuration.setProperty(
                    "hibernate.connection.driver_class",
                    dbProperties.getProperty("db.driver"));

            return configuration.buildSessionFactory();

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load db.properties: " + e.getMessage());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(

                    "Failed to initialize Hibernate: " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
