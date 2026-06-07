/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.app;

import com.neuro.db.DBConnection;
import com.neuro.repo.PatientRepository;
import com.neuro.repo.SessionRepository;
import com.neuro.repo.UserRepository;
import com.neuro.repo.impl.JdbcPatientRepository;
import com.neuro.repo.impl.JdbcSessionRepository;
import com.neuro.repo.impl.JdbcUserRepository;

/**
 * Lightweight application-scoped container that carries the three repository instances down the
 * Swing widget tree via constructor injection.
 *
 * <p>Tests build a context with mocks; production builds one with {@link #defaults()} which wires
 * the JDBC repositories against the shared {@link DBConnection}.
 */
public record AppContext(
        UserRepository userRepo, PatientRepository patientRepo, SessionRepository sessionRepo) {
    /** @return a context populated with the production JDBC repositories. */
    public static AppContext defaults() {
        return new AppContext(
                new JdbcUserRepository(DBConnection::getConnection),
                new JdbcPatientRepository(DBConnection::getConnection),
                new JdbcSessionRepository(DBConnection::getConnection));
    }
}
