/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo;

import com.neuro.exceptions.DatabaseException;
import com.neuro.model.Patient;
import com.neuro.model.PatientSummary;
import java.sql.SQLException;
import java.util.Optional;

/** Domain operations against the {@code PatientHistory} table. */
public interface PatientRepository {
    /** Loads the full patient record for the given id. */
    Optional<Patient> findById(int patientId) throws DatabaseException;

    /** Persists a full patient intake record. */
    void savePatient(Patient patient) throws SQLException;

    /**
     * Returns one page of patient summaries for the given user, ordered by newest first.
     * The returned {@link Page} also carries the total matching row count so the UI can
     * render "Page X of Y" without issuing a separate count query.
     *
     * <p>This is the only listing API exposed by the repository so callers cannot
     * accidentally fetch an unbounded result set.
     */
    Page<PatientSummary> getPatientsSummaryPage(int userId, int page, int pageSize) throws DatabaseException;

    /**
     * Returns one page of patient summaries for the given user filtered by mobile-number
     * fragment. The returned {@link Page} carries the total matching count.
     */
    Page<PatientSummary> searchPatientsSummaryByMobilePage(
            int userId, String mobile, int page, int pageSize) throws DatabaseException;

    /**
     * Deletes the given patient (and any neurotherapy sessions belonging to them) provided
     * the patient is owned by {@code userId}. Returns {@code true} when a row was deleted.
     */
    boolean deletePatient(int patientId, int userId) throws DatabaseException;
}
