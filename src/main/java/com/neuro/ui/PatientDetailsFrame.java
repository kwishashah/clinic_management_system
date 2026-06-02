/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.config.ClinicConfig;
import com.neuro.repo.queries.SqlQueries;
import com.neuro.db.DBConnection;
import com.neuro.model.ClinicInfo;
import com.neuro.repo.SessionRepository;
import java.awt.*;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
import java.awt.event.KeyEvent;
public class PatientDetailsFrame extends JFrame {
    private static final Logger logger = LogManager.getLogger(PatientDetailsFrame.class);
    private JTextArea textArea;
    private int patientId;
    private DoctorDashboard parentFrame;
    private String reportPath = "";

    private JTable sessionTable;
    private DefaultTableModel sessionModel;

    private final AppContext context;
    private final SessionRepository sessionRepo;

    public PatientDetailsFrame(DoctorDashboard parentFrame, int patientId, AppContext context) {

        this.parentFrame = parentFrame;
        this.patientId = patientId;
        this.context = context;
        this.sessionRepo = context.sessionRepo();
        logger.info("Opening PatientDetailsFrame for patientId={}", patientId);
        setTitle("Patient Details");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        // ================= TEXT AREA =================
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane textScroll = new JScrollPane(textArea);

        // ================= BUTTON PANEL =================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnBack = new JButton("⬅ Back");
        JButton btnAddSession = new JButton("Add Session");
        JButton btnOpenReport = new JButton("📄 Open Report");
        JButton btnExportPDF = new JButton("🧾 Export PDF");

        btnBack.addActionListener(e -> {
            logger.info("Returning to dashboard from patientId={}", patientId);
            parentFrame.setVisible(true);
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
                    JOptionPane.showMessageDialog(this, "No report available");
                }
            } catch (Exception ex) {
                logger.error("Failed opening report for patientId={}", patientId, ex);
                JOptionPane.showMessageDialog(this, "Cannot open file");
            }
        });

        btnExportPDF.addActionListener(e -> exportToPDF());

        buttonPanel.add(btnBack);
        buttonPanel.add(btnAddSession);
        buttonPanel.add(btnOpenReport);
        buttonPanel.add(btnExportPDF);

        // ================= TABLE =================
        String[] columns = {"ID", "Session No", "Date", "Treatment", "Pain Data", "Summary"};

        sessionModel = new DefaultTableModel(columns, 0);
        sessionTable = new JTable(sessionModel);

        sessionTable.setRowHeight(120);
        JScrollPane tableScroll = new JScrollPane(sessionTable);
        tableScroll.setPreferredSize(new Dimension(800, 200));

        // 🔥 DOUBLE CLICK → OPEN SESSION
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
        // ================= TOP PANEL (TEXT + BUTTONS) =================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(textScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ================= SPLIT =================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, tableScroll);

        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);

        // ================= LOAD DATA =================
        loadPatientDetails();
        loadSessions();
        registerEscapeKey();
    }

    // ================= LOAD SESSIONS =================
    public void loadSessions() {
        try {
            logger.info("Loading sessions for patientId={}", patientId);
            sessionModel.setRowCount(0);

            var data = sessionRepo.getSessionsByPatient(patientId);
            logger.info("Loaded {} sessions for patientId={}", data.size(), patientId);
            for (var row : data) {

                //                String painData = ((String) row.get(4))
                //                        .replace(",", "\n")
                //                        .replace("->", " → ");
                // String painData = getShortPainPreview((String) row.get(4));
                String painData = formatPainData((String) row.get(4));
                sessionModel.addRow(
                        new Object[] {row.get(0), row.get(1), row.get(2), row.get(3), painData, row.get(6)});
            }

        } catch (Exception e) {
            // e.printStackTrace();

            logger.error("Failed loading sessions for patientId={}", patientId, e);
        }
    }

    // ================= LOAD PATIENT DETAILS =================
    private void loadPatientDetails() {
        logger.info("Loading patient details for patientId={}", patientId);
        String sql = SqlQueries.PATIENT_SELECT_BY_ID;

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                logger.info("Patient record found for patientId={}", patientId);
                StringBuilder sb = new StringBuilder();

                sb.append("=========== PATIENT DETAILS ===========\n\n");

                sb.append("Name: ").append(safe(rs.getString("patient_name"))).append("\n");
                sb.append("Mobile: ")
                        .append(safe(rs.getString("mobile_number")))
                        .append("\n");
                sb.append("Age: ").append(rs.getInt("age")).append("\n");
                sb.append("Gender: ").append(safe(rs.getString("gender"))).append("\n");
                sb.append("Marital Status: ")
                        .append(safe(rs.getString("marital_status")))
                        .append("\n\n");

                sb.append("Address: ").append(safe(rs.getString("address"))).append("\n");
                sb.append("Occupation: ")
                        .append(safe(rs.getString("occupation")))
                        .append("\n");
                sb.append("Blood Group: ")
                        .append(safe(rs.getString("blood_group")))
                        .append("\n");
                sb.append("Height: ").append(safe(rs.getString("height"))).append(" cm\n");
                sb.append("Weight: ").append(safe(rs.getString("weight"))).append(" kg\n\n");

                sb.append("Suffering Duration: ")
                        .append(safe(rs.getString("suffering_duration")))
                        .append("\n");
                sb.append("Main Disease: ")
                        .append(safe(rs.getString("main_disease")))
                        .append("\n");
                sb.append("Complications: ")
                        .append(safe(rs.getString("complications")))
                        .append("\n");
                sb.append("Symptoms: ").append(safe(rs.getString("symptoms"))).append("\n\n");

                // 🔴 PAIN POINTS
                String[] labels = {
                    "Pan", "Gas", "Gast", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0",
                    "Mul0", "Follic", "Thia", "B12", "Nia"
                };

                String painRaw = safe(rs.getString("pain_points"));
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

                sb.append("Previous Treatment: ")
                        .append(safe(rs.getString("previous_treatment")))
                        .append("\n");
                sb.append("Medicines: ").append(safe(rs.getString("medicines"))).append("\n");
                sb.append("Detailed History: ")
                        .append(safe(rs.getString("detailed_history")))
                        .append("\n");
                sb.append("Examination: ")
                        .append(safe(rs.getString("examination")))
                        .append("\n\n");

                sb.append("----- VITALS -----\n");
                sb.append("BP: ").append(safe(rs.getString("bp"))).append("\n");
                sb.append("Pulse: ").append(safe(rs.getString("pulse"))).append("\n");
                sb.append("O2: ").append(safe(rs.getString("o2"))).append("\n");
                sb.append("Temperature: ")
                        .append(safe(rs.getString("temperature")))
                        .append("\n\n");

                reportPath = safe(rs.getString("reports"));

                sb.append("Reports: ").append(reportPath).append("\n");
                if (!reportPath.isEmpty()) {
                    sb.append("(Use 'Open Report' button to view)\n");
                }

                sb.append("Report Analysis: ")
                        .append(safe(rs.getString("media")))
                        .append("\n");
                sb.append("Allergy: ")
                        .append(safe(rs.getString("patient_story")))
                        .append("\n");
                sb.append("Remarks: ").append(safe(rs.getString("remarks"))).append("\n");

                textArea.setText(sb.toString());
            } else {
                logger.warn("No patient found for patientId={}", patientId);
            }

        } catch (Exception e) {
            logger.error("Error loading patient details patientId={}", patientId, e);
            JOptionPane.showMessageDialog(this, "Error loading details:\n" + e.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String getShortPainPreview(String painData) {

        if (painData == null || painData.isEmpty()) return "";

        String[] labels = {
            "Pan", "Gas", "Gast", "WD", "Gal", "Spl", "Liv", "Mu", "Rtov", "Ltov", "Dys", "Const", "Liv0", "Mul0",
            "Follic", "Thia", "B12", "Nia"
        };

        String[] pairs = painData.split(",");

        StringBuilder sb = new StringBuilder();

        int count = 0;

        for (int i = 0; i < pairs.length && i < labels.length; i++) {

            if (pairs[i].contains("->")) {

                String[] vals = pairs[i].split("->");

                if (!vals[0].equals(vals[1])) {

                    sb.append(labels[i])
                            .append(": ")
                            .append(vals[0])
                            .append("→")
                            .append(vals[1])
                            .append("  ");

                    count++;
                }
            }

            if (count == 2) break;
        }

        if (count == 0) {
            logger.debug("Pain preview generated: No Change");
            return "No Change";
        }

        logger.debug("Pain preview generated {}", sb.toString());

        return sb.toString();
    }
    // ================= EXPORT PDF =================

    private void exportToPDF() {
        logger.info("Exporting patient PDF for patientId={}", patientId);
        try {
            JFileChooser chooser = new JFileChooser();

            // if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
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
                        : "Neurotherapy Clinic";

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

            JOptionPane.showMessageDialog(this, "PDF Saved!");

        } catch (Exception e) {
            // e.printStackTrace();
            logger.error("PDF export failed for patientId={}", patientId, e);
            JOptionPane.showMessageDialog(this, "Error creating PDF");
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {

        // 🔥 remove unsupported characters
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
    private void registerEscapeKey() {

        getRootPane().registerKeyboardAction(
                e -> {
                    parentFrame.setVisible(true);
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

}
