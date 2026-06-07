/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import java.awt.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LicenseDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(LicenseDialog.class);

    private JTextField txtKey;
    private boolean success = false;

    public LicenseDialog() {
        setTitle("Activate License");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setModal(true);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Enter License Key", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtKey = new JTextField();
        JButton btnActivate = new JButton("Activate");
        btnActivate.addActionListener(e -> handleActivate());
        panel.add(title, BorderLayout.NORTH);
        panel.add(txtKey, BorderLayout.CENTER);
        panel.add(btnActivate, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(panel);
    }

    private void handleActivate() {
        try {
            String key = txtKey.getText().trim();
            LicenseInfo info = LicenseManager.validateLicenseKey(key);
            if (info != null && !info.isExpired()) {
                LicenseManager.saveLicense(key);
                success = true;
                dispose();
            } else {
                com.neuro.ui.UiTheme.showInfo(this, "License", "Invalid or expired license.");
            }
        } catch (Exception e) {
            logger.error("License validation failed", e);
            com.neuro.ui.UiTheme.showInfo(this, "Error", "Error validating license.");
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
