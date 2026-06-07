/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.application;

import com.neuro.app.AppContext;
import com.neuro.db.DBConnection;
import com.neuro.license.LicenseManager;
import com.neuro.ui.AppIcon;
import com.neuro.ui.LoginFrame;
import com.neuro.ui.SignupFrame;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application entry point for the Neurotherapy Dashboard desktop client.
 *
 * <p>Responsibilities at start-up:
 * <ol>
 *   <li>Print the machine identifier when invoked with {@code --machine-id} (used by license tooling).</li>
 *   <li>Install the system look-and-feel.</li>
 *   <li>Run the license / trial check via {@link LicenseManager#checkLicenseOrExit()}.</li>
 *   <li>Register a shutdown hook that closes the shared {@link DBConnection}.</li>
 *   <li>Launch {@code LoginFrame} or {@code SignupFrame} depending on whether any user exists.</li>
 * </ol>
 */
public class NeuroApplication {
    private static final Logger logger = LogManager.getLogger(NeuroApplication.class);

    /**
     * Program entry point.
     *
     * @param args optional command-line arguments. Supported flag: {@code --machine-id} prints the
     *     hardware identifier and exits.
     */
    public static void main(String[] args) {
        logger.info("Application Started");
        // Machine ID mode
        if (args.length > 0 && args[0].equalsIgnoreCase("--machine-id")) {
            logger.info(LicenseManager.getMachineIdentifier());
            return;
        }
        // Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Failed to set cross-platform look-and-feel; continuing with default", e);
        }
        // LICENSE CHECK
        boolean valid = LicenseManager.checkLicenseOrExit();
        logger.info("License is valid: {}", valid);
        if (!valid) {
            logger.error("License invalid or expired. Application will exit.");
            logger.info("Application exited");
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::close));
        // Wire repositories once and inject them into the UI tree.
        AppContext context = AppContext.defaults();
        // START UI
        SwingUtilities.invokeLater(() -> {
            // Apply the configured clinic logo as the macOS Dock / Windows-Linux taskbar icon
            // so the app no longer shows the default Java coffee-cup glyph when minimized.
            AppIcon.applyToTaskbar();
            boolean hasAnyUser = context.userRepo().hasAnyUser();
            if (hasAnyUser) {
                logger.info("User already exists, opening LoginFrame");
                new LoginFrame(context).setVisible(true);
            } else {
                logger.info("No user found, opening SignupFrame");
                new SignupFrame(context).setVisible(true);
            }
        });
        logger.info("Main thread finished UI bootstrap");
    }
}
