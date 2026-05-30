/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo.impl;

import com.neuro.exceptions.DatabaseException;
import com.neuro.repo.PatientRepository;
import com.neuro.repo.queries.SqlQueries;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.neuro.model.PatientSummary;
/** JDBC-backed {@link PatientRepository}. */
public final class JdbcPatientRepository implements PatientRepository {

    private static final Logger logger = LogManager.getLogger(JdbcPatientRepository.class);

    private final Supplier<Connection> connectionSupplier;

    public JdbcPatientRepository(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
    }

    @Override
    public void savePatient(
            String name,
            String mobile,
            Integer age,
            String gender,
            String maritalStatus,
            String address,
            String occupation,
            String bloodGroup,
            Float height,
            Float weight,
            String sufferingDuration,
            String mainDisease,
            String complications,
            String symptoms,
            String painPoints,
            String tongue,
            String stool,
            String urine,
            String nails,
            String navel,
            String neurotherapyRequired,
            String previousTreatment,
            String medicines,
            String detailedHistory,
            String examination,
            String bp,
            String pulse,
            String o2,
            String temperature,
            int userId,
            String reports,
            String media,
            String patientStory,
            String remarks,
            Timestamp createdAt)
            throws SQLException {

        logger.info("Saving patient '{}' for userId={}", name, userId);
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_INSERT)) {
            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setObject(3, age);
            ps.setString(4, gender);
            ps.setString(5, maritalStatus);
            ps.setString(6, address);
            ps.setString(7, occupation);
            ps.setString(8, bloodGroup);
            ps.setObject(9, height);
            ps.setObject(10, weight);
            ps.setString(11, sufferingDuration);
            ps.setString(12, mainDisease);
            ps.setString(13, complications);
            ps.setString(14, symptoms);
            ps.setString(15, painPoints);
            ps.setString(16, tongue);
            ps.setString(17, stool);
            ps.setString(18, urine);
            ps.setString(19, nails);
            ps.setString(20, navel);
            ps.setString(21, neurotherapyRequired);
            ps.setString(22, previousTreatment);
            ps.setString(23, medicines);
            ps.setString(24, detailedHistory);
            ps.setString(25, examination);
            ps.setString(26, bp);
            ps.setString(27, pulse);
            ps.setString(28, o2);
            ps.setString(29, temperature);
            ps.setInt(30, userId);
            ps.setString(31, reports);
            ps.setString(32, media);
            ps.setString(33, patientStory);
            ps.setString(34, remarks);
            ps.setTimestamp(35, createdAt);

            int rows = ps.executeUpdate();
            logger.info("Patient saved successfully rowsAffected={} mobile={}", rows, mobile);
        } catch (SQLException e) {
            logger.error("Failed saving patient for userId={}", userId, e);
            throw e;
        }
    }


    @Override
    public List<PatientSummary> getPatients(
            int userId,
            int page,
            int pageSize
    ) throws DatabaseException {

        List<PatientSummary> list = new ArrayList<>();

        int offset = (page - 1) * pageSize;

        String sql = """
        SELECT
            patient_id,
            patient_name,
            mobile_number,
            age,
            gender
        FROM PatientHistory
        WHERE user_id = ?
        ORDER BY patient_id DESC
        LIMIT ? OFFSET ?
    """;

        Connection con = connectionSupplier.get();

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            logger.info(
                    "Fetching patients userId={} page={} pageSize={}",
                    userId,
                    page,
                    pageSize
            );

            ps.setInt(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new PatientSummary(
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("mobile_number"),
                            rs.getInt("age"),
                            rs.getString("gender")
                    ));
                }
            }

            logger.info(
                    "Loaded {} patients for userId={}",
                    list.size(),
                    userId
            );

        } catch (SQLException e) {

            logger.error(
                    "Failed fetching paginated patients for userId={}",
                    userId,
                    e
            );

            throw new DatabaseException(
                    "Failed fetching patients for userId=" + userId,
                    e
            );
        }

        return list;
    }
    @Override
    public int getPatientCount(int userId) throws DatabaseException {

        String sql = """
        SELECT COUNT(*)
        FROM PatientHistory
        WHERE user_id = ?
    """;

        Connection con = connectionSupplier.get();

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {

            logger.error(
                    "Failed counting patients for userId={}",
                    userId,
                    e
            );

            throw new DatabaseException(
                    "Failed counting patients",
                    e
            );
        }

        return 0;
    }
    @Override
    public List<PatientSummary> searchPatientsByMobile(int userId, String mobile) throws DatabaseException {
        List<PatientSummary> list = new ArrayList<>();
        Connection con = connectionSupplier.get();
        try (PreparedStatement ps = con.prepareStatement(SqlQueries.PATIENT_SEARCH_BY_MOBILE)) {
            logger.info("Searching patients userId={} mobileLike={}", userId, mobile);
            ps.setInt(1, userId);
            ps.setString(2, "%" + mobile + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PatientSummary (
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("mobile_number"),
                        rs.getInt("age"),
                        rs.getString("gender")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed searching patients for userId={}", userId, e);
            throw new DatabaseException("Failed searching patients for userId=" + userId, e);
        }
        logger.info("Search returned {} patients", list.size());
        return list;
    }
}
