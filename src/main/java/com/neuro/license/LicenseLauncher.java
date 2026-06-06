/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.license;

import javax.swing.*;

import com.neuro.constants.MessageConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.neuro.ui.DialogUtil;
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

                DialogUtil.info(null, MessageConstants.LICENSE_GEN);

            } catch (Exception e) {
                logger.error("License generation failed", e);
                DialogUtil.error(null,MessageConstants.LICENSE_FAIL);
            }
        });
    }
}
