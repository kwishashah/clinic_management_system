/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import com.neuro.model.Patient;
import com.neuro.model.Session;
import com.neuro.repo.PatientRepository;
import com.neuro.repo.SessionRepository;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PatientDetailsFrame extends JDialog {
    private static final Logger logger = LogManager.getLogger(PatientDetailsFrame.class);
    private JTextArea textArea;
    private int patientId;
    private DoctorDashboard parentFrame;
    private String reportPath = "";

    private JTable sessionTable;
    private DefaultTableModel sessionModel;
    /**
     * Cache of {@link Session} objects in the same order as {@link #sessionModel}'s rows, so the
     * pain-data View button can hand a full Session to the read-only matrix dialog instead of
     * re-parsing the raw string out of a table cell.
     */
    private final List<Session> currentSessions = new ArrayList<>();

    private final AppContext context;
    private final SessionRepository sessionRepo;
    private final PatientRepository patientRepo;

    public PatientDetailsFrame(DoctorDashboard parentFrame, int patientId, AppContext context) {
        super(parentFrame, Messages.get("patient.details.title"), ModalityType.APPLICATION_MODAL);
        this.parentFrame = parentFrame;
        this.patientId = patientId;
        this.context = context;
        this.sessionRepo = context.sessionRepo();
        this.patientRepo = context.patientRepo();
        logger.info("Opening PatientDetailsFrame for patientId={}", patientId);
        setSize(1000, 708); // ~1.412 width:height ratio (matches application standard)
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
                Messages.get("patient.details.title"),
                Messages.get("patient.details.subtitle"));
        // ================= TEXT AREA =================
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(UiTheme.BORDER);
        // ================= TABLE =================
        // Pain Data (col 4) holds the raw "before->after" pain string. We render it as a
        // single "View" button per row that opens a themed dialog, so rows stay short.
        String[] columns = {"ID", "Session No", "Date", "Treatment", "Pain Data", "Summary"};
        sessionModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the action column needs to be editable so the button can receive clicks.
                return column == PAIN_COL;
            }
        };
        sessionTable = new JTable(sessionModel);
        sessionTable.setBackground(Color.WHITE);
        sessionTable.setRowHeight(28);
        UiTheme.brandTableHeader(sessionTable);
        // Replace the cell with a fixed-label "View" button; the click handler reads the raw
        // string from the model and opens a themed dialog with a Region/Before/After breakdown.
        sessionTable.getColumnModel().getColumn(PAIN_COL).setCellRenderer(UiTheme.viewButtonRenderer("View"));
        sessionTable.getColumnModel().getColumn(PAIN_COL).setCellEditor(
                UiTheme.viewButtonEditor("View", this::showPainDataForRow));
        sessionTable.getColumnModel().getColumn(PAIN_COL).setMinWidth(90);
        sessionTable.getColumnModel().getColumn(PAIN_COL).setMaxWidth(120);
        JScrollPane tableScroll = new JScrollPane(sessionTable);
        tableScroll.setBorder(UiTheme.BORDER);
        tableScroll.getViewport().setBackground(Color.WHITE);
        // 🔥 DOUBLE CLICK → OPEN SESSION
        sessionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = sessionTable.getSelectedRow();
                    if (row == -1) {
                        logger.warn("Session double-click with no row selected");
                        return;
                    }
                    int sessionId = (int) sessionModel.getValueAt(row, 0);
                    logger.info("Editing sessionId={} for patientId={}", sessionId, patientId);
                    new SessionFormDialog(PatientDetailsFrame.this, patientId, sessionId, context).setVisible(true);
                }
            }
        });
        // Row 3: split pane (text on top, session table on bottom) — absorbs all vertical slack
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroll, tableScroll);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(380);
        splitPane.setBorder(null);
        splitPane.setBackground(UiTheme.BG_WHITE);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(splitPane, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Row 4: bottom brand separator
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        content.add(UiTheme.newDividerSeparator(), gbc);
        // Row 5: action buttons right-aligned (Back | Add Session | Open Report | Export PDF)
        JButton btnBack = new JButton("Back");
        btnBack.setMnemonic(KeyEvent.VK_B);
        JButton btnAddSession = new JButton("Add Session");
        btnAddSession.setMnemonic(KeyEvent.VK_A);
        JButton btnOpenReport = new JButton("Open Report");
        btnOpenReport.setMnemonic(KeyEvent.VK_O);
        JButton btnExportPDF = new JButton("Export PDF");
        btnExportPDF.setMnemonic(KeyEvent.VK_E);
        UiTheme.asPrimary(btnExportPDF);
        btnBack.addActionListener(e -> {
            logger.info("Returning to dashboard from patientId={}", patientId);
            dispose();
        });
        btnAddSession.addActionListener(e -> {
            logger.info("Opening add-session dialog for patientId={}", patientId);
            new SessionFormDialog(this, patientId, null, context).setVisible(true);
        });
        btnOpenReport.addActionListener(e -> {
            try {
                if (!reportPath.isEmpty()) {
                    logger.info("Opening report {} for patientId={}", reportPath, patientId);
                    Desktop.getDesktop().open(new java.io.File(reportPath));
                } else {
                    logger.warn("No report available for patientId={}", patientId);
                    UiTheme.showInfo(this,
                            Messages.get("patient.details.report.title"),
                            Messages.get("patient.details.report.none"));
                }
            } catch (Exception ex) {
                logger.error("Failed opening report for patientId={}", patientId, ex);
                UiTheme.showInfo(this,
                        Messages.get("common.error.title"),
                        Messages.get("patient.details.report.error"));
            }
        });
        btnExportPDF.addActionListener(e -> exportToPDF());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(UiTheme.BG_WHITE);
        buttonRow.add(btnBack);
        buttonRow.add(btnAddSession);
        buttonRow.add(btnOpenReport);
        buttonRow.add(btnExportPDF);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        content.add(buttonRow, gbc);
        getRootPane().setDefaultButton(btnBack);
        // ESC dismisses the dialog (returns to the dashboard).
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDetails");
        root.getActionMap().put("closeDetails", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                logger.debug("PatientDetailsFrame dismissed via ESC patientId={}", patientId);
                dispose();
            }
        });
        // ================= LOAD DATA =================
        loadPatientDetails();
        loadSessions();
    }

    // ================= LOAD SESSIONS =================
    public void loadSessions() {
        try {
            logger.info("Loading sessions for patientId={}", patientId);
            sessionModel.setRowCount(0);
            currentSessions.clear();
            List<Session> data = sessionRepo.getSessionsByPatient(patientId);
            logger.info("Loaded {} sessions for patientId={}", data.size(), patientId);
            for (Session s : data) {
                currentSessions.add(s);
                // Pain Data column holds an empty payload — the View button renders a fixed label
                // and the click handler looks up the full Session via the row index.
                sessionModel.addRow(new Object[] {
                    s.sessionId(), s.sessionNumber(), s.sessionDate(), s.treatment(), "", s.summary()
                });
            }
        } catch (Exception e) {
            logger.error("Failed loading sessions for patientId={}", patientId, e);
        }
    }

    // ================= LOAD PATIENT DETAILS =================
    private void loadPatientDetails() {
        logger.info("Loading patient details for patientId={}", patientId);
        try {
            Optional<Patient> found = patientRepo.findById(patientId);
            if (found.isEmpty()) {
                logger.warn("No patient found for patientId={}", patientId);
                return;
            }
            Patient p = found.get();
            logger.info("Patient record found for patientId={}", patientId);
            reportPath = safe(p.getReports());
            textArea.setText(buildPatientSummary(p));
        } catch (Exception e) {
            logger.error("Error loading patient details patientId={}", patientId, e);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("patient.details.error.load", e.getMessage()));
        }
    }

    private String buildPatientSummary(Patient p) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========== PATIENT DETAILS ===========\n\n");
        sb.append("Name: ").append(safe(p.getName())).append("\n");
        sb.append("Mobile: ").append(safe(p.getMobile())).append("\n");
        sb.append("Age: ").append(p.getAge() == null ? "" : p.getAge()).append("\n");
        sb.append("Gender: ").append(safe(p.getGender())).append("\n");
        sb.append("Marital Status: ").append(safe(p.getMaritalStatus())).append("\n\n");
        sb.append("Address: ").append(safe(p.getAddress())).append("\n");
        sb.append("Occupation: ").append(safe(p.getOccupation())).append("\n");
        sb.append("Blood Group: ").append(safe(p.getBloodGroup())).append("\n");
        sb.append("Height: ").append(p.getHeight() == null ? "" : p.getHeight()).append(" cm\n");
        sb.append("Weight: ").append(p.getWeight() == null ? "" : p.getWeight()).append(" kg\n\n");
        sb.append("Suffering Duration: ").append(safe(p.getSufferingDuration())).append("\n");
        sb.append("Main Disease: ").append(safe(p.getMainDisease())).append("\n");
        sb.append("Complications: ").append(safe(p.getComplications())).append("\n");
        sb.append("Symptoms: ").append(safe(p.getSymptoms())).append("\n\n");
        String[] labels = {
            "Pan", "Gas", "Gast", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0",
            "Mul0", "Follic", "Thia", "B12", "Nia"
        };
        String painRaw = safe(p.getPainPoints());
        String[] values = painRaw.split(",");
        sb.append("----- Pain Points -----\n");
        for (int i = 0; i < labels.length && i < values.length; i++) {
            sb.append(labels[i]).append(": ").append(values[i]).append("\n");
        }
        for (String v : values) {
            if (v.startsWith("L4=")) {
                sb.append("Left 4th: ").append(v.replace("L4=", "")).append("\n");
            }
            if (v.startsWith("R4=")) {
                sb.append("Right 4th: ").append(v.replace("R4=", "")).append("\n");
            }
        }
        sb.append("\n");
        sb.append("Previous Treatment: ").append(safe(p.getPreviousTreatment())).append("\n");
        sb.append("Medicines: ").append(safe(p.getMedicines())).append("\n");
        sb.append("Detailed History: ").append(safe(p.getDetailedHistory())).append("\n");
        sb.append("Examination: ").append(safe(p.getExamination())).append("\n\n");
        sb.append("----- VITALS -----\n");
        sb.append("BP: ").append(safe(p.getBp())).append("\n");
        sb.append("Pulse: ").append(safe(p.getPulse())).append("\n");
        sb.append("O2: ").append(safe(p.getO2())).append("\n");
        sb.append("Temperature: ").append(safe(p.getTemperature())).append("\n\n");
        sb.append("Reports: ").append(reportPath).append("\n");
        if (!reportPath.isEmpty()) {
            sb.append("(Use 'Open Report' button to view)\n");
        }
        sb.append("Report Analysis: ").append(safe(p.getMedia())).append("\n");
        sb.append("Allergy: ").append(safe(p.getPatientStory())).append("\n");
        sb.append("Remarks: ").append(safe(p.getRemarks())).append("\n");
        return sb.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // ================= EXPORT PDF =================

    private void exportToPDF() {
        logger.info("Exporting patient PDF for patientId={}", patientId);
        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                logger.debug("PDF export cancelled by user for patientId={}", patientId);
                return;
            }
            String path = chooser.getSelectedFile().getAbsolutePath() + ".pdf";
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                PDPageContentStream content = new PDPageContentStream(doc, page);
                PDFont font = PDType1Font.HELVETICA;
                float fontSize = 12;
                float leading = 14.5f;
                float margin = 50;
                float yStart = 750;
                float yPosition = yStart;
                float width = page.getMediaBox().getWidth() - 2 * margin;
                ClinicInfo info = ClinicConfig.load();
                float pageWidth = page.getMediaBox().getWidth();
                String clinicName = (info != null
                                && info.getName() != null
                                && !info.getName().isBlank())
                        ? info.getName()
                        : Messages.get("patient.details.pdf.defaultClinic");
                // Sizes
                float logoWidth = 60;
                float logoHeight = 40;
                float gap = 15;
                // text width
                float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(clinicName) / 1000 * 18;
                // total width of logo + gap + text
                float totalWidth = logoWidth + gap + textWidth;
                // centered starting x
                float startX = (pageWidth - totalWidth) / 2;
                float headerY = 735;
                // -------- Logo (LEFT) --------
                if (info != null
                        && info.getLogoPath() != null
                        && !info.getLogoPath().isEmpty()) {
                    try {
                        BufferedImage bufferedImage = ImageIO.read(new File(info.getLogoPath()));
                        if (bufferedImage != null) {
                            PDImageXObject logo = LosslessFactory.createFromImage(doc, bufferedImage);
                            content.drawImage(logo, startX, headerY - 10, logoWidth, logoHeight);
                        }
                    } catch (Exception e) {
                        logger.warn("Logo could not load", e);
                    }
                }
                // -------- Clinic Name (RIGHT OF LOGO) --------
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(startX + logoWidth + gap, headerY);
                content.showText(clinicName);
                content.endText();
                // -------- Divider --------
                content.moveTo(50, 690);
                content.lineTo(pageWidth - 50, 690);
                content.stroke();
                // Start content below header
                yPosition = 665;
                // ================= MAIN CONTENT =================
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(margin, yPosition);
                content.setLeading(leading);
                for (String line : textArea.getText().split("\n")) {
                    line = line.replace("\t", "    ");
                    for (String wrappedLine : wrapText(line, font, fontSize, width)) {
                        if (yPosition <= 50) {
                            logger.debug("Adding additional PDF page for patientId={}", patientId);
                            content.endText();
                            content.close();
                            page = new PDPage();
                            doc.addPage(page);
                            content = new PDPageContentStream(doc, page);
                            content.setFont(font, fontSize);
                            yPosition = yStart;
                            content.beginText();
                            content.newLineAtOffset(margin, yPosition);
                            content.setLeading(leading);
                        }
                        content.showText(wrappedLine);
                        content.newLine();
                        yPosition -= leading;
                    }
                }
                content.endText();
                content.close();
                doc.save(path);
                logger.info("PDF exported successfully {}", path);
            }
            UiTheme.showInfo(this, Messages.get("common.saved.title"), Messages.get("patient.details.pdf.success"));
        } catch (Exception e) {
            logger.error("PDF export failed for patientId={}", patientId, e);
            UiTheme.showInfo(this, Messages.get("common.error.title"), Messages.get("patient.details.pdf.error"));
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        // Tabs are not supported by PDFont; convert to spaces so the wrap math stays consistent.
        text = text.replace("\t", "    ");
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = line.toString() + word + " ";
            float size = font.getStringWidth(testLine) / 1000 * fontSize;
            if (size > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        lines.add(line.toString());
        return lines;
    }

    // ================= PAIN DATA DIALOG =================

    /** Column index of the Pain Data action button in the session table. */
    private static final int PAIN_COL = 4;

    /** Display labels for the 18 indexed pain pairs (must match SessionFormDialog.PAIN_NAMES order). */
    private static final String[] PAIN_LABELS = {
        "Pan", "Gas", "GasI", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov",
        "Dys", "Const", "Liv0", "Mu0", "Folic", "Thia", "B12", "Nia"
    };

    /** Click handler bound to the View button in the Pain Data column. */
    private void showPainDataForRow(int row) {
        if (row < 0 || row >= currentSessions.size()) {
            return;
        }
        showSessionPainDialog(currentSessions.get(row));
    }

    /**
     * Opens a themed read-only modal showing the session's pain matrix in the same
     * {@code Pain | Before | After} layout used by {@link SessionFormDialog}, but with plain
     * labels in place of the editable combos. Data is read directly from the {@link Session}
     * object (the persisted {@code painBefore} string carries both before and after values as
     * {@code before->after} pairs).
     */
    private void showSessionPainDialog(Session session) {
        JDialog dialog = new JDialog(this,
                "Pain Data \u2013 Session " + session.sessionNumber(), ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        Container content = dialog.getContentPane();
        content.setBackground(UiTheme.BG_WHITE);
        content.setLayout(new GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        int y = UiTheme.addDialogHeader(content, gbc, 0,
                "Pain Points",
                "Before \u2192 After comparison for session " + session.sessionNumber());
        // Pain matrix — same 3-column grid as SessionFormDialog, but every cell is a JLabel
        // (the dialog is read-only).
        JPanel painPanel = buildReadOnlyPainMatrix(session.painBefore());
        JScrollPane painScroll = new JScrollPane(painPanel);
        painScroll.setBorder(UiTheme.BORDER);
        painScroll.getViewport().setBackground(UiTheme.BG_WHITE);
        painScroll.setPreferredSize(new Dimension(360, 360));
        gbc.gridy = y;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(painScroll, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Bottom separator + Close button row, mirroring the rest of the app's dialog layout.
        gbc.gridy = y + 1;
        content.add(UiTheme.newDividerSeparator(), gbc);
        JButton btnClose = new JButton("Close");
        btnClose.setMnemonic(KeyEvent.VK_C);
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setBackground(UiTheme.BG_WHITE);
        buttons.add(btnClose);
        gbc.gridy = y + 2;
        gbc.anchor = GridBagConstraints.EAST;
        content.add(buttons, gbc);
        dialog.getRootPane().setDefaultButton(btnClose);
        // ESC closes the popup.
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closePain");
        root.getActionMap().put("closePain", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) { dialog.dispose(); }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Builds the read-only pain matrix panel: a {@link GridLayout} of three columns
     * (Pain region, Before value, After value), with a branded header row matching
     * the editable matrix in {@link SessionFormDialog}.
     */
    private static JPanel buildReadOnlyPainMatrix(String rawPain) {
        JPanel painPanel = new JPanel(new GridLayout(0, 3, 6, 6));
        painPanel.setBackground(UiTheme.BG_WHITE);
        painPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painPanel.add(UiTheme.brandHeaderLabel("Pain"));
        painPanel.add(UiTheme.brandHeaderLabel("Before"));
        painPanel.add(UiTheme.brandHeaderLabel("After"));
        // Pre-fill 18 indexed labels + L4/R4 with empty placeholders, then overwrite from rawPain.
        String[] before = new String[PAIN_LABELS.length];
        String[] after = new String[PAIN_LABELS.length];
        String[] l4 = {"", ""};
        String[] r4 = {"", ""};
        parsePainPairs(rawPain, before, after, l4, r4);
        for (int i = 0; i < PAIN_LABELS.length; i++) {
            painPanel.add(new JLabel(PAIN_LABELS[i]));
            painPanel.add(readOnlyValueLabel(before[i]));
            painPanel.add(readOnlyValueLabel(after[i]));
        }
        painPanel.add(new JLabel("Left 4th"));
        painPanel.add(readOnlyValueLabel(l4[0]));
        painPanel.add(readOnlyValueLabel(l4[1]));
        painPanel.add(new JLabel("Right 4th"));
        painPanel.add(readOnlyValueLabel(r4[0]));
        painPanel.add(readOnlyValueLabel(r4[1]));
        return painPanel;
    }

    /** Builds a single read-only value cell: centered, white background, brand border. */
    private static JLabel readOnlyValueLabel(String value) {
        JLabel label = new JLabel(value == null ? "" : value, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                UiTheme.BORDER, BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        return label;
    }

    /**
     * Parses the persisted pain string into {@code before}/{@code after} arrays for the 18
     * indexed regions plus the L4/R4 extremities. Empty / malformed entries leave the
     * corresponding slot blank.
     */
    private static void parsePainPairs(String raw, String[] before, String[] after,
            String[] l4, String[] r4) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        String[] entries = raw.split(",");
        int idx = 0;
        for (String entry : entries) {
            if (entry.startsWith("L4=")) {
                splitPair(entry.substring(3), l4);
            } else if (entry.startsWith("R4=")) {
                splitPair(entry.substring(3), r4);
            } else if (idx < before.length) {
                String[] pair = {"", ""};
                splitPair(entry, pair);
                before[idx] = pair[0];
                after[idx] = pair[1];
                idx++;
            }
        }
    }

    /** Splits {@code "a->b"} into {@code out[0]=a, out[1]=b}; leaves them empty if malformed. */
    private static void splitPair(String pair, String[] out) {
        int sep = pair.indexOf("->");
        if (sep < 0) {
            out[0] = pair.trim();
            out[1] = "";
        } else {
            out[0] = pair.substring(0, sep).trim();
            out[1] = pair.substring(sep + 2).trim();
        }
    }
}
