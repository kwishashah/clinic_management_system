/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.model;

import java.time.LocalDate;

/**
 * One row of the {@code NeurotherapySessions} table. Immutable projection used by the UI and
 * the {@link com.neuro.repo.SessionRepository repository} layer.
 *
 * <p>The owning {@code patientId} is intentionally not part of this record; it is passed as a
 * separate parameter on the repository APIs that need it (insert / update), which keeps the
 * record focused on a single session's own data.
 *
 * <p>Pain readings are stored as comma-separated {@code before->after} pairs (one per pain
 * label) plus optional {@code L4=before->after} and {@code R4=before->after} entries; see
 * {@code SessionFormDialog} for the encoding contract.
 *
 * <p>For records that have not been persisted yet, {@link #sessionId} is {@link #UNSAVED_ID}
 * (the database assigns the real id on insert). Use {@link #forNew(int, LocalDate, String,
 * String, String, String)} to construct an instance for an insert.
 */
public record Session(
        int sessionId,
        int sessionNumber,
        LocalDate sessionDate,
        String treatment,
        String painBefore,
        String painAfter,
        String summary) {

    /** Sentinel {@code sessionId} value used for sessions that have not yet been persisted. */
    public static final int UNSAVED_ID = 0;

    /**
     * Builds a {@link Session} suitable for an insert: {@code sessionId} is {@link #UNSAVED_ID}
     * because the database will generate it.
     */
    public static Session forNew(
            int sessionNumber,
            LocalDate sessionDate,
            String treatment,
            String painBefore,
            String painAfter,
            String summary) {
        return new Session(UNSAVED_ID, sessionNumber, sessionDate, treatment, painBefore, painAfter, summary);
    }
}
