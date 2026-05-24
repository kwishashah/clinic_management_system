package com.neuro.application;

import com.neuro.dao.UserDAO;
import com.neuro.db.DBConnection;
import com.neuro.license.LicenseManager;
import com.neuro.ui.LoginFrame;
import com.neuro.ui.SignupFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class NeuroApplication {

    public static void clearClipboard() {
        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(""), null);
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {

        clearClipboard();

        // Machine ID mode
        if (args.length > 0 && args[0].equalsIgnoreCase("--machine-id")) {
            System.out.println(LicenseManager.getMachineIdentifier());
            return;
        }

        // Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // LICENSE CHECK
        boolean valid = LicenseManager.checkLicenseOrExit();
        if (!valid) {
            JOptionPane.showMessageDialog(null,
                    "License invalid or expired. Application will exit.");
            System.exit(0);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::close));

        // START UI
        SwingUtilities.invokeLater(() -> {
            if (UserDAO.hasAnyUser()) {
                new LoginFrame().setVisible(true);
            } else {
                new SignupFrame().setVisible(true);
            }
        });
    }
}