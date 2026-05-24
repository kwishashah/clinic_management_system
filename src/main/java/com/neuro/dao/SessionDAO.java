package com.neuro.dao;

import com.neuro.db.DBConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Vector;

public class SessionDAO {

    private static final Logger logger =
            LoggerFactory.getLogger(SessionDAO.class);


    // ================= GET SESSIONS =================
    public static Vector<Vector<Object>> getSessionsByPatient(
            int patientId
    ) throws Exception {

        logger.debug(
                "Loading sessions for patientId={}",
                patientId
        );

        Vector<Vector<Object>> data = new Vector<>();

        String sql = """
            SELECT session_id,
                   session_number,
                   session_date,
                   treatment_given,
                   pain_before,
                   pain_after,
                   session_summary
            FROM NeurotherapySessions
            WHERE patient_id = ?
            ORDER BY session_number ASC
        """;

        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Vector<Object> row = new Vector<>();

                    row.add(rs.getInt("session_id"));
                    row.add(rs.getInt("session_number"));
                    row.add(rs.getDate("session_date"));
                    row.add(rs.getString("treatment_given"));
                    row.add(rs.getString("pain_before"));
                    row.add(rs.getString("pain_after"));
                    row.add(rs.getString("session_summary"));

                    data.add(row);
                }
            }

            if(data.isEmpty()) {

                logger.warn(
                        "No sessions found for patientId={}",
                        patientId
                );
            }

            logger.info(
                    "Loaded {} sessions for patientId={}",
                    data.size(),
                    patientId
            );

            return data;

        } catch(Exception e) {

            logger.error(
                    "Failed loading sessions for patientId={}",
                    patientId,
                    e
            );

            throw e;
        }
    }



    // ================= NEXT SESSION NUMBER =================
    public static int getNextSessionNumber(
            int patientId
    ) {

        logger.debug(
                "Fetching next session number for patientId={}",
                patientId
        );

        String sql =
                "SELECT COALESCE(MAX(session_number),0)+1 " +
                        "FROM NeurotherapySessions " +
                        "WHERE patient_id=?";

        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try(ResultSet rs = ps.executeQuery()) {

                if(rs.next()) {

                    int next =
                            rs.getInt(1);

                    logger.info(
                            "Next session number={} for patientId={}",
                            next,
                            patientId
                    );

                    return next;
                }
            }

        } catch(Exception e) {

            logger.error(
                    "Error getting next session number patientId={}",
                    patientId,
                    e
            );
        }

        return 1;
    }



    // ================= ADD SESSION =================
    public static void addSession(
            int patientId,
            int sessionNo,
            Date date,
            String treatment,
            String painBefore,
            String painAfter,
            String summary
    ) throws Exception {

        logger.info(
                "Saving session patientId={} sessionNo={}",
                patientId,
                sessionNo
        );

        if(treatment == null || treatment.isBlank()) {
            logger.warn(
                    "Treatment blank for patientId={} sessionNo={}",
                    patientId,
                    sessionNo
            );
        }

        if(painBefore == null || painBefore.isBlank()) {
            logger.warn(
                    "Pain before empty patientId={} sessionNo={}",
                    patientId,
                    sessionNo
            );
        }

        if(painAfter == null || painAfter.isBlank()) {
            logger.warn(
                    "Pain after empty patientId={} sessionNo={}",
                    patientId,
                    sessionNo
            );
        }


        String sql = """
            INSERT INTO NeurotherapySessions
            (
                patient_id,
                session_number,
                session_date,
                treatment_given,
                pain_before,
                pain_after,
                session_summary
            )
            VALUES (?,?,?,?,?,?,?)
        """;


        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, sessionNo);
            ps.setDate(3, date);
            ps.setString(4, treatment);
            ps.setString(5, painBefore);
            ps.setString(6, painAfter);
            ps.setString(7, summary);

            int rows =
                    ps.executeUpdate();

            logger.info(
                    "Session saved successfully rows={} patientId={}",
                    rows,
                    patientId
            );

        } catch(Exception e) {

            logger.error(
                    "Session save failed patientId={} sessionNo={}",
                    patientId,
                    sessionNo,
                    e
            );

            throw e;
        }
    }



    // ================= UPDATE SESSION =================
    public static void updateSession(
            int sessionId,
            int sessionNo,
            Date date,
            String treatment,
            String painBefore,
            String painAfter,
            String summary
    ) throws Exception {

        logger.info(
                "Updating sessionId={}",
                sessionId
        );

        String sql = """
            UPDATE NeurotherapySessions
            SET session_number=?,
                session_date=?,
                treatment_given=?,
                pain_before=?,
                pain_after=?,
                session_summary=?
            WHERE session_id=?
        """;

        Connection con = DBConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sessionNo);
            ps.setDate(2, date);
            ps.setString(3, treatment);
            ps.setString(4, painBefore);
            ps.setString(5, painAfter);
            ps.setString(6, summary);
            ps.setInt(7, sessionId);

            int rows =
                    ps.executeUpdate();

            logger.info(
                    "Session updated successfully rows={} sessionId={}",
                    rows,
                    sessionId
            );

        } catch(Exception e) {

            logger.error(
                    "Session update failed sessionId={}",
                    sessionId,
                    e
            );

            throw e;
        }
    }

}