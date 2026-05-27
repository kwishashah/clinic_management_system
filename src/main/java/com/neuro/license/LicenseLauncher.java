/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LicenseLauncher {

    private static final Logger logger = LogManager.getLogger(LicenseLauncher.class);

    private LicenseLauncher() {}

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {

                String machineId = LicenseManager.getMachineIdentifier();

                System.out.println("Machine ID: " + machineId);

                String license = LicenseGenerator.generateLicense(machineId, "FULL", 365);

                System.out.println("Generated License:\n" + license);

                LicenseManager.saveLicense(license);

                JOptionPane.showMessageDialog(null, "License generated successfully!");

            } catch (Exception e) {
                logger.error("License generation failed", e);
                JOptionPane.showMessageDialog(null, "License generation failed");
            }
        });
    }
}
