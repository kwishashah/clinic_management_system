/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.repo.PatientRepository;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLIntegrityConstraintViolationException;
import com.neuro.constants.MessageConstants;
import com.neuro.constants.ErrorConstants;
public class PatientHistoryFormMySQL extends JFrame {
    private static final Logger logger = LogManager.getLogger(PatientHistoryFormMySQL.class);
    private JTextField txtName, txtMobile, txtAge, txtOccupation;
    private JTextField txtBloodGroup, txtHeight, txtWeight, txtDuration;
    private JTextField txtBP, txtPulse, txtO2, txtTemp;
    private JTextArea txtAddress, txtMainDisease, txtComplications;
    private JTextArea txtSymptoms, txtAllergy, txtRemarks;
    private JTextArea txtPreviousTreatment, txtMedicines, txtDetailedHistory;
    private JTextArea txtExamination, txtReportAnalysis;

    private JTextField txtReportPath;
    private JButton btnUploadReport;

    private JComboBox<String>[] painFields = new JComboBox[18];
    private JComboBox<String> left4th, right4th;
    private JComboBox<String> cmbGender, cmbMarital;

    // ✅ ONLY ONE userId
    private int userId;

    // ✅ Constructor receives userId
    private final PatientRepository patientRepo;

    public PatientHistoryFormMySQL(int userId, AppContext context) {

        this.userId = userId;
        this.patientRepo = context.patientRepo();

        logger.info("Opening Patient History Form for userId={}", userId);

        setTitle("Patient History Form");
        setSize(750, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridwidth = 1;

        // ===== Fields =====
        txtName = new JTextField(20);
        txtMobile = new JTextField(20);
        txtAge = new JTextField(5);

        cmbGender = new JComboBox<>(new String[] {"Male", "Female", "Other"});
        cmbMarital = new JComboBox<>(new String[] {"Single", "Married"});

        txtAddress = createArea();
        txtOccupation = new JTextField(20);
        txtBloodGroup = new JTextField();

        txtHeight = new JTextField(5);
        txtWeight = new JTextField(5);
        txtDuration = new JTextField();

        txtMainDisease = createArea();
        txtComplications = createArea();
        txtSymptoms = createArea();

        txtPreviousTreatment = createArea();
        txtMedicines = createArea();
        txtDetailedHistory = createArea();
        txtExamination = createArea();

        txtReportAnalysis = createArea();
        txtAllergy = createArea();
        txtRemarks = createArea();

        txtReportPath = new JTextField(20);
        txtReportPath.setEditable(false);

        btnUploadReport = new JButton("Upload Report");

        txtBP = new JTextField(8);
        txtPulse = new JTextField(5);
        txtO2 = new JTextField(5);
        txtTemp = new JTextField(5);

        // ===== Layout =====
        y = addSectionHeader(panel, gbc, y, "Patient Information");
        y = addRow(panel, gbc, y, "Name", txtName);
        y = addRow(panel, gbc, y, "Mobile", txtMobile);
        JPanel demographicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        demographicPanel.add(new JLabel("Age"));
        demographicPanel.add(txtAge);

        demographicPanel.add(new JLabel("Gender"));
        demographicPanel.add(cmbGender);

        demographicPanel.add(new JLabel("Marital"));
        demographicPanel.add(cmbMarital);

        y = addRow(panel, gbc, y, "Patient Info", demographicPanel);
        y = addRow(panel, gbc, y, "Address", txtAddress);
        y = addRow(panel, gbc, y, "Occupation", txtOccupation);
        JPanel physicalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        physicalPanel.setBorder(null);
        physicalPanel.setOpaque(false);
        txtBloodGroup.setColumns(5);
        physicalPanel.add(new JLabel("Blood"));
        physicalPanel.add(txtBloodGroup);
        physicalPanel.add(new JLabel("Height"));
        physicalPanel.add(txtHeight);
        physicalPanel.add(new JLabel("cm"));
        physicalPanel.add(new JLabel("Weight"));
        physicalPanel.add(txtWeight);
        physicalPanel.add(new JLabel("kg"));

        y = addRow(panel, gbc, y, "Physical", physicalPanel);
        y = addSectionHeader(panel, gbc, y, "Medical History");

        y = addRow(panel, gbc, y, "Duration", txtDuration);
        y = addRow(panel, gbc, y, "Main Disease", txtMainDisease);
        y = addRow(panel, gbc, y, "Complications", txtComplications);
        y = addRow(panel, gbc, y, "Symptoms", txtSymptoms);

        // ===== Pain Points =====
        JPanel painPanel = new JPanel(new GridLayout(0, 2));
        painPanel.setBorder(null);
        painPanel.setOpaque(false);
        String[] names = {
            "Pan", "Gas", "GasI", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mu0",
            "Folic", "Thia", "B12", "Nia"
        };

        String[] scale = {"0", "1", "2", "3", "4"};
        y = addSectionHeader(panel, gbc, y, "Examination");
        for (int i = 0; i < names.length; i++) {
            painFields[i] = new JComboBox<>(scale);
            painPanel.add(new JLabel(names[i]));
            painPanel.add(painFields[i]);
        }

        left4th = new JComboBox<>(new String[] {"No", "Yes"});
        right4th = new JComboBox<>(new String[] {"No", "Yes"});

        painPanel.add(new JLabel("Left 4th"));
        painPanel.add(left4th);
        painPanel.add(new JLabel("Right 4th"));
        painPanel.add(right4th);

        y = addRow(panel, gbc, y, "Pain Points", painPanel);

        // ===== Vitals =====
        JPanel vitals = new JPanel(new FlowLayout(FlowLayout.LEFT));
        vitals.setBorder(null);
        vitals.setOpaque(false);
        vitals.add(new JLabel("BP"));
        vitals.add(txtBP);
        vitals.add(new JLabel("Pulse"));
        vitals.add(txtPulse);
        vitals.add(new JLabel("O2"));
        vitals.add(txtO2);
        vitals.add(new JLabel("Temp"));
        vitals.add(txtTemp);

        y = addRow(panel, gbc, y, "Vitals", vitals);

        y = addRow(panel, gbc, y, "Previous Treatment", txtPreviousTreatment);
        y = addRow(panel, gbc, y, "Medicines", txtMedicines);
        y = addRow(panel, gbc, y, "Detailed History", txtDetailedHistory);
        y = addRow(panel, gbc, y, "Examination", txtExamination);

        y = addRow(panel, gbc, y, "Report Analysis", txtReportAnalysis);
        y = addRow(panel, gbc, y, "Allergy", txtAllergy);
        y = addRow(panel, gbc, y, "Remarks", txtRemarks);
        enableEnterFocus(txtName);
        enableEnterFocus(txtMobile);
        enableEnterFocus(txtAge);

        enableEnterFocus(cmbGender);
        enableEnterFocus(cmbMarital);

        enableEnterFocus(txtAddress);

        enableEnterFocus(txtOccupation);
        enableEnterFocus(txtBloodGroup);

        enableEnterFocus(txtHeight);
        enableEnterFocus(txtWeight);

        enableEnterFocus(txtDuration);

        enableEnterFocus(txtMainDisease);
        enableEnterFocus(txtComplications);
        enableEnterFocus(txtSymptoms);

        enableEnterFocus(txtPreviousTreatment);
        enableEnterFocus(txtMedicines);

        enableEnterFocus(txtDetailedHistory);
        enableEnterFocus(txtExamination);

        enableEnterFocus(txtReportAnalysis);

        enableEnterFocus(txtAllergy);
        enableEnterFocus(txtRemarks);

        enableEnterFocus(txtBP);
        enableEnterFocus(txtPulse);
        enableEnterFocus(txtO2);
        enableEnterFocus(txtTemp);
        // ===== SAVE BUTTON =====
        JButton btnSave = new JButton("Save");

        btnSave.addActionListener(e -> saveData());
        getRootPane().setDefaultButton(btnSave);
        btnSave.setBackground(new Color(0, 120, 215));
        //btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(100, 35));

        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        savePanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        panel.add(savePanel, gbc);
        registerEscapeKey();
    }
    private int addSectionHeader(
            JPanel panel,
            GridBagConstraints gbc,
            int y,
            String title) {

        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0, 102, 204));

        // Increase height
        lbl.setPreferredSize(new Dimension(0, 35));

        // Optional: add internal padding
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(lbl, gbc);

        gbc.gridwidth = 1;

        return y + 1;
    }
    private void enableEnterFocus(Component component) {

        // JTextField + JComboBox
        if (component instanceof JTextField || component instanceof JComboBox) {

            if (component instanceof JTextField textField) {

                textField.addActionListener(e -> textField.transferFocus());
            }

            if (component instanceof JComboBox<?> comboBox) {

                comboBox.addActionListener(e -> comboBox.transferFocus());
            }
        }

        // JTextArea special handling
        if (component instanceof JTextArea area) {

            area.setFocusTraversalKeysEnabled(false);

            area.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                        e.consume();

                        area.transferFocus();
                    }
                }
            });
        }
    }

    // ================= SAVE =================
    private void saveData() {

        try {

            logger.info(
                    "Saving patient started name={} mobile={} userId={}",
                    txtName.getText(),
                    txtMobile.getText(),
                    userId);

            if (txtName.getText().trim().isEmpty()) {
                logger.warn("Save blocked: patient name empty");
                DialogUtil.warning(this, ErrorConstants.PATIENT_NAME_REQD);
                return;
            }

            Float height = txtHeight.getText().isEmpty() ? null : Float.parseFloat(txtHeight.getText());

            Float weight = txtWeight.getText().isEmpty() ? null : Float.parseFloat(txtWeight.getText());

            logger.debug("Parsed vitals height={} weight={} bp={}", height, weight, txtBP.getText());

            StringBuilder pain = new StringBuilder();

            for (JComboBox<String> field : painFields) {
                pain.append(field.getSelectedItem()).append(",");
            }

            pain.append("L4=").append(left4th.getSelectedItem()).append(",");

            pain.append("R4=").append(right4th.getSelectedItem());

            logger.debug("Pain points captured={}", pain);

            patientRepo.savePatient(
                    txtName.getText(),
                    txtMobile.getText(),
                    txtAge.getText().isEmpty() ? null : Integer.parseInt(txtAge.getText()),
                    (String) cmbGender.getSelectedItem(),
                    (String) cmbMarital.getSelectedItem(),
                    txtAddress.getText(),
                    txtOccupation.getText(),
                    txtBloodGroup.getText(),
                    height,
                    weight,
                    txtDuration.getText(),
                    txtMainDisease.getText(),
                    txtComplications.getText(),
                    txtSymptoms.getText(),
                    pain.toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    txtPreviousTreatment.getText(),
                    txtMedicines.getText(),
                    txtDetailedHistory.getText(),
                    txtExamination.getText(),
                    txtBP.getText(),
                    txtPulse.getText(),
                    txtO2.getText(),
                    txtTemp.getText(),
                    userId,
                    txtReportPath.getText(),
                    "",
                    txtReportAnalysis.getText(),
                    txtRemarks.getText(),
                    new java.sql.Timestamp(System.currentTimeMillis()));

            logger.info("Patient saved successfully name={} userId={}", txtName.getText(), userId);
            DialogUtil.info(this, MessageConstants.SAVED);
            dispose();

        } catch (NumberFormatException e) {
            logger.warn("Invalid numeric input while saving patient", e);
            DialogUtil.error(this, ErrorConstants.INVALID_AGE_HEIGHT_WEIGHT);
        } catch (SQLIntegrityConstraintViolationException e) {

            logger.warn(
                    "Duplicate patient mobile={} userId={}",
                    txtMobile.getText(),
                    userId);

            DialogUtil.warning(
                    this,ErrorConstants.DUPLICATE_MOBILE);
        }
        catch (Exception e) {

            logger.error(
                    "Patient save failed userId={}",
                    userId,
                    e);

            DialogUtil.error(
                    this,ErrorConstants.UNABLE_TO_SAVE_PATIENT);
        }
    }

    private JTextArea createArea() {
        JTextArea a = new JTextArea(2, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);

        a.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return a;
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {

        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;

        if (field instanceof JTextArea area) {
            JScrollPane sp = new JScrollPane(area);
            sp.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            panel.add(sp, gbc);
        } else if (field instanceof JPanel) {
            panel.add(field, gbc);   // NO BORDER for panels
        } else {
            ((JComponent) field).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            panel.add(field, gbc);
        }

        return y + 1;
    }

    private void registerEscapeKey() {

        getRootPane()
                .registerKeyboardAction(
                        e -> dispose(),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
