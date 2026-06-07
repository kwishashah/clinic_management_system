/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.repo.queries;
/**
 * Centralized SQL statements used by the DAO layer.
 *
 * <p>Kept as {@code public static final} constants so that all query text lives in one place,
 * making review, refactoring and future migration (e.g. to a query loader / JOOQ / JPA) easier.
 */
public final class SqlQueries {
    private SqlQueries() {}
    // ============================================================
    //  USERS
    // ============================================================
    public static final String USER_SELECT_PASSWORD = "SELECT password FROM users WHERE TRIM(username)=?";

    public static final String USER_COUNT = "SELECT COUNT(*) FROM users";

    public static final String USER_INSERT = "INSERT INTO users(username,password) VALUES (?,?)";

    public static final String USER_EXISTS = "SELECT 1 FROM users WHERE TRIM(username)=?";

    public static final String USER_SELECT_ALL_USERNAME_PASSWORD = "SELECT username,password FROM users";

    public static final String USER_UPDATE_PASSWORD = "UPDATE users SET password=? WHERE username=?";

    public static final String USER_SELECT_ID = "SELECT user_id FROM users WHERE TRIM(username)=?";

    // ============================================================
    //  PATIENT HISTORY
    // ============================================================
    public static final String PATIENT_INSERT = "INSERT INTO PatientHistory ("
            + "patient_name, mobile_number, age, gender, marital_status, address, occupation, "
            + "blood_group, height, weight, suffering_duration, main_disease, complications, symptoms, "
            + "pain_points, tongue, stool, urine, nails, navel, neurotherapy_required, previous_treatment, "
            + "medicines, detailed_history, examination, bp, pulse, o2, temperature, user_id, "
            + "reports, media, patient_story, remarks, created_at"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
            + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String PATIENT_SELECT_BY_ID = "SELECT * FROM PatientHistory WHERE patient_id = ?";

    // ---- Paginated patient listing ----
    // The repository exposes only paginated listing APIs so callers cannot accidentally
    // fetch an unbounded result set. Each "page" query is paired with a COUNT query for
    // the total-row metadata used to render "Page X of Y" in the UI.

    /** Total patient count for a user (for paging metadata). */
    public static final String PATIENT_COUNT_BY_USER =
            "SELECT COUNT(*) FROM PatientHistory WHERE user_id=?";

    /** Single page of patients for a user (params: userId, limit, offset). */
    public static final String PATIENT_SELECT_PAGE_BY_USER =
            "SELECT patient_id, patient_name, mobile_number, age, gender "
                    + "FROM PatientHistory "
                    + "WHERE user_id=? "
                    + "ORDER BY patient_id DESC "
                    + "LIMIT ? OFFSET ?";

    /** Total matching patient count for a mobile filter (params: userId, mobileLike). */
    public static final String PATIENT_COUNT_BY_MOBILE =
            "SELECT COUNT(*) FROM PatientHistory WHERE user_id=? AND mobile_number LIKE ?";

    /** Single page of patients matching a mobile filter (params: userId, mobileLike, limit, offset). */
    public static final String PATIENT_SEARCH_PAGE_BY_MOBILE =
            "SELECT patient_id, patient_name, mobile_number, age, gender "
                    + "FROM PatientHistory "
                    + "WHERE user_id=? AND mobile_number LIKE ? "
                    + "ORDER BY patient_id DESC "
                    + "LIMIT ? OFFSET ?";

    /** Removes all sessions for a patient (must run before deleting the patient row). */
    public static final String SESSION_DELETE_BY_PATIENT =
            "DELETE FROM NeurotherapySessions WHERE patient_id=?";

    /** Deletes a patient row, scoped by owning user for safety (params: patientId, userId). */
    public static final String PATIENT_DELETE =
            "DELETE FROM PatientHistory WHERE patient_id=? AND user_id=?";

    // ============================================================
    //  NEUROTHERAPY SESSIONS
    // ============================================================
    public static final String SESSION_SELECT_BY_PATIENT =
            """
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

    public static final String SESSION_NEXT_NUMBER =
            "SELECT COALESCE(MAX(session_number),0)+1 FROM NeurotherapySessions WHERE patient_id=?";

    public static final String SESSION_INSERT =
            """
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

    public static final String SESSION_UPDATE =
            """
            UPDATE NeurotherapySessions
            SET session_number=?,
                session_date=?,
                treatment_given=?,
                pain_before=?,
                pain_after=?,
                session_summary=?
            WHERE session_id=?
            """;
}
