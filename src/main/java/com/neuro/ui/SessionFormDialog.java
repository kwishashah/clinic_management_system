/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.model.Session;
import com.neuro.repo.SessionRepository;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionFormDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(SessionFormDialog.class);

    /** Number of pain-scale rows shown in the matrix (one per entry in {@link #PAIN_NAMES}). */
    private static final int PAIN_FIELD_COUNT = 18;

    /** Discrete pain-scale values (low to high) shown in each combo box. */
    private static final String[] SCALE = {"0", "1", "2", "3", "4"};

    /** Labels for each pain row in the matrix, in display order. */
    private static final String[] PAIN_NAMES = {
        "Pan", "Gas", "GasI", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mu0", "Folic",
        "Thia", "B12", "Nia"
    };

    // ---- Encoding tokens for the pain string persisted on Session.painData. ----
    /** Separator between consecutive {@code before->after} pairs in the pain string. */
    private static final String PAIR_SEPARATOR = ",";
    /** Delimiter between the before and after values within a single pair. */
    private static final String BEFORE_AFTER_DELIM = "->";
    /** Prefix marking the left fourth-extremity entry. */
    private static final String LEFT_4TH_PREFIX = "L4=";
    /** Prefix marking the right fourth-extremity entry. */
    private static final String RIGHT_4TH_PREFIX = "R4=";

    private final int patientId;
    private final Integer sessionId;
    private final PatientDetailsFrame parentFrame;
    private final SessionRepository sessionRepo;

    private JTextField txtSessionNumber;
    private JTextField txtDate;

    private JTextArea txtTreatment;
    private JTextArea txtSummary;

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] beforeFields = new JComboBox[PAIN_FIELD_COUNT];

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] afterFields = new JComboBox[PAIN_FIELD_COUNT];

    private JComboBox<String> left4thBefore;
    private JComboBox<String> left4thAfter;

    private JComboBox<String> right4thBefore;
    private JComboBox<String> right4thAfter;

    public SessionFormDialog(PatientDetailsFrame parent, int patientId, Integer sessionId, AppContext context) {
        super(parent, Messages.get(titleKey(sessionId)), ModalityType.APPLICATION_MODAL);
        this.sessionRepo = context.sessionRepo();
        this.parentFrame = parent;
        this.patientId = patientId;
        this.sessionId = sessionId;
        logger.info("Opening SessionFormDialog patientId={} sessionId={}", patientId, sessionId);
        setSize(850, 602); // ~1.41 width:height ratio (application standard)
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        if (sessionId == null) {
            int next = sessionRepo.getNextSessionNumber(patientId);
            logger.info("Generated next session number {} for patientId={}", next, patientId);
            txtSessionNumber.setText(String.valueOf(next));
            txtDate.setText(java.time.LocalDate.now().toString());
        } else {
            loadSessionData();
        }
    }

    private void initComponents() {
        Container content = getContentPane();
        content.setBackground(UiTheme.BG_WHITE);
        content.setLayout(new GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Rows 0–2: standard brand title + separator + subtitle header.
        UiTheme.addDialogHeader(content, gbc, 0,
                Messages.get(titleKey(sessionId)),
                Messages.get("session.subtitle"));
        // Row 3: Session Number label (col 0) + field (col 1) + Date label (col 2) + field (col 3)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel("Session Number:"), gbc);
        txtSessionNumber = new JTextField();
        UiTheme.styleField(txtSessionNumber);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        content.add(txtSessionNumber, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        content.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        txtDate = new JTextField();
        UiTheme.styleField(txtDate);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        content.add(txtDate, gbc);
        // Row 4: Treatment label (col 0) + scrollable text area (cols 1-3)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel("Treatment:"), gbc);
        txtTreatment = new JTextArea(2, 20);
        txtTreatment.setLineWrap(true);
        txtTreatment.setWrapStyleWord(true);
        UiTheme.styleField(txtTreatment);
        JScrollPane treatmentScroll = new JScrollPane(txtTreatment);
        treatmentScroll.setBorder(UiTheme.BORDER);
        treatmentScroll.setPreferredSize(new Dimension(0, 56));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(treatmentScroll, gbc);
        // Row 5: pain matrix (Pain | Before | After) — absorbs vertical slack via scrollpane
        JPanel painPanel = new JPanel(new GridLayout(0, 3, 6, 6));
        painPanel.setBackground(UiTheme.BG_WHITE);
        // Branded header row (matches DoctorDashboard/PatientDetailsFrame table header).
        painPanel.add(UiTheme.brandHeaderLabel("Pain"));
        painPanel.add(UiTheme.brandHeaderLabel("Before"));
        painPanel.add(UiTheme.brandHeaderLabel("After"));
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
        // Defensive: keep the matrix and the field arrays in lockstep.
        assert PAIN_NAMES.length == PAIN_FIELD_COUNT : "PAIN_NAMES length must match PAIN_FIELD_COUNT";
        painPanel.add(new JLabel("Left 4th"));
        painPanel.add(left4thBefore);
        painPanel.add(left4thAfter);
        painPanel.add(new JLabel("Right 4th"));
        painPanel.add(right4thBefore);
        painPanel.add(right4thAfter);
        JScrollPane painScroll = new JScrollPane(painPanel);
        painScroll.setBorder(UiTheme.BORDER);
        painScroll.getViewport().setBackground(UiTheme.BG_WHITE);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(painScroll, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Row 6: Session Summary label (col 0) + scrollable text area (cols 1-3)
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel("Session Summary:"), gbc);
        txtSummary = new JTextArea(3, 20);
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        UiTheme.styleField(txtSummary);
        JScrollPane summaryScroll = new JScrollPane(txtSummary);
        summaryScroll.setBorder(UiTheme.BORDER);
        summaryScroll.setPreferredSize(new Dimension(0, 56));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(summaryScroll, gbc);
        // Row 7: bottom brand separator
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        content.add(UiTheme.newDividerSeparator(), gbc);
        // Row 8: Cancel + Save buttons right-aligned
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setMnemonic(KeyEvent.VK_C);
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = new JButton(sessionId == null ? "Add" : "Update");
        btnSave.setMnemonic(sessionId == null ? KeyEvent.VK_A : KeyEvent.VK_U);
        UiTheme.asPrimary(btnSave);
        btnSave.addActionListener(this::handleSave);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(UiTheme.BG_WHITE);
        buttonRow.add(btnCancel);
        buttonRow.add(btnSave);
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        content.add(buttonRow, gbc);
        getRootPane().setDefaultButton(btnSave);
        // ESC dismisses the dialog without saving.
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSession");
        root.getActionMap().put("closeSession", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("SessionFormDialog dismissed via ESC patientId={}", patientId);
                dispose();
            }
        });
    }

    private void loadSessionData() {
        try {
            logger.info("Loading existing session data sessionId={} patientId={}", sessionId, patientId);
            sessionRepo.getSessionsByPatient(patientId).stream()
                    .filter(s -> s.sessionId() == sessionId)
                    .findFirst()
                    .ifPresent(this::applySession);
        } catch (RuntimeException e) {
            logger.error("Error loading session sessionId={} patientId={}", sessionId, patientId, e);
            UiTheme.showInfo(this, Messages.get("common.error.title"), Messages.get("session.error.load"));
        }
    }

    /** Populates the form fields from a single session returned by the repository. */
    private void applySession(Session s) {
        logger.info("Session found sessionId={}", sessionId);
        txtSessionNumber.setText(String.valueOf(s.sessionNumber()));
        txtDate.setText(s.sessionDate() == null ? "" : s.sessionDate().toString());
        txtTreatment.setText(s.treatment() == null ? "" : s.treatment());
        txtSummary.setText(s.summary() == null ? "" : s.summary());
        applyPainData(s.painBefore());
    }

    /** Decodes the persisted pain string into the matrix combos and the L4/R4 entries. */
    private void applyPainData(String painData) {
        if (painData == null || painData.isEmpty()) {
            return;
        }
        String[] pairs = painData.split(PAIR_SEPARATOR);
        for (int i = 0; i < pairs.length && i < beforeFields.length; i++) {
            applyIndexedPair(pairs[i], i);
        }
        for (String pair : pairs) {
            if (pair.startsWith(LEFT_4TH_PREFIX)) {
                applyExtremityPair(pair.substring(LEFT_4TH_PREFIX.length()), left4thBefore, left4thAfter);
            } else if (pair.startsWith(RIGHT_4TH_PREFIX)) {
                applyExtremityPair(pair.substring(RIGHT_4TH_PREFIX.length()), right4thBefore, right4thAfter);
            }
        }
        logger.debug("Pain values loaded for sessionId={}", sessionId);
    }

    /** Applies a {@code before->after} pair to the i-th row of the pain matrix. */
    private void applyIndexedPair(String pair, int index) {
        if (!pair.contains(BEFORE_AFTER_DELIM)) {
            return;
        }
        String[] vals = pair.split(BEFORE_AFTER_DELIM);
        if (vals.length == 2) {
            beforeFields[index].setSelectedItem(vals[0]);
            afterFields[index].setSelectedItem(vals[1]);
        }
    }

    /** Applies a {@code before->after} pair (already stripped of its L4=/R4= prefix) to the given combos. */
    private void applyExtremityPair(String value, JComboBox<String> before, JComboBox<String> after) {
        String[] vals = value.split(BEFORE_AFTER_DELIM);
        if (vals.length == 2) {
            before.setSelectedItem(vals[0]);
            after.setSelectedItem(vals[1]);
        }
    }

    private String buildPainString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < beforeFields.length; i++) {
            if (i > 0) {
                sb.append(PAIR_SEPARATOR);
            }
            sb.append(beforeFields[i].getSelectedItem())
                    .append(BEFORE_AFTER_DELIM)
                    .append(afterFields[i].getSelectedItem());
        }
        // Pass `sb` (not sb.toString()) so the conversion happens only when DEBUG is enabled.
        logger.debug("Built pain data {}", sb);
        appendExtremity(sb, LEFT_4TH_PREFIX, left4thBefore, left4thAfter);
        appendExtremity(sb, RIGHT_4TH_PREFIX, right4thBefore, right4thAfter);
        return sb.toString();
    }

    /** Appends {@code ,<prefix><before>-><after>} to the pain string builder. */
    private static void appendExtremity(
            StringBuilder sb, String prefix, JComboBox<String> before, JComboBox<String> after) {
        sb.append(PAIR_SEPARATOR)
                .append(prefix)
                .append(before.getSelectedItem())
                .append(BEFORE_AFTER_DELIM)
                .append(after.getSelectedItem());
    }

    private void handleSave(ActionEvent e) {
        try {
            logger.info("Saving session patientId={} sessionId={}", patientId, sessionId);
            int sessionNo = parseSessionNumber();
            LocalDate sessionDate = parseSessionDate();
            String treatment = txtTreatment.getText().trim();
            String summary = txtSummary.getText().trim();
            String pain = buildPainString();
            persistSession(sessionNo, sessionDate, treatment, summary, pain);
            logger.info("Refreshing sessions grid for patientId={}", patientId);
            parentFrame.loadSessions();
            dispose();
        } catch (IllegalArgumentException ex) {
            // Validation / parsing problem: typically caused by the user's input.
            logger.warn("Session save validation failed patientId={} sessionId={}: {}",
                    patientId, sessionId, ex.getMessage());
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("session.error.generic", ex.getMessage()));
        } catch (RuntimeException ex) {
            // Unexpected repository / runtime failure.
            logger.error("Session save failed patientId={} sessionId={}", patientId, sessionId, ex);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("session.error.generic", ex.getMessage()));
        }
    }

    /**
     * @throws IllegalArgumentException if the session-number field is empty or not a valid integer
     */
    private int parseSessionNumber() {
        String raw = txtSessionNumber.getText().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(Messages.get("session.validation.number"));
        }
        return Integer.parseInt(raw);
    }

    /**
     * @throws IllegalArgumentException if the date field is empty or not in {@code yyyy-MM-dd} format
     */
    private LocalDate parseSessionDate() {
        String raw = txtDate.getText().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(Messages.get("session.validation.date"));
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            // Normalize parse failures so handleSave's IllegalArgumentException branch catches them.
            throw new IllegalArgumentException(Messages.get("session.validation.date"), ex);
        }
    }

    /** Inserts a new session or updates the existing one, depending on {@link #sessionId}. */
    private void persistSession(int sessionNo, LocalDate sessionDate, String treatment, String summary, String pain) {
        if (sessionId == null) {
            logger.info("Adding new session {} for patientId={}", sessionNo, patientId);
            sessionRepo.addSession(
                    patientId, Session.forNew(sessionNo, sessionDate, treatment, pain, "", summary));
            logger.info("Session added successfully patientId={} sessionNo={}", patientId, sessionNo);
            UiTheme.showInfo(this, Messages.get("common.saved.title"), Messages.get("session.success.added"));
        } else {
            logger.info("Updating sessionId={} patientId={}", sessionId, patientId);
            sessionRepo.updateSession(
                    patientId, new Session(sessionId, sessionNo, sessionDate, treatment, pain, "", summary));
            logger.info("Session updated successfully sessionId={}", sessionId);
            UiTheme.showInfo(this, Messages.get("common.saved.title"), Messages.get("session.success.updated"));
        }
    }

    /** Returns the i18n key for the dialog title given the add/update mode. */
    private static String titleKey(Integer sessionId) {
        return sessionId == null ? "session.title.add" : "session.title.update";
    }
}
