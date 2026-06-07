/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo.impl;

import com.neuro.exceptions.DatabaseException;
import com.neuro.repo.UserRepository;
import com.neuro.repo.queries.SqlQueries;
import com.neuro.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** JDBC-backed {@link UserRepository}. */
public final class JdbcUserRepository implements UserRepository {
    private static final Logger logger = LogManager.getLogger(JdbcUserRepository.class);

    private final Supplier<Connection> connectionSupplier;

    public JdbcUserRepository(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
    }

    @Override
    public boolean validateUser(String username, String password) {
        Connection conn = connectionSupplier.get();
        try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.USER_SELECT_PASSWORD)) {
            logger.info("Login validation requested for username={}", username);
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean valid = PasswordUtil.verify(password, rs.getString("password"));
                    if (valid) {
                        logger.info("Login success username={}", username);
                    } else {
                        logger.warn("Invalid password username={}", username);
                    }
                    return valid;
                }
            }
            logger.warn("Login failed user not found username={}", username);
            return false;
        } catch (SQLException e) {
            logger.error("Login validation failed for username={}", username, e);
            throw new DatabaseException("Login validation failed for username=" + username, e);
        }
    }

    @Override
    public boolean hasAnyUser() {
        logger.info("hasAnyUser() called");
        Connection conn = connectionSupplier.get();
        try (PreparedStatement ps = conn.prepareStatement(SqlQueries.USER_COUNT);
                ResultSet rs = ps.executeQuery()) {
            boolean exists = rs.next() && rs.getInt(1) > 0;
            logger.info("Has any user check result={}", exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Failed checking whether any user exists", e);
            throw new DatabaseException("Failed checking whether any user exists", e);
        }
    }

    @Override
    public boolean insertUser(String username, String password) {
        logger.info("Creating user username={}", username);
        String hashedPassword = PasswordUtil.hash(password);
        Connection conn = connectionSupplier.get();
        try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.USER_INSERT)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword);
            int rows = stmt.executeUpdate();
            logger.info("User created username={} rows={}", username, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("User creation failed for username={}", username, e);
            throw new DatabaseException("User creation failed for username=" + username, e);
        }
    }

    @Override
    public boolean userExists(String username) {
        Connection conn = connectionSupplier.get();
        try (PreparedStatement ps = conn.prepareStatement(SqlQueries.USER_EXISTS)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.info("Username exists check username={} result={}", username, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("User existence check failed for username={}", username, e);
            throw new DatabaseException("User existence check failed for username=" + username, e);
        }
    }

    @Override
    public int getUserId(String username) {
        Connection conn = connectionSupplier.get();
        try (PreparedStatement stmt = conn.prepareStatement(SqlQueries.USER_SELECT_ID)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    logger.info("Resolved userId={} for username={}", userId, username);
                    return userId;
                }
            }
            logger.warn("No userId found for username={}", username);
            return -1;
        } catch (SQLException e) {
            logger.error("getUserId failed for username={}", username, e);
            throw new DatabaseException("getUserId failed for username=" + username, e);
        }
    }
}
