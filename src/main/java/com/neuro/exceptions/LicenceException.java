/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.exceptions;

/** Checked exception raised when a license key cannot be decoded or fails validation. */
public class LicenceException extends Exception {
    /** @param message human-readable description of the validation failure */
    public LicenceException(String message) {
        super(message);
    }

    /**
     * @param message human-readable description of the validation failure
     * @param e underlying cause (decoder error, HMAC failure, etc.)
     */
    public LicenceException(String message, Throwable e) {
        super(message, e);
    }
}
