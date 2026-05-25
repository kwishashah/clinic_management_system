package com.neuro.license;

import javax.swing.*;
import java.time.LocalDate;

public class LicenseLauncher {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                String machineId =
                        LicenseManager.getMachineIdentifier();

                System.out.println(
                        "Machine ID: " + machineId
                );

                String license =
                        LicenseGenerator.generateLicense(
                                machineId,
                                "FULL",365
                        );

                System.out.println(
                        "Generated License:\n" + license
                );

                LicenseManager.saveLicense(license);

                JOptionPane.showMessageDialog(
                        null,
                        "License generated successfully!"
                );

            } catch (Exception e) {

                e.printStackTrace();

                JOptionPane.showMessageDialog(
                        null,
                        "License generation failed"
                );
            }
        });
    }
}