package com.neuro.application;

import com.neuro.dao.UserDAO;
import com.neuro.db.DBConnection;
import com.neuro.license.LicenseManager;
import com.neuro.ui.LoginFrame;
import com.neuro.ui.SignupFrame;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NeuroApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(NeuroApplication.class);

    public static void main(String[] args) {
        logger.info("Application Started");
        // Machine ID mode
        if (args.length > 0 && args[0].equalsIgnoreCase("--machine-id")) {
            logger.info(LicenseManager.getMachineIdentifier());
            return;
        }

        // Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Failed to set system look and feel", e);
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

        // START UI
        SwingUtilities.invokeLater(() -> {
            boolean hasAnyUser = UserDAO.hasAnyUser();
            if (hasAnyUser) {
                logger.info("User already exists, opening LoginFrame");
                new LoginFrame().setVisible(true);
            } else {
                logger.info("No user found, opening SignupFrame");
                new SignupFrame().setVisible(true);
            }
        });
        logger.info("Main thread finished UI bootstrap");
    }
}