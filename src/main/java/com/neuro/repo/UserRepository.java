/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.repo;

/** Domain operations against the {@code users} table. */
public interface UserRepository {
    /** Validates a username / plaintext-password pair against the stored BCrypt hash. */
    boolean validateUser(String username, String password);

    /** Returns {@code true} when at least one user row exists. */
    boolean hasAnyUser();

    /** Creates a new user, BCrypt-hashing the supplied password. */
    boolean insertUser(String username, String password);

    /** Returns {@code true} if a row with the given username already exists. */
    boolean userExists(String username);

    /** Resolves the auto-increment id for the given username, or {@code -1} if not found. */
    int getUserId(String username);
}
