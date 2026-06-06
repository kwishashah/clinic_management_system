/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.constants.ErrorConstants;
import com.neuro.constants.MessageConstants;
import com.neuro.repo.PatientRepository;
import com.neuro.session.UserSession;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PatientForm extends JDialog {

    private static final Logger logger = LogManager.getLogger(PatientForm.class);

    private Runnable onSaveCallback;

    private JTextField txtName, txtMobile, txtAge, txtDate;
    private JComboBox<String> cmbGender, cmbMarital;
    private JTextArea txtSymptoms;
    private JButton btnSave;

    private final PatientRepository patientRepo;

    public PatientForm(JFrame parent, Runnable onSaveCallback, AppContext context) {
        super(parent, true);
        this.onSaveCallback = onSaveCallback;
        this.patientRepo = context.patientRepo();

        setTitle("Add Patient");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Patient Registration");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Patient Name:"), gbc);
        txtName = new JTextField();
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        // Mobile
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Mobile:"), gbc);
        txtMobile = new JTextField();
        gbc.gridx = 1;
        panel.add(txtMobile, gbc);

        // Age
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Age:"), gbc);
        txtAge = new JTextField();
        gbc.gridx = 1;
        panel.add(txtAge, gbc);

        // Gender
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Gender:"), gbc);
        cmbGender = new JComboBox<>(new String[] {"-- Select --", "Male", "Female", "Other"});
        gbc.gridx = 1;
        panel.add(cmbGender, gbc);

        // Marital
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Marital Status:"), gbc);
        cmbMarital = new JComboBox<>(new String[] {"-- Select --", "Single", "Married", "Divorced", "Widowed"});
        gbc.gridx = 1;
        panel.add(cmbMarital, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Visit Date:"), gbc);
        txtDate = new JTextField(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        gbc.gridx = 1;
        panel.add(txtDate, gbc);

        // Symptoms
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Symptoms:"), gbc);
        txtSymptoms = new JTextArea(3, 20);
        panel.add(new JScrollPane(txtSymptoms), gbc);

        // Save Button
        btnSave = new JButton("Save Patient");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> {
            if (validateForm()) {
                savePatient();
            }
        });

        add(panel);
    }

    private boolean validateForm() {

        if (txtName.getText().trim().isEmpty()) {
            DialogUtil.warning(this,ErrorConstants.PATIENT_NAME_REQD);
            return false;
        }

        if (!txtMobile.getText().trim().matches("[6-9][0-9]{9}")) {
            DialogUtil.warning(this, "Invalid mobile number");
            return false;
        }

        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            if (age < 1 || age > 120) {
                DialogUtil.warning(this, ErrorConstants.INVALID_AGE);
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid age input: {}", txtAge.getText(), e);
            DialogUtil.warning(this, ErrorConstants.AGE_NUMBER);
            return false;
        }

        if (cmbGender.getSelectedIndex() == 0 || cmbMarital.getSelectedIndex() == 0) {
            DialogUtil.warning(this,ErrorConstants.SELECT_GENDER_MARITAL);
            return false;
        }

        return true;
    }

    private void savePatient() {

        try {
            java.sql.Timestamp ts = new java.sql.Timestamp(
                    new SimpleDateFormat("dd-MM-yyyy").parse(txtDate.getText()).getTime());

            patientRepo.savePatient(
                    txtName.getText(),
                    txtMobile.getText(),
                    Integer.parseInt(txtAge.getText().trim()),
                    (String) cmbGender.getSelectedItem(),
                    (String) cmbMarital.getSelectedItem(),
                    "",
                    "",
                    "", // address, occupation, bloodgroup
                    0f,
                    0f,
                    "",
                    "",
                    "",
                    "",
                    txtSymptoms.getText(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "", // neuro + history
                    "",
                    "",
                    "",
                    "", // vitals
                    UserSession.getUserId(), // ✅ FIXED USER ID
                    "",
                    "",
                    "",
                    "",
                    ts);

            DialogUtil.info(this, MessageConstants.SAVED);

            if (onSaveCallback != null) onSaveCallback.run();

            dispose();

        } catch (Exception e) {
            logger.error("Patient save failed userId={}", UserSession.getUserId(), e);
            DialogUtil.error(this, ErrorConstants.UNABLE_TO_SAVE_PATIENT);
        }
    }
}
