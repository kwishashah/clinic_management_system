/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.exceptions;

/**
 * Unchecked exception thrown by the DAO layer when an underlying JDBC operation fails. Carries
 * contextual information (entity, identifier, operation) in its message and the original
 * {@link java.sql.SQLException} as the cause.
 */
public class DatabaseException extends RuntimeException {
    /** @param message contextual description of the failure */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * @param message contextual description of the failure
     * @param cause underlying exception (typically {@link java.sql.SQLException})
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
