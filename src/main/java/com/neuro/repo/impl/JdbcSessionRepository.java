/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo.impl;

import com.neuro.exceptions.DatabaseException;
import com.neuro.model.Session;
import com.neuro.repo.SessionRepository;
import com.neuro.repo.queries.SqlQueries;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** JDBC-backed {@link SessionRepository}. */
public final class JdbcSessionRepository implements SessionRepository {
    private static final Logger logger = LogManager.getLogger(JdbcSessionRepository.class);

    private final Supplier<Connection> connectionSupplier;

    public JdbcSessionRepository(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
    }

    @Override
    public List<Session> getSessionsByPatient(int patientId) {
        logger.debug("Loading sessions for patientId={}", patientId);
        List<Session> data = new ArrayList<>();
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.SESSION_SELECT_BY_PATIENT)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("session_date");
                    data.add(new Session(
                            rs.getInt("session_id"),
                            rs.getInt("session_number"),
                            sqlDate == null ? null : sqlDate.toLocalDate(),
                            rs.getString("treatment_given"),
                            rs.getString("pain_before"),
                            rs.getString("pain_after"),
                            rs.getString("session_summary")));
                }
            }
            if (data.isEmpty()) {
                logger.warn("No sessions found for patientId={}", patientId);
            }
            logger.info("Loaded {} sessions for patientId={}", data.size(), patientId);
            return data;
        } catch (SQLException e) {
            logger.error("Failed loading sessions for patientId={}", patientId, e);
            throw new DatabaseException("Failed loading sessions for patientId=" + patientId, e);
        }
    }

    @Override
    public int getNextSessionNumber(int patientId) {
        logger.debug("Fetching next session number for patientId={}", patientId);
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.SESSION_NEXT_NUMBER)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int next = rs.getInt(1);
                    logger.info("Next session number={} for patientId={}", next, patientId);
                    return next;
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting next session number patientId={}", patientId, e);
            throw new DatabaseException("Error getting next session number patientId=" + patientId, e);
        }
        return 1;
    }

    @Override
    public void addSession(int patientId, Session session) throws DatabaseException {
        int sessionNo = session.sessionNumber();
        logger.info("Saving session patientId={} sessionNo={}", patientId, sessionNo);
        if (session.treatment() == null || session.treatment().isBlank()) {
            logger.warn("Treatment blank for patientId={} sessionNo={}", patientId, sessionNo);
        }
        if (session.painBefore() == null || session.painBefore().isBlank()) {
            logger.warn("Pain before empty patientId={} sessionNo={}", patientId, sessionNo);
        }
        if (session.painAfter() == null || session.painAfter().isBlank()) {
            logger.warn("Pain after empty patientId={} sessionNo={}", patientId, sessionNo);
        }
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.SESSION_INSERT)) {
            ps.setInt(1, patientId);
            ps.setInt(2, sessionNo);
            ps.setDate(3, toSqlDate(session.sessionDate()));
            ps.setString(4, session.treatment());
            ps.setString(5, session.painBefore());
            ps.setString(6, session.painAfter());
            ps.setString(7, session.summary());
            int rows = ps.executeUpdate();
            logger.info("Session saved successfully rows={} patientId={}", rows, patientId);
        } catch (SQLException e) {
            logger.error("Session save failed patientId={} sessionNo={}", patientId, sessionNo, e);
            throw new DatabaseException(
                    "Session save failed patientId=" + patientId + " sessionNo=" + sessionNo, e);
        }
    }

    @Override
    public void updateSession(int patientId, Session session) throws DatabaseException {
        if (session.sessionId() == Session.UNSAVED_ID) {
            throw new IllegalArgumentException("Cannot update a session with UNSAVED_ID; use addSession instead");
        }
        int sessionId = session.sessionId();
        logger.info("Updating sessionId={} patientId={}", sessionId, patientId);
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.SESSION_UPDATE)) {
            ps.setInt(1, session.sessionNumber());
            ps.setDate(2, toSqlDate(session.sessionDate()));
            ps.setString(3, session.treatment());
            ps.setString(4, session.painBefore());
            ps.setString(5, session.painAfter());
            ps.setString(6, session.summary());
            ps.setInt(7, sessionId);
            int rows = ps.executeUpdate();
            logger.info("Session updated successfully rows={} sessionId={}", rows, sessionId);
        } catch (SQLException e) {
            logger.error("Session update failed sessionId={}", sessionId, e);
            throw new DatabaseException("Session update failed sessionId=" + sessionId, e);
        }
    }

    /** Converts a {@link LocalDate} to a {@link java.sql.Date}, preserving {@code null}. */
    private static Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }
}
