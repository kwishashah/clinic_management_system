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
    private JTextField txtName, txtMobile, txtOccupation,txtAge,txtWeight;
    private JTextField txtHeight, txtDuration;
    private JTextField txtBP, txtPulse, txtO2, txtTemp;
   // private JSpinner spnAge, spnWeight;
    private JComboBox<String> cmbBloodGroup;
    /** Standard ABO + Rh blood groups; blank first entry means "not selected". */
    private static final String[] BLOOD_GROUPS = {"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
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
        txtMobile = new JTextField(15);
        txtAge = new JTextField(5);
        cmbGender = new JComboBox<>(new String[] {"Male", "Female", "Other"});
        cmbMarital = new JComboBox<>(new String[] {"Single", "Married"});
        txtAddress = createArea();
        txtOccupation = new JTextField(20);
        cmbBloodGroup = new JComboBox<>(BLOOD_GROUPS);
        txtHeight = new JTextField(5);
        txtWeight = new JTextField(5);
        //spnWeight = createBoundedSpinner(1, 200);
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
        UiTheme.attachNumericValidation(txtMobile);
        UiTheme.attachNumericValidation(txtAge);
        UiTheme.attachNumericValidation(txtHeight);
        UiTheme.attachNumericValidation(txtWeight);
        UiTheme.attachNumericValidation(txtO2);
        UiTheme.attachNumericValidation(txtPulse);
        UiTheme.attachNumericValidation(txtTemp);
       // UiTheme.attachNumericValidation(txtBP);
        // Apply styling to every input.
        styleAllInputs();
        // ===== Tab content panels =====
        JPanel basicPanel = buildBasicPanel();
        JPanel historyPanel = buildHistoryPanel();
        // ===== IntelliJ-style tab bar + CardLayout =====
        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        cardPanel.setBackground(FORM_BG);
        cardPanel.add(whiteScrollPane(basicPanel), "basic");
        cardPanel.add(whiteScrollPane(historyPanel), "history");

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(FORM_BG);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.DIVIDER));

        ButtonGroup tabGroup = new ButtonGroup();
        JToggleButton tabBasic = UiTheme.createTabButton("Basic Information");
        tabBasic.setMnemonic(KeyEvent.VK_B);   // Alt+B switches to Basic Information
        JToggleButton tabHistory = UiTheme.createTabButton("Patient History");
        tabHistory.setMnemonic(KeyEvent.VK_H); // Alt+H switches to Patient History
        // Action listeners both toggle the tab's selection and switch the visible card.
        tabBasic.addActionListener(e -> cards.show(cardPanel, "basic"));
        tabHistory.addActionListener(e -> cards.show(cardPanel, "history"));
        tabGroup.add(tabBasic);
        tabGroup.add(tabHistory);
        tabBar.add(tabBasic);
        tabBar.add(tabHistory);
        tabBasic.setSelected(true);

        JPanel tabsContainer = new JPanel(new BorderLayout());
        tabsContainer.setBackground(FORM_BG);
        tabsContainer.add(tabBar, BorderLayout.NORTH);
        tabsContainer.add(cardPanel, BorderLayout.CENTER);
        add(tabsContainer, BorderLayout.CENTER);
        // ===== Enter-to-next-focus =====
        enableEnterFocus(txtName);
        enableEnterFocus(txtMobile);
        enableEnterFocus(txtAge);
        enableEnterFocus(cmbGender);
        enableEnterFocus(cmbMarital);
        enableEnterFocus(txtAddress);
        enableEnterFocus(txtOccupation);
        enableEnterFocus(cmbBloodGroup);
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
        // ===== CANCEL + SAVE BUTTONS =====
        // Cancel routes through the existing windowClosing handler so the user still gets the
        // unsaved-changes confirmation prompt, matching the ESC shortcut behavior.
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setMnemonic(KeyEvent.VK_C);
        btnCancel.addActionListener(e ->
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        JButton btnSave = new JButton("Save");
        btnSave.setMnemonic(KeyEvent.VK_S);
        UiTheme.asPrimary(btnSave);
        btnSave.addActionListener(e -> saveData());
        // South region: a vertical stack of (a) brand separator inset from the window edges and
        // (b) right-aligned Cancel + Save row. The horizontal padding around the separator
        // matches the gutter used elsewhere in the app so the line doesn't run edge-to-edge.
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBackground(FORM_BG);
        JPanel sepWrap = new JPanel(new BorderLayout());
        sepWrap.setBackground(FORM_BG);
        sepWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        sepWrap.add(UiTheme.newDividerSeparator(), BorderLayout.CENTER);
        // Match the tighter spacing used by SessionFormDialog (FlowLayout vgap=0, 6-px top
        // padding) so the buttons sit just under the separator instead of floating below it.
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(FORM_BG);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 16));
        buttonRow.add(btnCancel);
        buttonRow.add(btnSave);
        south.add(sepWrap);
        south.add(buttonRow);
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

        btnUploadReport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "PDF & Images", "pdf", "jpg", "jpeg", "png"
            ));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtReportPath.setText(chooser.getSelectedFile().getAbsolutePath());
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

    /**
     * Builds the "Basic Information" tab content: name, mobile, gender/marital,
     * age/weight/height, address, occupation, blood group, and duration.
     */
    private JPanel buildBasicPanel() {
        JPanel basicPanel = whitePanel();
        GridBagConstraints bgbc = newGbc();
        int y = 0;
        y = addSectionHeader(basicPanel, bgbc, y, "Basic Information");
        y = addRow(basicPanel, bgbc, y, "Name", txtName);
        // Mobile is wrapped in a left-aligned flow panel so the field keeps its preferred width
        // (~15 cols) instead of stretching across the row.
        JPanel mobileWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mobileWrap.setOpaque(false);
        mobileWrap.add(txtMobile);
        y = addRow(basicPanel, bgbc, y, "Mobile", mobileWrap);
        // Gender + Marital share one inline row; each combo sizes to its content (not stretched).
        y = addInlineRow(
                basicPanel,
                bgbc,
                y,
                new JLabel("Gender"), cmbGender,
                new JLabel("Marital"), cmbMarital);
        // Single combined row: Age | Weight kg | Height cm.
        y = addInlineRow(
                basicPanel,
                bgbc,
                y,
                new JLabel("Age"), txtAge,
                new JLabel("Weight"), txtWeight, new JLabel("kg"),
                new JLabel("Height"), txtHeight, new JLabel("cm"));
        y = addRow(basicPanel, bgbc, y, "Address", txtAddress);
        y = addRow(basicPanel, bgbc, y, "Occupation", txtOccupation);
        // Blood Group must not stretch full-width either; wrap the combo in a flow-left panel.
        JPanel bloodWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bloodWrap.setOpaque(false);
        bloodWrap.add(cmbBloodGroup);
        y = addRow(basicPanel, bgbc, y, "Blood Group", bloodWrap);
        y = addRow(basicPanel, bgbc, y, "Duration", txtDuration);
        addVerticalFiller(basicPanel, bgbc, y);
        return basicPanel;
    }

    /**
     * Builds the "Patient History" tab content: clinical history, pain points grid,
     * vitals, report upload, and trailing narrative fields.
     */
    private JPanel buildHistoryPanel() {
        JPanel historyPanel = whitePanel();
        GridBagConstraints hgbc = newGbc();
        int h = 0;
        h = addSectionHeader(historyPanel, hgbc, h, "Patient History");
        h = addRow(historyPanel, hgbc, h, "Main Disease", txtMainDisease);
        h = addRow(historyPanel, hgbc, h, "Complications", txtComplications);
        h = addRow(historyPanel, hgbc, h, "Symptoms", txtSymptoms);
        h = addRow(historyPanel, hgbc, h, "Pain Points", buildPainPointsPanel());
        h = addRow(historyPanel, hgbc, h, "Vitals", buildVitalsPanel());
        h = addRow(historyPanel, hgbc, h, "Previous Treatment", txtPreviousTreatment);
        h = addRow(historyPanel, hgbc, h, "Medicines", txtMedicines);
        h = addRow(historyPanel, hgbc, h, "Detailed History", txtDetailedHistory);
        h = addRow(historyPanel, hgbc, h, "Examination", txtExamination);
        // Upload-report row: read-only path field on the left, "Upload Report" button on the right.
        JPanel uploadPanel = new JPanel(new BorderLayout(6, 0));
        uploadPanel.setOpaque(false);
        uploadPanel.add(txtReportPath, BorderLayout.CENTER);
        uploadPanel.add(btnUploadReport, BorderLayout.EAST);
        h = addRow(historyPanel, hgbc, h, "Upload Report", uploadPanel);
        h = addRow(historyPanel, hgbc, h, "Report Analysis", txtReportAnalysis);
        h = addRow(historyPanel, hgbc, h, "Allergy", txtAllergy);
        h = addRow(historyPanel, hgbc, h, "Remarks", txtRemarks);
        addVerticalFiller(historyPanel, hgbc, h);
        return historyPanel;
    }

    /**
     * Builds the pain-points grid: 4-column GridBagLayout (label, combo, label, combo) with a
     * 3-px label-to-combo gap and a 12-px gap between pairs. Trailing horizontal glue absorbs
     * any leftover width so cells stay flush-left.
     */
    private JPanel buildPainPointsPanel() {
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
        return painPanel;
    }

    /** Builds the inline vitals row (BP, Pulse, O2, Temp) with 3-px left padding on each label. */
    private JPanel buildVitalsPanel() {
        JPanel vitals = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        vitals.setBackground(FORM_BG);
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
        return vitals;
    }

    private static JScrollPane whiteScrollPane(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.getViewport().setBackground(FORM_BG);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void styleAllInputs() {
        JTextField[] fields = {
            txtName, txtMobile, txtOccupation,
            txtHeight, txtDuration, txtBP, txtPulse, txtO2, txtTemp, txtReportPath
        };
        for (JTextField f : fields) {
            UiTheme.styleField(f);
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

    /**
     * Creates a mouse-wheel-scrollable {@link JSpinner} bounded to {@code [min, max]} (step = 1).
     * Initial value is {@code min}. Scrolling up increments, scrolling down decrements.
     */
    private JSpinner createBoundedSpinner(int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
        spinner.addMouseWheelListener(e -> {
            int current = ((Number) spinner.getValue()).intValue();
            // Wheel-up returns negative rotation; subtract so up = increase.
            int next = current - e.getWheelRotation();
            spinner.setValue(Math.max(min, Math.min(max, next)));
        });
        UiTheme.styleSpinner(spinner);
        return spinner;
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
        // JSpinner: hook the underlying editor text field so Enter moves to the next component.
        if (component instanceof JSpinner spinner) {
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
                defaultEditor.getTextField().addActionListener(e -> spinner.transferFocus());
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
            //Float weight = ((Number) txtWeight.floatValue();
            Float weight = txtWeight.getText().isEmpty()
                    ? null
                    : Float.parseFloat(txtWeight.getText());
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
            //patient.setAge(((Number) txtAge.getValue()).intValue());
            patient.setAge(
                    txtAge.getText().isEmpty()
                            ? 0
                            : Integer.parseInt(txtAge.getText())
            );
            patient.setGender((String) cmbGender.getSelectedItem());
            patient.setMaritalStatus((String) cmbMarital.getSelectedItem());
            patient.setAddress(txtAddress.getText());
            patient.setOccupation(txtOccupation.getText());
            // Normalize the unselected ("") option to null so the persistence layer stores absence
            // as NULL rather than an empty string.
            String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
            patient.setBloodGroup((bloodGroup == null || bloodGroup.isBlank()) ? null : bloodGroup);
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
