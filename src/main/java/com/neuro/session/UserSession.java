/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004@gmail.com for more details.
 */
package com.neuro.session;

/**
 * Holds the currently authenticated user's identity for the lifetime of the running JVM. Backed by
 * static state because the application is a single-user desktop client.
 */
public class UserSession {
    private static int userId;
    private static String username;

    /** @return the id of the currently logged-in user, or 0 if no session is active. */
    public static int getUserId() {
        return userId;
    }

    /** Records the id of the currently logged-in user. */
    public static void setUserId(int id) {
        userId = id;
    }

    /** @return the username of the currently logged-in user, or {@code null}. */
    public static String getUsername() {
        return username;
    }

    /** Records the username of the currently logged-in user. */
    public static void setUsername(String name) {
        username = name;
    }

    /** Clears the session on logout. */
    public static void clear() {
        userId = 0;
        username = null;
    }
}
