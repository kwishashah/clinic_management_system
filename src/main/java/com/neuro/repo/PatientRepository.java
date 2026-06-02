/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo;

import com.neuro.exceptions.DatabaseException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/** Domain operations against the {@code PatientHistory} table. */
public interface PatientRepository {

    /** Persists a full patient intake record. */
    void savePatient(
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
            throws SQLException;

    /** Returns a summary view of every patient owned by the given user. */
    List<Object[]> getAllPatients(int userId) throws DatabaseException;
    List<Object[]> getPatientsPage(int userId, int offset, int limit);

    int getPatientCount(int userId);
    /** Searches the user's patients by mobile-number fragment. */
    List<Object[]> searchPatientsByMobile(int userId, String mobile) throws DatabaseException;
}
