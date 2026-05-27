/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Immutable value object describing a parsed license: its type (e.g. {@code FULL}, {@code TRIAL})
 * and its expiry date. Expiry checks consult {@link LicenseManager#getTrustedDate()} rather than
 * the local clock to resist tampering.
 */
public class LicenseInfo {

    private final String type;
    private final LocalDate expiry;

    /**
     * @param type license type label
     * @param expiry expiry date; never {@code null} for a valid license
     */
    public LicenseInfo(String type, LocalDate expiry) {
        this.type = type;
        this.expiry = expiry;
    }

    /** @return {@code true} if the license has no expiry or the trusted date is past expiry. */
    public boolean isExpired() {
        LocalDate today = LicenseManager.getTrustedDate();
        return expiry == null || today.isAfter(expiry);
    }

    /** @return number of whole days until expiry; never negative. */
    public long daysLeft() {
        LocalDate today = LicenseManager.getTrustedDate();
        long days = ChronoUnit.DAYS.between(today, expiry);
        return Math.max(days, 0);
    }

    /** @return the license type label. */
    public String getType() {
        return type;
    }

    /** @return the expiry date. */
    public LocalDate getExpiry() {
        return expiry;
    }
}
