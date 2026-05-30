/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import com.neuro.model.PatientSummary;
import com.neuro.repo.PatientRepository;
import com.neuro.session.UserSession;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Stack;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DoctorDashboard extends JFrame {

    private JTextField txtSearchMobile;
    private JTable tblPatients;
    private DefaultTableModel tableModel;

    private int userId;
    private static final Logger logger = LogManager.getLogger(DoctorDashboard.class);
    private JLabel lblLogo = new JLabel();
    private JLabel lblTitle = new JLabel();

    private final AppContext context;
    private final PatientRepository patientRepo;
    private int currentPage = 1;

    private final int pageSize = 10;

    private JButton btnNext;
    private JButton btnPrev;
    private JButton btnFirst;
    private JButton btnLast;

    private JLabel lblPageInfo;
    public DoctorDashboard(int userId, AppContext context) {
        this.context = context;
        this.patientRepo = context.patientRepo();

        // ✅ SAFE SESSION HANDLING
        this.userId = (userId > 0) ? userId : UserSession.getUserId();
        logger.info("DoctorDashboard opened for userId={}", this.userId);
        setTitle("Doctor Dashboard");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ================= TOP BAR =================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel clinicHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        ClinicInfo info = ClinicConfig.load();

        if (info != null && info.getName() != null) {
            lblTitle.setText(info.getName());
        } else {
            lblTitle.setText("Neurotherapy Clinic");
        }

        if (info != null && info.getLogoPath() != null && !info.getLogoPath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(info.getLogoPath());
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                logger.warn("Failed to load clinic logo from {}", info.getLogoPath(), e);
            }
        }

        clinicHeader.add(lblLogo);
        clinicHeader.add(lblTitle);

        // ================= BUTTONS =================
        JButton btnAddPatient = new JButton("➕ Add Patient");
        JButton btnLogout = new JButton("Logout");
        JButton settingsBtn = new JButton("Clinic Settings");

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(settingsBtn);
        rightPanel.add(btnAddPatient);
        rightPanel.add(btnLogout);

        topBar.add(clinicHeader, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ================= MAIN =================
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        // SEARCH
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by Mobile:"));

        txtSearchMobile = new JTextField(15);
        searchPanel.add(txtSearchMobile);

        JButton btnSearch = new JButton("Search");
        searchPanel.add(btnSearch);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {"Patient ID", "Name", "Mobile", "Age", "Gender"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblPatients = new JTable(tableModel);
        mainPanel.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

        JButton btnView = new JButton("View Details");

        btnPrev = new JButton("Previous");

        btnNext = new JButton("Next");
        btnFirst = new JButton("First");

        btnLast = new JButton("Last");

        lblPageInfo = new JLabel("Page 1");

        JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.add(btnView, BorderLayout.WEST);

        JPanel paginationPanel = new JPanel();

        paginationPanel.add(btnFirst);
        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);
        paginationPanel.add(btnLast);

        bottomPanel.add(paginationPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ================= ACTIONS =================
        btnSearch.addActionListener(e -> searchPatients());
        btnView.addActionListener(e -> viewPatientDetails());
        btnPrev.addActionListener(e -> previousPage());
        btnFirst.addActionListener(e -> firstPage());

        btnLast.addActionListener(e -> lastPage());
        btnNext.addActionListener(e -> nextPage());
        btnAddPatient.addActionListener(e -> openPatientForm());

        btnLogout.addActionListener(e -> logout());

        settingsBtn.addActionListener(e -> {
            ClinicSettingsDialog dialog = new ClinicSettingsDialog();
            dialog.setModal(true);
            dialog.setVisible(true);
            refreshHeader();
        });

        tblPatients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewPatientDetails();
            }
        });

        loadPatients();
        logger.info("Dashboard initialized successfully for userId={}", userId);
    }
    private void firstPage() {

        currentPage = 1;

        loadPatients();
    }
    private void lastPage() {

        try {

            int totalRecords =
                    patientRepo.getPatientCount(userId);

            int totalPages =
                    (int) Math.ceil((double) totalRecords / pageSize);

            currentPage = totalPages;

            loadPatients();

        } catch (Exception e) {

            logger.error("Failed moving to last page", e);
        }
    }
    // ================= OPEN FORM =================
    private void openPatientForm() {
        logger.info("Opening patient registration form for userId={}", userId);
        PatientHistoryFormMySQL form = new PatientHistoryFormMySQL(userId, context);

        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                currentPage=1;
                loadPatients();
            }
        });

        form.setVisible(true);
    }

    // ================= LOAD ALL =================

    private void loadPatients() {

        tableModel.setRowCount(0);

        try {

            logger.info(
                    "Loading patients page={} pageSize={} userId={}",
                    currentPage,
                    pageSize,
                    userId
            );

            List<PatientSummary> patients =
                    patientRepo.getPatients(
                            userId,
                            currentPage,
                            pageSize
                    );

            for (PatientSummary patient : patients) {

                tableModel.addRow(new Object[]{
                        patient.patientId(),
                        patient.name(),
                        patient.mobile(),
                        patient.age(),
                        patient.gender()

                });

            }

            updatePaginationControls();

            logger.info(
                    "Loaded {} patients into dashboard",
                    patients.size()
            );

        } catch (Exception ex) {

            logger.error(
                    "Failed loading patients for userId={}",
                    userId,
                    ex
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading patients:\n" + ex.getMessage()
            );
        }
    }
    private void nextPage() {

        try {

            int totalRecords =
                    patientRepo.getPatientCount(userId);

            int totalPages =
                    (int) Math.ceil((double) totalRecords / pageSize);

            if (currentPage < totalPages) {

                currentPage++;

                loadPatients();
            }

        } catch (Exception e) {

            logger.error("Failed moving to next page", e);

            JOptionPane.showMessageDialog(
                    this,
                    "Failed loading next page:\n" + e.getMessage()
            );
        }
    }
    private void previousPage() {

        if (currentPage > 1) {

            currentPage--;

            loadPatients();
        }
    }
    private void updatePaginationControls() {

        try {

            int totalRecords =
                    patientRepo.getPatientCount(userId);

            int totalPages =
                    (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages == 0) {
                totalPages = 1;
            }

            lblPageInfo.setText(
                    "Page " + currentPage + " of " + totalPages
            );

            btnPrev.setEnabled(currentPage > 1);
            btnFirst.setEnabled(currentPage > 1);

            btnLast.setEnabled(currentPage < totalPages);
            btnNext.setEnabled(currentPage < totalPages);

        } catch (Exception e) {

            logger.error("Failed updating pagination controls", e);
        }
    }
    // ================= SEARCH =================
    private void searchPatients() {

        String mobile = txtSearchMobile.getText().trim();

        if (mobile.isEmpty()) {
            currentPage=1;
            loadPatients();
            return;
        }

        tableModel.setRowCount(0);

        try {
            logger.info("Searching patients for userId={} mobile={}", userId, mobile);
            List<PatientSummary> patients = patientRepo.searchPatientsByMobile(userId, mobile);

            for (PatientSummary patient : patients) {

                tableModel.addRow(new Object[]{
                        patient.patientId(),
                        patient.name(),
                        patient.mobile(),
                        patient.age(),
                        patient.gender()
                });
            }
            logger.info("Search returned {} records", patients.size());

        } catch (Exception ex) {
            logger.error("Patient search failed for mobile={}", mobile, ex);
            JOptionPane.showMessageDialog(this, "Search error:\n" + ex.getMessage());
        }
    }

    // ================= VIEW =================
    private void viewPatientDetails() {

        int row = tblPatients.getSelectedRow();

        if (row == -1) {
            logger.warn("View details attempted without selecting patient");
            JOptionPane.showMessageDialog(this, "Please select a patient.");
            return;
        }

        int patientId = (int) tableModel.getValueAt(row, 0);
        logger.info("Opening patient details for patientId={}", patientId);
        setVisible(false);
        new PatientDetailsFrame(this, patientId, context).setVisible(true);
    }

    // ================= LOGOUT =================
    private void logout() {
        logger.info("Logout initiated by userId={}", userId);
        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("User {} logged out successfully", userId);
            UserSession.clear();
            new LoginFrame(context).setVisible(true);
            dispose();
        } else {

            logger.info("Logout cancelled by userId={}", userId);
        }
    }

    // ================= HEADER REFRESH =================
    private void refreshHeader() {

        ClinicInfo info = ClinicConfig.load();

        if (info != null && info.getName() != null) {
            lblTitle.setText(info.getName());
        } else {
            lblTitle.setText("Neurotherapy Clinic");
        }

        if (info != null && info.getLogoPath() != null) {
            try {
                logger.info("Clinic header refreshed");
                ImageIcon icon = new ImageIcon(info.getLogoPath());
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                logger.warn("Clinic logo load failed", e);
                lblLogo.setIcon(null);
            }
        }
    }
}
