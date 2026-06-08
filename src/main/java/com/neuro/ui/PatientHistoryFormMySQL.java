/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.model.Patient;
import com.neuro.repo.PatientRepository;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.*;
import javax.swing.border.Border;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PatientHistoryFormMySQL extends JDialog {
    private static final Logger logger = LogManager.getLogger(PatientHistoryFormMySQL.class);

    private static final Color HEADER_COLOR = UiTheme.BRAND;
    private static final Color FORM_BG = UiTheme.BG_WHITE;
    private static final Border FIELD_BORDER =
            BorderFactory.createCompoundBorder(
                    UiTheme.BORDER,
                    BorderFactory.createEmptyBorder(2, 4, 2, 4));
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

    private int userId;

    private final PatientRepository patientRepo;

    public PatientHistoryFormMySQL(Window owner, int userId, AppContext context) {
        super(owner, Messages.get("patient.history.title"), ModalityType.APPLICATION_MODAL);
        this.userId = userId;
        this.patientRepo = context.patientRepo();
        logger.info("Opening Patient History Form for userId={}", userId);
        setSize(850, 601); // 850 / 1.413 ≈ 601 (kept smaller than the Doctor Dashboard, which is the primary window)
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                boolean confirmed = UiTheme.showConfirm(
                        PatientHistoryFormMySQL.this,
                        Messages.get("patient.history.close.title"),
                        Messages.get("patient.history.close.message"));
                if (confirmed) {
                    logger.info("User confirmed close of patient form");
                    dispose();
                } else {
                    logger.debug("User cancelled close of patient form");
                }
            }
        });
        setLayout(new BorderLayout());
        getContentPane().setBackground(FORM_BG);
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
        // Apply styling to every input.
        styleAllInputs();
        // ===== Tab 1: Basic Information =====
        JPanel basicPanel = whitePanel();
        GridBagConstraints bgbc = newGbc();
        int y = 0;
        y = addSectionHeader(basicPanel, bgbc, y, "Basic Information");
        y = addRow(basicPanel, bgbc, y, "Name", txtName);
        y = addRow(basicPanel, bgbc, y, "Mobile", txtMobile);
        // Gender + Marital share one inline row; each combo sizes to its content (not stretched).
        y = addInlineRow(
                basicPanel,
                bgbc,
                y,
                new JLabel("Gender"), cmbGender,
                new JLabel("Marital"), cmbMarital);
        // Single combined row: Age | Weight kg | Height cm — placed directly on basicPanel
        y = addInlineRow(
                basicPanel,
                bgbc,
                y,
                new JLabel("Age"), txtAge,
                new JLabel("Weight"), txtWeight, new JLabel("kg"),
                new JLabel("Height"), txtHeight, new JLabel("cm"));
        y = addRow(basicPanel, bgbc, y, "Address", txtAddress);
        y = addRow(basicPanel, bgbc, y, "Occupation", txtOccupation);
        y = addRow(basicPanel, bgbc, y, "Blood Group", txtBloodGroup);
        y = addRow(basicPanel, bgbc, y, "Duration", txtDuration);
        addVerticalFiller(basicPanel, bgbc, y);
        // ===== Tab 2: Patient History =====
        JPanel historyPanel = whitePanel();
        GridBagConstraints hgbc = newGbc();
        int h = 0;
        h = addSectionHeader(historyPanel, hgbc, h, "Patient History");
        h = addRow(historyPanel, hgbc, h, "Main Disease", txtMainDisease);
        h = addRow(historyPanel, hgbc, h, "Complications", txtComplications);
        h = addRow(historyPanel, hgbc, h, "Symptoms", txtSymptoms);
        // Pain points: 4-column GridBagLayout. label\u2192combo gap = 3px, pair\u2192pair gap = 12px.
        JPanel painPanel = new JPanel(new GridBagLayout());
        painPanel.setBackground(FORM_BG);
        String[] names = {
            "Pan", "Gas", "GasI", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mu0",
            "Folic", "Thia", "B12", "Nia"
        };
        String[] scale = {"0", "1", "2", "3", "4"};
        left4th = new JComboBox<>(new String[] {"No", "Yes"});
        right4th = new JComboBox<>(new String[] {"No", "Yes"});
        JLabel[] painLabels = new JLabel[names.length + 2];
        JComboBox<?>[] painCombos = new JComboBox<?>[names.length + 2];
        for (int i = 0; i < names.length; i++) {
            painFields[i] = new JComboBox<>(scale);
            painLabels[i] = new JLabel(names[i]);
            painCombos[i] = painFields[i];
        }
        painLabels[names.length] = new JLabel("Left 4th");
        painCombos[names.length] = left4th;
        painLabels[names.length + 1] = new JLabel("Right 4th");
        painCombos[names.length + 1] = right4th;
        GridBagConstraints pg = new GridBagConstraints();
        pg.anchor = GridBagConstraints.WEST;
        pg.fill = GridBagConstraints.NONE;
        for (int i = 0; i < painLabels.length; i++) {
            int row = i / 2;
            int pair = i % 2; // 0 => left pair (cols 0,1), 1 => right pair (cols 2,3)
            int colLabel = pair * 2;
            int colCombo = pair * 2 + 1;
            pg.gridx = colLabel;
            pg.gridy = row;
            pg.insets = new Insets(2, 0, 2, 3); // 3px to its combo
            painPanel.add(painLabels[i], pg);
            pg.gridx = colCombo;
            pg.gridy = row;
            pg.insets = new Insets(2, 0, 2, pair == 0 ? 12 : 0); // 12px to next pair, 0 at row end
            painPanel.add(painCombos[i], pg);
        }
        // Trailing filler in column 4 to absorb slack and keep cells flush-left.
        pg.gridx = 4;
        pg.gridy = 0;
        pg.gridwidth = 1;
        pg.gridheight = (painLabels.length + 1) / 2;
        pg.weightx = 1.0;
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.insets = new Insets(0, 0, 0, 0);
        painPanel.add(Box.createHorizontalGlue(), pg);
        h = addRow(historyPanel, hgbc, h, "Pain Points", painPanel);
        JPanel vitals = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        vitals.setBackground(FORM_BG);
        // 3px left padding on each label so they sit slightly inset from the previous field.
        Border vitalLabelPad = BorderFactory.createEmptyBorder(0, 3, 0, 0);
        JLabel lblBP = new JLabel("BP");
        lblBP.setBorder(vitalLabelPad);
        JLabel lblPulse = new JLabel("Pulse");
        lblPulse.setBorder(vitalLabelPad);
        JLabel lblO2 = new JLabel("O2");
        lblO2.setBorder(vitalLabelPad);
        JLabel lblTemp = new JLabel("Temp");
        lblTemp.setBorder(vitalLabelPad);
        vitals.add(lblBP);
        vitals.add(txtBP);
        vitals.add(lblPulse);
        vitals.add(txtPulse);
        vitals.add(lblO2);
        vitals.add(txtO2);
        vitals.add(lblTemp);
        vitals.add(txtTemp);
        h = addRow(historyPanel, hgbc, h, "Vitals", vitals);
        h = addRow(historyPanel, hgbc, h, "Previous Treatment", txtPreviousTreatment);
        h = addRow(historyPanel, hgbc, h, "Medicines", txtMedicines);
        h = addRow(historyPanel, hgbc, h, "Detailed History", txtDetailedHistory);
        h = addRow(historyPanel, hgbc, h, "Examination", txtExamination);
        h = addRow(historyPanel, hgbc, h, "Report Analysis", txtReportAnalysis);
        h = addRow(historyPanel, hgbc, h, "Allergy", txtAllergy);
        h = addRow(historyPanel, hgbc, h, "Remarks", txtRemarks);
        addVerticalFiller(historyPanel, hgbc, h);
        // ===== Tabbed pane (left-aligned tabs) =====
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        tabs.setBackground(FORM_BG);
        tabs.addTab("Basic Information", whiteScrollPane(basicPanel));
        tabs.addTab("Patient History", whiteScrollPane(historyPanel));
        add(tabs, BorderLayout.CENTER);
        // ===== Enter-to-next-focus =====
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
        btnSave.setMnemonic(KeyEvent.VK_S);
        btnSave.addActionListener(e -> saveData());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.setBackground(FORM_BG);
        south.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 16));
        south.add(btnSave);
        add(south, BorderLayout.SOUTH);
        // ESC routes through windowClosing so the existing close-confirmation prompt fires.
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeForm");
        root.getActionMap().put("closeForm", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispatchEvent(new WindowEvent(PatientHistoryFormMySQL.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    /**
     * Panel that fills the scroll viewport horizontally (so section-header underlines and other
     * full-width components reach the right edge) while still scrolling vertically.
     */
    private static class FormPanel extends JPanel implements Scrollable {
        FormPanel() {
            super(new GridBagLayout());
            setBackground(FORM_BG);
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static JPanel whitePanel() {
        return new FormPanel();
    }

    private static JScrollPane whiteScrollPane(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.getViewport().setBackground(FORM_BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void styleAllInputs() {
        JTextField[] fields = {
            txtName, txtMobile, txtAge, txtOccupation, txtBloodGroup,
            txtHeight, txtWeight, txtDuration, txtBP, txtPulse, txtO2, txtTemp, txtReportPath
        };
        for (JTextField f : fields) {
            f.setBorder(FIELD_BORDER);
            f.setBackground(FORM_BG);
        }
        JTextArea[] areas = {
            txtAddress, txtMainDisease, txtComplications, txtSymptoms,
            txtPreviousTreatment, txtMedicines, txtDetailedHistory,
            txtExamination, txtReportAnalysis, txtAllergy, txtRemarks
        };
        for (JTextArea a : areas) {
            a.setBackground(FORM_BG);
        }
    }

    private static GridBagConstraints newGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private int addSectionHeader(JPanel panel, GridBagConstraints gbc, int y, String title) {
        JLabel header = new JLabel(title);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setForeground(HEADER_COLOR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, HEADER_COLOR),
                BorderFactory.createEmptyBorder(2, 0, 6, 0)));
        int oldGridWidth = gbc.gridwidth;
        double oldWeightX = gbc.weightx;
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(header, gbc);
        gbc.gridwidth = oldGridWidth;
        gbc.weightx = oldWeightX;
        return y + 1;
    }

    /**
     * Adds an inline row of components directly onto {@code panel}, each in its own grid column.
     * The final cell absorbs slack so everything stays flush-left. The leftmost component lands at
     * {@code gridx=0}, aligning with the label column of regular rows.
     */
    private static int addInlineRow(JPanel panel, GridBagConstraints gbc, int y, Component... cells) {
        int oldGridWidth = gbc.gridwidth;
        double oldWeightX = gbc.weightx;
        int oldFill = gbc.fill;
        int oldAnchor = gbc.anchor;
        for (int i = 0; i < cells.length; i++) {
            gbc.gridx = i;
            gbc.gridy = y;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(cells[i], gbc);
        }
        // Trailing filler absorbs remaining horizontal space.
        gbc.gridx = cells.length;
        gbc.gridy = y;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalGlue(), gbc);
        gbc.gridwidth = oldGridWidth;
        gbc.weightx = oldWeightX;
        gbc.fill = oldFill;
        gbc.anchor = oldAnchor;
        return y + 1;
    }

    private static void addVerticalFiller(JPanel panel, GridBagConstraints gbc, int y) {
        GridBagConstraints filler = (GridBagConstraints) gbc.clone();
        filler.gridx = 0;
        filler.gridy = y;
        filler.gridwidth = 2;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), filler);
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
                UiTheme.showInfo(this,
                        Messages.get("common.validation.title"),
                        Messages.get("patient.history.validation.name"));
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
            Patient patient = new Patient();
            patient.setName(txtName.getText());
            patient.setMobile(txtMobile.getText());
            patient.setAge(txtAge.getText().isEmpty() ? null : Integer.parseInt(txtAge.getText()));
            patient.setGender((String) cmbGender.getSelectedItem());
            patient.setMaritalStatus((String) cmbMarital.getSelectedItem());
            patient.setAddress(txtAddress.getText());
            patient.setOccupation(txtOccupation.getText());
            patient.setBloodGroup(txtBloodGroup.getText());
            patient.setHeight(height);
            patient.setWeight(weight);
            patient.setSufferingDuration(txtDuration.getText());
            patient.setMainDisease(txtMainDisease.getText());
            patient.setComplications(txtComplications.getText());
            patient.setSymptoms(txtSymptoms.getText());
            patient.setPainPoints(pain.toString());
            patient.setPreviousTreatment(txtPreviousTreatment.getText());
            patient.setMedicines(txtMedicines.getText());
            patient.setDetailedHistory(txtDetailedHistory.getText());
            patient.setExamination(txtExamination.getText());
            patient.setBp(txtBP.getText());
            patient.setPulse(txtPulse.getText());
            patient.setO2(txtO2.getText());
            patient.setTemperature(txtTemp.getText());
            patient.setUserId(userId);
            patient.setReports(txtReportPath.getText());
            patient.setPatientStory(txtReportAnalysis.getText());
            patient.setRemarks(txtRemarks.getText());
            patient.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            patientRepo.savePatient(patient);
            logger.info("Patient saved successfully name={} userId={}", txtName.getText(), userId);
            UiTheme.showInfo(this, Messages.get("common.saved.title"), Messages.get("patient.history.success"));
            dispose();
        } catch (NumberFormatException e) {
            logger.warn("Invalid numeric input while saving patient", e);
            UiTheme.showInfo(this,
                    Messages.get("common.validation.title"),
                    Messages.get("patient.history.validation.numeric"));
        } catch (SQLIntegrityConstraintViolationException e) {
                logger.warn("Duplicate mobile number while saving patient", e);
                UiTheme.showInfo(
                        this,
                        "Duplicate Mobile Number",
                        "A patient with this mobile number already exists.");

        } catch (Exception e) {
            logger.error("Patient save failed userId={}", userId, e);
            UiTheme.showInfo(this, Messages.get("common.error.title"), e.getMessage());
        }
    }

    private JTextArea createArea() {
        JTextArea a = new JTextArea(3, 20);
        a.setLineWrap(true);
        return a;
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Component wrapped = field;
        if (field instanceof JTextArea area) {
            JScrollPane sp = new JScrollPane(area);
            sp.getViewport().setBackground(FORM_BG);
            sp.setBorder(UiTheme.BORDER);
            // Force a 56px-tall row for every text-area field. Width is set to 0 because
            // fill=HORIZONTAL + weightx=1.0 makes the GridBagLayout stretch it across the column.
            sp.setPreferredSize(new Dimension(0, 56));
            sp.setMinimumSize(new Dimension(0, 56));
            wrapped = sp;
        }
        panel.add(wrapped, gbc);
        return y + 1;
    }
}
