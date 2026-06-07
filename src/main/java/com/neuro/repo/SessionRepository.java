/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo;

import com.neuro.exceptions.DatabaseException;
import com.neuro.model.Session;
import java.util.List;

/** Domain operations against the {@code NeurotherapySessions} table. */
public interface SessionRepository {
    /** Returns every session for a given patient, ordered as stored in the database. */
    List<Session> getSessionsByPatient(int patientId);

    /** Returns the next session number for a patient (max + 1, or 1 if none). */
    int getNextSessionNumber(int patientId);

    /**
     * Inserts a new session belonging to {@code patientId}. The provided
     * {@link Session#sessionId()} is ignored (the database assigns it); callers should typically
     * build the argument via
     * {@link Session#forNew(int, java.time.LocalDate, String, String, String, String)}.
     */
    void addSession(int patientId, Session session) throws DatabaseException;

    /**
     * Updates an existing session belonging to {@code patientId}. The session is identified by
     * {@link Session#sessionId()}, which must be a persisted id (i.e. not
     * {@link Session#UNSAVED_ID}).
     */
    void updateSession(int patientId, Session session) throws DatabaseException;
}
