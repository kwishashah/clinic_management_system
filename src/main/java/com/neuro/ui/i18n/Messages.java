/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Centralized lookup for all user-facing UI strings.
 *
 * <p>Backed by a standard {@link ResourceBundle} (base name {@code messages}) so that
 * additional locales can be added simply by dropping a sibling
 * {@code messages_<lang>.properties} file onto the classpath. Keys are stable English
 * identifiers (e.g. {@code dashboard.title}); only values are translated.
 *
 * <p>This class is a static helper and not instantiable.
 */
public final class Messages {
    private static final Logger logger = LogManager.getLogger(Messages.class);

    /** Base name of the resource bundle on the classpath (without the {@code _en} suffix). */
    private static final String BUNDLE_BASE_NAME = "messages";

    private static volatile ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.getDefault());

    private Messages() {
        // Static utility class; not instantiable.
    }

    /**
     * Reloads the bundle for the given locale. Useful when the application allows
     * runtime language switching. Safe to call from any thread.
     */
    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
        logger.info("Messages bundle reloaded for locale={}", locale);
    }

    /**
     * Returns the translated string for {@code key}. If the key is missing, the key
     * itself is returned (and a warning is logged) so a missing translation never
     * crashes the UI.
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            logger.warn("Missing message key '{}' in bundle {}", key, BUNDLE_BASE_NAME);
            return key;
        }
    }

    /**
     * Returns the translated, {@link MessageFormat}-formatted string for {@code key}.
     * Use this for messages with placeholders such as {@code "Hello {0}"}.
     */
    public static String format(String key, Object... args) {
        String pattern = get(key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }
}
