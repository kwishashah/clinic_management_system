/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.repo.SessionRepository;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.event.KeyEvent;
import com.neuro.constants.*;
public class SessionFormDialog extends JDialog {

    private static final Logger logger = LogManager.getLogger(SessionFormDialog.class);

    private final int patientId;
    private final Integer sessionId;
    private final PatientDetailsFrame parentFrame;

    private JTextField txtSessionNumber;
    private JTextField txtDate;

    private JTextArea txtTreatment;
    private JTextArea txtSummary;

    @SuppressWarnings("unchecked")
    private JComboBox<String>[] beforeFields = new JComboBox[18];

    @SuppressWarnings("unchecked")
    private JComboBox<String>[] afterFields = new JComboBox[18];

    private JComboBox<String> left4thBefore;
    private JComboBox<String> left4thAfter;

    private JComboBox<String> right4thBefore;
    private JComboBox<String> right4thAfter;
    private static final String[] PAIN_NAMES = {
        "Pan", "Gas", "GasI", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mu0", "Folic",
        "Thia", "B12", "Nia"
    };

    private static final String[] SCALE = {"0", "1", "2", "3", "4"};

    private final SessionRepository sessionRepo;

    public SessionFormDialog(PatientDetailsFrame parent, int patientId, Integer sessionId, AppContext context) {
        super(parent, true);
        this.sessionRepo = context.sessionRepo();

        this.parentFrame = parent;
        this.patientId = patientId;
        this.sessionId = sessionId;

        logger.info("Opening SessionFormDialog patientId={} sessionId={}", patientId, sessionId);

        setTitle(sessionId == null ? "Add Session" : "Update Session");

        setSize(750, 650);
        setResizable(false);
        setLocationRelativeTo(parent);

        initComponents();

        if (sessionId == null) {

            int next = sessionRepo.getNextSessionNumber(patientId);

            logger.info("Generated next session number {} for patientId={}", next, patientId);

            txtSessionNumber.setText(String.valueOf(next));

            txtDate.setText(java.time.LocalDate.now().toString());

        } else {
            loadSessionData();
        }

        registerEscapeKey();
    }

    private void initComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));

        top.add(new JLabel("Session Number:"));

        txtSessionNumber = new JTextField();

        top.add(txtSessionNumber);

        top.add(new JLabel("Date (yyyy-MM-dd):"));

        txtDate = new JTextField();

        top.add(txtDate);

        top.add(new JLabel("Treatment:"));

        txtTreatment = new JTextArea(2, 20);

        top.add(new JScrollPane(txtTreatment));

        mainPanel.add(top, BorderLayout.NORTH);

        JPanel painPanel = new JPanel(new GridLayout(0, 3, 10, 10));

        painPanel.add(new JLabel("Pain"));
        painPanel.add(new JLabel("Before"));
        painPanel.add(new JLabel("After"));
        left4thBefore = new JComboBox<>(SCALE);
        left4thAfter = new JComboBox<>(SCALE);

        right4thBefore = new JComboBox<>(SCALE);
        right4thAfter = new JComboBox<>(SCALE);

        for (int i = 0; i < PAIN_NAMES.length; i++) {

            beforeFields[i] = new JComboBox<>(SCALE);

            afterFields[i] = new JComboBox<>(SCALE);

            painPanel.add(new JLabel(PAIN_NAMES[i]));

            painPanel.add(beforeFields[i]);

            painPanel.add(afterFields[i]);
        }
        painPanel.add(new JLabel("Left 4th"));
        painPanel.add(left4thBefore);
        painPanel.add(left4thAfter);

        painPanel.add(new JLabel("Right 4th"));
        painPanel.add(right4thBefore);
        painPanel.add(right4thAfter);
        mainPanel.add(new JScrollPane(painPanel), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));

        txtSummary = new JTextArea(4, 20);

        bottom.add(new JLabel("Session Summary:"), BorderLayout.NORTH);

        bottom.add(new JScrollPane(txtSummary), BorderLayout.CENTER);

        JButton btnSave = new JButton(sessionId == null ? "Add" : "Update");

        btnSave.addActionListener(this::handleSave);

        bottom.add(btnSave, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSave);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadSessionData() {

        try {

            logger.info("Loading existing session data sessionId={} patientId={}", sessionId, patientId);

            var sessions = sessionRepo.getSessionsByPatient(patientId);

            for (var row : sessions) {

                if ((int) row.get(0) == sessionId) {

                    logger.info("Session found sessionId={}", sessionId);

                    txtSessionNumber.setText(String.valueOf(row.get(1)));

                    txtDate.setText(String.valueOf(row.get(2)));

                    txtTreatment.setText(String.valueOf(row.get(3)));

                    txtSummary.setText(String.valueOf(row.get(6)));

                    String painData = (String) row.get(4);

                    if (painData != null && !painData.isEmpty()) {

                        String[] pairs = painData.split(",");

                        for (int i = 0; i < pairs.length && i < beforeFields.length; i++) {

                            if (pairs[i].contains("->")) {

                                String[] vals = pairs[i].split("->");

                                if (vals.length == 2) {
                                    beforeFields[i].setSelectedItem(vals[0]);

                                    afterFields[i].setSelectedItem(vals[1]);
                                }
                            }
                        }

                        logger.debug("Pain values loaded for sessionId={}", sessionId);

                        for (String pair : pairs) {

                            if (pair.startsWith("L4=")) {

                                String value = pair.replace("L4=", "");

                                String[] vals = value.split("->");

                                if (vals.length == 2) {

                                    left4thBefore.setSelectedItem(vals[0]);
                                    left4thAfter.setSelectedItem(vals[1]);
                                }
                            } else if (pair.startsWith("R4=")) {

                                String value = pair.replace("R4=", "");

                                String[] vals = value.split("->");

                                if (vals.length == 2) {

                                    right4thBefore.setSelectedItem(vals[0]);
                                    right4thAfter.setSelectedItem(vals[1]);
                                }
                            }
                        }
                    }

                    break;
                }
            }

        } catch (Exception e) {

            logger.error("Error loading session sessionId={} patientId={}", sessionId, patientId, e);
            DialogUtil.error(this, ErrorConstants.UNABLE_TO_LOAD_SESSION);
        }
    }

    private String buildPainString() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < beforeFields.length; i++) {

            if (i > 0) {
                sb.append(",");
            }

            sb.append(beforeFields[i].getSelectedItem()).append("->").append(afterFields[i].getSelectedItem());
        }

        logger.debug("Built pain data {}", sb.toString());
        sb.append(",L4=").append(left4thBefore.getSelectedItem()).append("->").append(left4thAfter.getSelectedItem());

        sb.append(",R4=").append(right4thBefore.getSelectedItem()).append("->").append(right4thAfter.getSelectedItem());

        return sb.toString();
    }

    private void handleSave(ActionEvent e) {

        try {

            logger.info("Saving session patientId={} sessionId={}", patientId, sessionId);

            if (txtSessionNumber.getText().trim().isEmpty()) {
                throw new Exception("Session number required");
            }

            if (txtDate.getText().trim().isEmpty()) {
                throw new Exception("Date required");
            }

            int sessionNo = Integer.parseInt(txtSessionNumber.getText().trim());

            Date sqlDate = Date.valueOf(txtDate.getText().trim());

            String treatment = txtTreatment.getText().trim();

            String summary = txtSummary.getText().trim();

            String pain = buildPainString();

            if (sessionId == null) {

                logger.info("Adding new session {} for patientId={}", sessionNo, patientId);

                sessionRepo.addSession(patientId, sessionNo, sqlDate, treatment, pain, "", summary);

                logger.info("Session added successfully patientId={} sessionNo={}", patientId, sessionNo);
                DialogUtil.info(this, MessageConstants.SESSION_ADDED);

            } else {

                logger.info("Updating sessionId={} patientId={}", sessionId, patientId);
                sessionRepo.updateSession(sessionId, sessionNo, sqlDate, treatment, pain, "", summary);
                logger.info("Session updated successfully sessionId={}", sessionId);
                DialogUtil.info(this, MessageConstants.SESSION_UPDATED);
            }
            logger.info("Refreshing sessions grid for patientId={}", patientId);
            parentFrame.loadSessions();
            dispose();
            }
            catch (Exception ex) {
            logger.error("Session save failed patientId={} sessionId={}", patientId, sessionId, ex);
            DialogUtil.error(this, ErrorConstants.UNABLE_TO_SAVE_SESSION);
        }
    }
    private void registerEscapeKey() {
        getRootPane()
                .registerKeyboardAction(
                        e -> dispose(),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
