package com.neuro.dao;

import com.neuro.db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(PatientDAO.class);


    // ================= SAVE PATIENT =================

    public static void savePatient(
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
            Timestamp createdAt
    ) throws SQLException {

        try {

            logger.info(
                    "Saving patient '{}' for userId={}",
                    name,
                    userId
            );

            String sql =
                    "INSERT INTO PatientHistory ("
                            + "patient_name, mobile_number, age, gender, marital_status, address, occupation, "
                            + "blood_group, height, weight, suffering_duration, main_disease, complications, symptoms, "
                            + "pain_points, tongue, stool, urine, nails, navel, neurotherapy_required, previous_treatment, "
                            + "medicines, detailed_history, examination, bp, pulse, o2, temperature, user_id, "
                            + "reports, media, patient_story, remarks, created_at"
                            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

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

                logger.info(
                        "Patient saved successfully rowsAffected={} mobile={}",
                        rows,
                        mobile
                );
            }

        } catch (SQLException e) {

            logger.error(
                    "Failed saving patient for userId={}",
                    userId,
                    e
            );

            throw e;
        }
    }



    // ================= GET ALL PATIENTS =================

    public static List<Object[]> getAllPatients(int userId) throws Exception {

        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT patient_id, patient_name, mobile_number, age, gender " +
                        "FROM PatientHistory " +
                        "WHERE user_id=? " +
                        "ORDER BY patient_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            logger.info(
                    "Fetching patients for userId={}",
                    userId
            );

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("mobile_number"),
                            rs.getInt("age"),
                            rs.getString("gender")
                    });
                }
            }
        }

        logger.info(
                "Loaded {} patients for userId={}",
                list.size(),
                userId
        );

        if (list.isEmpty()) {
            logger.warn(
                    "No patients found for userId={}",
                    userId
            );
        }

        return list;
    }



    // ================= SEARCH =================

    public static List<Object[]> searchPatientsByMobile(
            int userId,
            String mobile
    ) throws Exception {

        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT patient_id, patient_name, mobile_number, age, gender " +
                        "FROM PatientHistory " +
                        "WHERE user_id=? AND mobile_number LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            logger.info(
                    "Searching patients userId={} mobileLike={}",
                    userId,
                    mobile
            );

            ps.setInt(1, userId);
            ps.setString(2, "%" + mobile + "%");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("mobile_number"),
                            rs.getInt("age"),
                            rs.getString("gender")
                    });
                }
            }
        }

        logger.info(
                "Search returned {} patients",
                list.size()
        );

        return list;
    }
}