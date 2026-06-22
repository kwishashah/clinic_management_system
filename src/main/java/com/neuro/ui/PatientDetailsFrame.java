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
import javax.swing.table.JTableHeader;
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
        // Row 0: brand header label
        JLabel headerLabel = new JLabel(Messages.get("patient.details.title"));
        headerLabel.setFont(UiTheme.TITLE_FONT);
        headerLabel.setForeground(UiTheme.BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        content.add(headerLabel, gbc);
        // Row 1: top brand separator
        gbc.gridy = 1;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 2: subtitle / instructional label
        gbc.insets = new Insets(6, 8, 6, 8);
        JLabel subtitleLabel = new JLabel(Messages.get("patient.details.subtitle"));
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.SUBTITLE_GRAY);
        gbc.gridy = 2;
        content.add(subtitleLabel, gbc);
        // ================= TEXT AREA =================
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(UiTheme.BORDER);
        // ================= TABLE =================
        String[] columns = {"ID", "Session No", "Date", "Treatment", "Pain Data", "Summary"};
        sessionModel = new DefaultTableModel(columns, 0);
        sessionTable = new JTable(sessionModel);
        sessionTable.setBackground(Color.WHITE);
        sessionTable.setRowHeight(120);
        // Branded header (matches DoctorDashboard)
        JTableHeader sessionHeader = sessionTable.getTableHeader();
        sessionHeader.setBackground(UiTheme.BRAND);
        sessionHeader.setForeground(Color.WHITE);
        sessionHeader.setOpaque(true); // required so the brand color paints on macOS/Aqua L&F
        sessionHeader.setFont(sessionHeader.getFont().deriveFont(Font.BOLD));
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
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 5: action buttons right-aligned (Back | Add Session | Open Report | Export PDF)
        JButton btnBack = new JButton("Back");
        btnBack.setMnemonic(KeyEvent.VK_B);
        JButton btnAddSession = new JButton("Add Session");
        btnAddSession.setMnemonic(KeyEvent.VK_A);
        JButton btnOpenReport = new JButton("Open Report");
        btnOpenReport.setMnemonic(KeyEvent.VK_O);
        JButton btnExportPDF = new JButton("Export PDF");
        btnExportPDF.setMnemonic(KeyEvent.VK_E);
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
                if (reportPath == null || reportPath.isBlank()) {

                    UiTheme.showInfo(

                            this,

                            "Report",

                            "No report is attached for this patient.");

                    return;

                }

                File reportFile = new File(reportPath);

                if (!reportFile.exists()) {

                    UiTheme.showInfo(

                            this,

                            "Report Not Found",

                            "The report file could not be found.\n\n"

                                    + "It may have been moved, renamed, or deleted.");

                    return;

                }
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
            List<Session> data = sessionRepo.getSessionsByPatient(patientId);
            logger.info("Loaded {} sessions for patientId={}", data.size(), patientId);
            for (Session s : data) {
                String painData = formatPainData(s.painBefore());
                sessionModel.addRow(new Object[] {
                    s.sessionId(), s.sessionNumber(), s.sessionDate(), s.treatment(), painData, s.summary()
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
        sb.append("Report Analysis: ").append(safe(p.getPatientStory())).append("\n");

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

    private String formatPainData(String painData) {
        if (painData == null || painData.isEmpty()) return "";
        String[] labels = {
            "Pan", "Gas", "Gast", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mul0",
            "Follic", "Thia", "B12", "Nia"
        };
        StringBuilder sb = new StringBuilder();
        String[] pairs = painData.split(",");
        int index = 0;
        for (String p : pairs) {
            if (p.contains("->") && index < labels.length) {
                sb.append(labels[index])
                        .append(": ")
                        .append(p.replace("->", " → "))
                        .append("\n");
                index++;
            }
        }
        return sb.toString();
    }
}
