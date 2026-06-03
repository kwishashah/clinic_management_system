/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.event.KeyEvent;

public class ClinicSettingsDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(ClinicSettingsDialog.class);
    private JTextField nameField;
    private JLabel logoLabel;
    private JLabel previewLabel;
    private String logoPath;

    public ClinicSettingsDialog() {
        logger.info("Opening Clinic Settings dialog");
        setTitle("Clinic Settings");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 🔹 Clinic Name
        panel.add(new JLabel("Clinic Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(Box.createVerticalStrut(15));

        // 🔹 Upload Button
        JButton uploadBtn = new JButton("Upload Logo");
        panel.add(uploadBtn);

        // 🔹 Logo name
        logoLabel = new JLabel("No file selected");
        panel.add(logoLabel);

        panel.add(Box.createVerticalStrut(10));

        // 🔹 Preview
        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(120, 60));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(previewLabel);

        panel.add(Box.createVerticalStrut(20));

        // 🔹 Save button
        JButton saveBtn = new JButton("Save");
        getRootPane().setDefaultButton(saveBtn);
        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        // ================= ACTIONS =================
        uploadBtn.addActionListener(e -> chooseLogo());
        saveBtn.addActionListener(e -> save());

        loadExisting();
        registerEscapeKey();
    }

    private void chooseLogo() {

        logger.info("Logo upload initiated");

        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (JPG, PNG, JPEG)", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();

            logger.info("User selected logo file={}", file.getAbsolutePath());

            String path = file.getAbsolutePath().toLowerCase();

            if (!(path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"))) {

                logger.warn("Invalid logo format selected={}", file.getName());
                DialogUtil.warning(this, "Invalid image. Use PNG or JPG");
                return;
            }

            try {

                logoPath = file.getAbsolutePath();

                logoLabel.setText(file.getName());

                ImageIcon icon = new ImageIcon(logoPath);

                Image img = icon.getImage().getScaledInstance(120, 60, Image.SCALE_SMOOTH);

                previewLabel.setIcon(new ImageIcon(img));

                logger.info("Logo preview loaded successfully path={}", logoPath);

            } catch (Exception e) {

                logger.error("Failed loading logo preview", e);
                DialogUtil.error(this, "Unable to load image");
            }

        } else {

            logger.debug("Logo upload cancelled by user");
        }
    }

    private void save() {

        try {

            String clinicName = nameField.getText().trim();

            if (clinicName.isEmpty()) {
                logger.warn("Save blocked: clinic name empty");
                DialogUtil.warning(this, "Clinic name is required");
                return;
            }

            logger.info("Saving clinic settings name={} logo={}", clinicName, logoPath);

            ClinicInfo info = new ClinicInfo(clinicName, logoPath);

            ClinicConfig.save(info);

            logger.info("Clinic settings saved successfully");
            DialogUtil.info(this, "Settings saved successfully");
            dispose();
        } catch (Exception e) {
            logger.error("Clinic settings save failed", e);
            DialogUtil.error(this, "Unable to save settings");
        }
    }

    private void loadExisting() {
        try {
            logger.debug("Loading existing clinic settings");

            ClinicInfo info = ClinicConfig.load();

            if (info != null) {

                nameField.setText(info.getName());

                logoPath = info.getLogoPath();

                logger.info("Loaded clinic settings name={} logo={}", info.getName(), logoPath);

                if (logoPath != null && !logoPath.isEmpty()) {

                    File file = new File(logoPath);

                    logoLabel.setText(file.getName());

                    ImageIcon icon = new ImageIcon(logoPath);

                    Image img = icon.getImage().getScaledInstance(120, 60, Image.SCALE_SMOOTH);

                    previewLabel.setIcon(new ImageIcon(img));
                }
            }

        } catch (Exception e) {
            logger.error("Failed loading clinic settings", e);
        }
    }
    private void registerEscapeKey() {

        getRootPane().registerKeyboardAction(
                        e -> dispose(),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

}
