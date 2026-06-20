/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo.impl;

import com.neuro.exceptions.DatabaseException;
import com.neuro.model.Patient;
import com.neuro.model.PatientSummary;
import com.neuro.repo.Page;
import com.neuro.repo.PatientRepository;
import com.neuro.repo.queries.SqlQueries;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.boot.model.internal.BinderHelper;

/** JDBC-backed {@link PatientRepository}. */
public final class JdbcPatientRepository implements PatientRepository {
    private static final Logger logger = LogManager.getLogger(JdbcPatientRepository.class);

    private final Supplier<Connection> connectionSupplier;

    public JdbcPatientRepository(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
    }

    @Override
    public void savePatient(Patient patient) throws SQLException {
        Objects.requireNonNull(patient, "patient");
        if (patient.getUserId() == null) {
            throw new IllegalArgumentException("patient.userId is required");
        }
        logger.info("Saving patient '{}' for userId={}", patient.getName(), patient.getUserId());
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_INSERT)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getMobile());
            ps.setObject(3, patient.getAge());
            ps.setString(4, patient.getGender());
            ps.setString(5, patient.getMaritalStatus());
            ps.setString(6, patient.getAddress());
            ps.setString(7, patient.getOccupation());
            ps.setString(8, patient.getBloodGroup());
            ps.setObject(9, patient.getHeight());
            ps.setObject(10, patient.getWeight());
            ps.setString(11, patient.getSufferingDuration());
            ps.setString(12, patient.getMainDisease());
            ps.setString(13, patient.getComplications());
            ps.setString(14, patient.getSymptoms());
            ps.setString(15, patient.getPainPoints());
            ps.setString(16, patient.getTongue());
            ps.setString(17, patient.getStool());
            ps.setString(18, patient.getUrine());
            ps.setString(19, patient.getNails());
            ps.setString(20, patient.getNavel());
            ps.setString(21, patient.getNeurotherapyRequired());
            ps.setString(22, patient.getPreviousTreatment());
            ps.setString(23, patient.getMedicines());
            ps.setString(24, patient.getDetailedHistory());
            ps.setString(25, patient.getExamination());
            ps.setString(26, patient.getBp());
            ps.setString(27, patient.getPulse());
            ps.setString(28, patient.getO2());
            ps.setString(29, patient.getTemperature());
            ps.setInt(30, patient.getUserId());
            ps.setString(31, patient.getReports());
            ps.setString(32, patient.getMedia());
            ps.setString(33, patient.getPatientStory());
            ps.setString(34, patient.getRemarks());
            ps.setTimestamp(35, patient.getCreatedAt());
            int rows = ps.executeUpdate();
            logger.info("Patient saved successfully rowsAffected={} mobile={}", rows, patient.getMobile());
        } catch (SQLException e) {
            logger.error("Failed saving patient for userId={}", patient.getUserId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Patient> findById(int patientId) throws DatabaseException {
        logger.info("Fetching patient by id={}", patientId);
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_SELECT_BY_ID)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPatient(rs));
                }
                logger.warn("No patient found for id={}", patientId);
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Failed fetching patient id={}", patientId, e);
            throw new DatabaseException("Failed fetching patient id=" + patientId, e);
        }
    }

    private static Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setName(rs.getString("patient_name"));
        p.setMobile(rs.getString("mobile_number"));
        int age = rs.getInt("age");
        p.setAge(rs.wasNull() ? null : age);
        p.setGender(rs.getString("gender"));
        p.setMaritalStatus(rs.getString("marital_status"));
        p.setAddress(rs.getString("address"));
        p.setOccupation(rs.getString("occupation"));
        p.setBloodGroup(rs.getString("blood_group"));
        BigDecimal height = rs.getBigDecimal("height");
        p.setHeight(rs.wasNull() ? null : height);
        BigDecimal weight = rs.getBigDecimal("weight");
        p.setWeight(rs.wasNull() ? null : weight);
        p.setSufferingDuration(rs.getString("suffering_duration"));
        p.setMainDisease(rs.getString("main_disease"));
        p.setComplications(rs.getString("complications"));
        p.setSymptoms(rs.getString("symptoms"));
        p.setPainPoints(rs.getString("pain_points"));
        p.setTongue(rs.getString("tongue"));
        p.setStool(rs.getString("stool"));
        p.setUrine(rs.getString("urine"));
        p.setNails(rs.getString("nails"));
        p.setNavel(rs.getString("navel"));
        p.setNeurotherapyRequired(rs.getString("neurotherapy_required"));
        p.setPreviousTreatment(rs.getString("previous_treatment"));
        p.setMedicines(rs.getString("medicines"));
        p.setDetailedHistory(rs.getString("detailed_history"));
        p.setExamination(rs.getString("examination"));
        p.setBp(rs.getString("bp"));
        p.setPulse(rs.getString("pulse"));
        p.setO2(rs.getString("o2"));
        p.setTemperature(rs.getString("temperature"));
        p.setReports(rs.getString("reports"));
        p.setMedia(rs.getString("media"));
        p.setPatientStory(rs.getString("patient_story"));
        p.setRemarks(rs.getString("remarks"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }

    @Override
    public Page<PatientSummary> getPatientsSummaryPage(int userId, int page, int pageSize)
            throws DatabaseException {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, pageSize);
        Connection con = connectionSupplier.get();
        try {
            int total;
            try (PreparedStatement countPs = con.prepareStatement(SqlQueries.PATIENT_COUNT_BY_USER)) {
                countPs.setInt(1, userId);
                try (ResultSet rs = countPs.executeQuery()) {
                    total = rs.next() ? rs.getInt(1) : 0;
                }
            }
            // Clamp the requested page so callers never see an empty result when one isn't expected.
            int totalPages = total == 0 ? 1 : (int) Math.ceil(total / (double) safeSize);
            int clampedPage = Math.min(safePage, totalPages - 1);
            int offset = clampedPage * safeSize;
            List<PatientSummary> items = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_SELECT_PAGE_BY_USER)) {
                ps.setInt(1, userId);
                ps.setInt(2, safeSize);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapPatientSummary(rs));
                    }
                }
            }
            logger.info(
                    "Loaded patient page userId={} page={} pageSize={} returned={} total={}",
                    userId, clampedPage, safeSize, items.size(), total);
            return new Page<>(items, total, clampedPage, safeSize);
        } catch (SQLException e) {
            logger.error("Failed fetching patient page for userId={}", userId, e);
            throw new DatabaseException("Failed fetching patient page for userId=" + userId, e);
        }
    }

    @Override
    public Page<PatientSummary> searchPatientsSummaryByMobilePage(
            int userId, String mobile, int page, int pageSize) throws DatabaseException {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, pageSize);
        String like = "%" + (mobile == null ? "" : mobile) + "%";
        Connection con = connectionSupplier.get();
        try {
            int total;
            try (PreparedStatement countPs = con.prepareStatement(SqlQueries.PATIENT_COUNT_BY_MOBILE)) {
                countPs.setInt(1, userId);
                countPs.setString(2, like);
                try (ResultSet rs = countPs.executeQuery()) {
                    total = rs.next() ? rs.getInt(1) : 0;
                }
            }
            int totalPages = total == 0 ? 1 : (int) Math.ceil(total / (double) safeSize);
            int clampedPage = Math.min(safePage, totalPages - 1);
            int offset = clampedPage * safeSize;
            List<PatientSummary> items = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_SEARCH_PAGE_BY_MOBILE)) {
                ps.setInt(1, userId);
                ps.setString(2, like);
                ps.setInt(3, safeSize);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapPatientSummary(rs));
                    }
                }
            }
            logger.info(
                    "Search page userId={} mobileLike={} page={} pageSize={} returned={} total={}",
                    userId, mobile, clampedPage, safeSize, items.size(), total);
            return new Page<>(items, total, clampedPage, safeSize);
        } catch (SQLException e) {
            logger.error("Failed searching patient page for userId={}", userId, e);
            throw new DatabaseException("Failed searching patient page for userId=" + userId, e);
        }
    }

    @Override
    public boolean deletePatient(int patientId, int userId) throws DatabaseException {
        logger.info("Deleting patient id={} for userId={}", patientId, userId);
        Connection con = connectionSupplier.get();
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            // 1. Remove dependent sessions (no ON DELETE CASCADE on the FK).
            try (PreparedStatement ps = con.prepareStatement(SqlQueries.SESSION_DELETE_BY_PATIENT)) {
                ps.setInt(1, patientId);
                int sessionsRemoved = ps.executeUpdate();
                logger.debug("Removed {} session row(s) for patientId={}", sessionsRemoved, patientId);
            }
            // 2. Remove the patient row, scoped by user_id so callers cannot delete other doctors' data.
            int patientRows;
            try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_DELETE)) {
                ps.setInt(1, patientId);
                ps.setInt(2, userId);
                patientRows = ps.executeUpdate();
            }
            con.commit();
            logger.info("Patient delete committed patientId={} userId={} rowsAffected={}",
                    patientId, userId, patientRows);
            return patientRows > 0;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rb) {
                logger.warn("Rollback failed after delete error patientId={}", patientId, rb);
            }
            logger.error("Failed deleting patient id={} userId={}", patientId, userId, e);
            throw new DatabaseException("Failed deleting patient id=" + patientId, e);
        } finally {
            try {
                con.setAutoCommit(originalAutoCommit);
            } catch (SQLException restore) {
                logger.warn("Failed restoring auto-commit after delete patientId={}", patientId, restore);
            }
        }
    }

    private static PatientSummary mapPatientSummary(ResultSet rs) throws SQLException {
        int age = rs.getInt("age");
        Integer ageOrNull = rs.wasNull() ? null : age;
        return new PatientSummary(
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getString("mobile_number"),
                ageOrNull,
                rs.getString("gender"));
    }
    @Override
    public boolean mobileExists(String mobile) throws DatabaseException {

        Connection con = connectionSupplier.get();

        try (PreparedStatement ps =
                     con.prepareStatement(
                             "SELECT 1 FROM PatientHistory WHERE mobile_number = ? LIMIT 1")) {

            ps.setString(1, mobile);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed checking mobile number", e);
        }
    }
}
