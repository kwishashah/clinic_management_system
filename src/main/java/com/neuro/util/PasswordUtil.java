/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.util;

import java.security.MessageDigest;
import org.mindrot.jbcrypt.BCrypt;

/** Thin wrapper around {@link BCrypt} for hashing and verifying user passwords. */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using BCrypt with a freshly generated salt.
     *
     * @param password plaintext password
     * @return the BCrypt hash (suitable for storage)
     */
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifies that a plaintext password matches a previously generated BCrypt hash.
     *
     * @param password plaintext candidate
     * @param hashedPassword stored BCrypt hash
     * @return {@code true} on match
     */
    public static boolean verify(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /** Returns the SHA-256 hex digest of a string. Retained for ad-hoc use; not part of password storage. */
    @SuppressWarnings("unused")
    private static String sha256(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }

        return hex.toString();
    }
}
