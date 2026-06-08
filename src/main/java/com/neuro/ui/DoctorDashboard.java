/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import com.neuro.model.PatientSummary;
import com.neuro.repo.Page;
import com.neuro.repo.PatientRepository;
import com.neuro.session.UserSession;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DoctorDashboard extends JFrame {
    private JTextField txtSearchMobile;
    private JTable tblPatients;
    private DefaultTableModel tableModel;

    // ---- Pagination state ----
    private static final int PAGE_SIZE = 25;
    /** Current zero-indexed page being displayed. */
    private int currentPage = 0;
    /** Total number of pages across the current dataset. */
    private int totalPages = 1;
    /** Total matching row count across all pages (for the status label). */
    private int totalRows = 0;
    /** Active mobile filter; {@code null} means "show all patients" (no search). */
    private String activeMobileFilter = null;
    private JLabel lblPageInfo;
    private JButton btnFirst, btnPrev, btnNext, btnLast;

    private int userId;
    private static final Logger logger = LogManager.getLogger(DoctorDashboard.class);
    private JLabel lblLogo = new JLabel();
    private JLabel lblTitle = new JLabel();

    private final AppContext context;
    private final PatientRepository patientRepo;

    public DoctorDashboard(int userId, AppContext context) {
        this.context = context;
        this.patientRepo = context.patientRepo();
        this.userId = (userId > 0) ? userId : UserSession.getUserId();
        logger.info("DoctorDashboard opened for userId={}", this.userId);
        setTitle(Messages.get("dashboard.title"));
        setSize(1058, 750); // ~1.41 width:height ratio
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                boolean confirmed = UiTheme.showConfirm(
                        DoctorDashboard.this,
                        Messages.get("dashboard.exit.title"),
                        Messages.get("dashboard.exit.message"));
                if (confirmed) {
                    logger.info("User confirmed dashboard exit; terminating application");
                    dispose();
                    System.exit(0);
                } else {
                    logger.debug("User cancelled dashboard exit");
                }
            }
        });
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        // ================= TOP BAR =================
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel clinicHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        clinicHeader.setBackground(Color.WHITE);
        lblLogo.setPreferredSize(new Dimension(40, 40));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setVerticalAlignment(SwingConstants.CENTER);
        clinicHeader.add(lblLogo);
        clinicHeader.add(lblTitle);
        applyClinicHeader(ClinicConfig.load());
        // Use the configured clinic logo (if any) as the window icon so the OS taskbar /
        // minimized-window glyph shows the brand instead of the default Java icon.
        AppIcon.applyAll(this);
        // ================= TOP BAR BUTTONS (icon-only) =================
        // Compact icon-only buttons keep the header clean; tooltips and mnemonics preserve discoverability.
        JButton settingsBtn = makeIconButton(
                "\u2699", Messages.get("dashboard.button.settings.tooltip"), KeyEvent.VK_C);
        JButton btnLogout = makeIconButton(
                "\u23FB", Messages.get("dashboard.button.logout.tooltip"), KeyEvent.VK_L);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(settingsBtn);
        rightPanel.add(btnLogout);
        topBar.add(clinicHeader, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);
        // ================= MAIN =================
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);
        // SEARCH (search controls on the left, patient actions pinned to the right)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchControls.setBackground(Color.WHITE);
        searchControls.add(new JLabel(Messages.get("dashboard.search.label")));
        txtSearchMobile = new JTextField(15);
        txtSearchMobile.setBorder(UiTheme.BORDER);
        txtSearchMobile.setToolTipText(Messages.get("dashboard.search.tooltip.field"));
        searchControls.add(txtSearchMobile);
        JButton btnSearch = new JButton(Messages.get("dashboard.search.button"));
        btnSearch.setMnemonic(KeyEvent.VK_S);
        btnSearch.setToolTipText(Messages.get("dashboard.search.tooltip.button"));
        searchControls.add(btnSearch);
        // Icon-only Add / Remove patient buttons, right-aligned on the search row.
        JButton btnAddPatient = makeIconButton(
                "\u002B", Messages.get("dashboard.button.add.tooltip"), KeyEvent.VK_A);
        JButton btnRemovePatient = makeIconButton(
                "\u2212", Messages.get("dashboard.button.remove.tooltip"), KeyEvent.VK_R);
        JPanel patientActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        patientActions.setBackground(Color.WHITE);
        patientActions.add(btnAddPatient);
        patientActions.add(btnRemovePatient);
        searchPanel.add(searchControls, BorderLayout.WEST);
        searchPanel.add(patientActions, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        // TABLE
        String[] columns = {
            Messages.get("dashboard.column.id"),
            Messages.get("dashboard.column.name"),
            Messages.get("dashboard.column.mobile"),
            Messages.get("dashboard.column.age"),
            Messages.get("dashboard.column.gender"),
            Messages.get("dashboard.column.action")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5; // only the action column is "editable" so the button can receive clicks
            }
        };
        tblPatients = new JTable(tableModel);
        tblPatients.setBackground(Color.WHITE);
        tblPatients.setRowHeight(28);
        JTableHeader tableHeader = tblPatients.getTableHeader();
        tableHeader.setBackground(UiTheme.BRAND);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setOpaque(true); // required so the brand color paints on macOS/Aqua L&F
        tableHeader.setFont(tableHeader.getFont().deriveFont(Font.BOLD));
        TableColumn actionCol = tblPatients.getColumnModel().getColumn(5);
        actionCol.setMinWidth(110);
        actionCol.setMaxWidth(140);
        actionCol.setCellRenderer(new ViewButtonRenderer());
        actionCol.setCellEditor(new ViewButtonEditor(tblPatients, this::viewPatientDetailsForRow));
        JScrollPane tableScroll = new JScrollPane(tblPatients);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setBorder(UiTheme.BORDER);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        // PAGINATION BAR
        mainPanel.add(buildPaginationBar(), BorderLayout.SOUTH);
        // Normalize every button on the dashboard to the standard search-button height so the
        // header, search row and pagination bar share a consistent vertical rhythm.
        equalizeButtonHeights(
                btnSearch,
                settingsBtn, btnLogout,
                btnAddPatient, btnRemovePatient,
                btnFirst, btnPrev, btnNext, btnLast);
        // ================= ACTIONS =================
        btnSearch.addActionListener(e -> searchPatients());
        btnAddPatient.addActionListener(e -> openPatientForm());
        btnRemovePatient.addActionListener(e -> removeSelectedPatient());
        btnLogout.addActionListener(e -> logout());
        settingsBtn.addActionListener(e -> {
            ClinicSettingsDialog dialog = new ClinicSettingsDialog(this);
            dialog.setVisible(true);
            refreshHeader();
        });
        tblPatients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewPatientDetails();
            }
        });
        loadAllPatients();
        logger.info("Dashboard initialized successfully for userId={}", userId);
    }

    // ================= OPEN FORM =================
    private void openPatientForm() {
        logger.info("Opening patient registration form for userId={}", userId);
        PatientHistoryFormMySQL form = new PatientHistoryFormMySQL(this, userId, context);
        form.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadAllPatients();
            }
        });
        // Modal dialog: setVisible(true) blocks until the form is disposed.
        form.setVisible(true);
    }

    // ================= LOAD ALL =================
    /** Resets the dataset to "no filter" and loads the first page from the database. */
    private void loadAllPatients() {
        activeMobileFilter = null;
        loadPage(0);
    }

    // ================= SEARCH =================
    /** Reads the search field and (re)loads the first matching page from the database. */
    private void searchPatients() {
        String mobile = txtSearchMobile.getText().trim();
        if (mobile.isEmpty()) {
            loadAllPatients();
            return;
        }
        activeMobileFilter = mobile;
        loadPage(0);
    }

    /**
     * Issues a paginated SQL query for the requested page using the current
     * {@link #activeMobileFilter} and populates the table + pagination controls
     * with the returned {@link Page}.
     */
    private void loadPage(int requestedPage) {
        try {
            Page<PatientSummary> result;
            if (activeMobileFilter == null) {
                logger.info("Loading patient page userId={} page={}", userId, requestedPage);
                result = patientRepo.getPatientsSummaryPage(userId, requestedPage, PAGE_SIZE);
            } else {
                logger.info(
                        "Loading search page userId={} mobile={} page={}",
                        userId, activeMobileFilter, requestedPage);
                result = patientRepo.searchPatientsSummaryByMobilePage(
                        userId, activeMobileFilter, requestedPage, PAGE_SIZE);
            }
            currentPage = result.page();
            totalRows = result.total();
            totalPages = result.totalPages();
            tableModel.setRowCount(0);
            for (PatientSummary p : result.items()) {
                tableModel.addRow(toRow(p));
            }
            updatePaginationControls();
        } catch (Exception ex) {
            logger.error("Failed loading patient page userId={} page={}", userId, requestedPage, ex);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("dashboard.error.load", ex.getMessage()));
        }
    }

    private static Object[] toRow(PatientSummary p) {
        return new Object[] {
            p.patientId(), p.name(), p.mobile(), p.age(), p.gender(), Messages.get("dashboard.action.view")
        };
    }

    // ================= PAGINATION UI =================

    /**
     * Builds the bottom pagination bar with First / Previous / Next / Last buttons
     * (Unicode glyphs) and a "Page X of Y (N patients)" status label, all themed
     * via {@link UiTheme}.
     */
    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        btnFirst = makePagerButton("\u23EE", Messages.get("dashboard.pager.first"));
        btnPrev = makePagerButton("\u25C0", Messages.get("dashboard.pager.prev"));
        btnNext = makePagerButton("\u25B6", Messages.get("dashboard.pager.next"));
        btnLast = makePagerButton("\u23ED", Messages.get("dashboard.pager.last"));
        btnFirst.addActionListener(e -> loadPage(0));
        btnPrev.addActionListener(e -> loadPage(currentPage - 1));
        btnNext.addActionListener(e -> loadPage(currentPage + 1));
        btnLast.addActionListener(e -> loadPage(totalPages - 1));
        lblPageInfo = new JLabel(Messages.get("dashboard.pager.empty"));
        lblPageInfo.setForeground(UiTheme.SUBTITLE_GRAY);
        lblPageInfo.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        bar.add(lblPageInfo);
        bar.add(btnFirst);
        bar.add(btnPrev);
        bar.add(btnNext);
        bar.add(btnLast);
        return bar;
    }

    /** Builds a single small pager button with the given Unicode glyph and tooltip. */
    private static JButton makePagerButton(String glyph, String tooltip) {
        JButton b = new JButton(glyph);
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.setMargin(new Insets(2, 8, 2, 8));
        b.setForeground(UiTheme.BRAND);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        return b;
    }

    /** Refreshes the status label and enables/disables pager buttons based on current page. */
    private void updatePaginationControls() {
        lblPageInfo.setText(Messages.format(
                "dashboard.pager.info",
                currentPage + 1, totalPages, totalRows, totalRows == 1 ? "" : "s"));
        btnFirst.setEnabled(currentPage > 0);
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
        btnLast.setEnabled(currentPage < totalPages - 1);
    }

    // ================= VIEW =================
    private void viewPatientDetails() {
        int row = tblPatients.getSelectedRow();
        if (row == -1) {
            logger.warn("View details attempted without selecting patient");
            UiTheme.showInfo(this,
                    Messages.get("common.selection.title"),
                    Messages.get("dashboard.select.patient"));
            return;
        }
        viewPatientDetailsForRow(row);
    }

    private void viewPatientDetailsForRow(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return;
        }
        int patientId = (int) tableModel.getValueAt(row, 0);
        logger.info("Opening patient details for patientId={}", patientId);
        // PatientDetailsFrame is now an APPLICATION_MODAL JDialog, so the dashboard stays
        // visible behind it and regains focus automatically when it is dismissed.
        new PatientDetailsFrame(this, patientId, context).setVisible(true);
        // Refresh in case sessions/data changed while the modal was open.
        loadAllPatients();
    }

    // ================= REMOVE =================
    /**
     * Deletes the currently selected patient (and their sessions) after explicit confirmation.
     * No-op with an info dialog if no row is selected.
     */
    private void removeSelectedPatient() {
        int row = tblPatients.getSelectedRow();
        if (row == -1) {
            logger.warn("Remove patient attempted without selection userId={}", userId);
            UiTheme.showInfo(this,
                    Messages.get("common.selection.title"),
                    Messages.get("dashboard.select.patient.remove"));
            return;
        }
        int patientId = (int) tableModel.getValueAt(row, 0);
        String patientName = String.valueOf(tableModel.getValueAt(row, 1));
        boolean confirmed = UiTheme.showConfirm(
                this,
                Messages.get("dashboard.remove.title"),
                Messages.format("dashboard.remove.message", patientName, patientId));
        if (!confirmed) {
            logger.info("Patient removal cancelled patientId={} userId={}", patientId, userId);
            return;
        }
        try {
            boolean removed = patientRepo.deletePatient(patientId, userId);
            if (removed) {
                logger.info("Patient removed patientId={} userId={}", patientId, userId);
                loadPage(currentPage);
            } else {
                logger.warn("Patient removal affected 0 rows patientId={} userId={}", patientId, userId);
                UiTheme.showInfo(this,
                        Messages.get("dashboard.remove.notRemoved.title"),
                        Messages.get("dashboard.remove.notRemoved"));
            }
        } catch (Exception ex) {
            logger.error("Failed removing patientId={} userId={}", patientId, userId, ex);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("dashboard.remove.error", ex.getMessage()));
        }
    }

    /** Builds a small square icon-only button with a brand-colored glyph and tooltip. */
    private static JButton makeIconButton(String glyph, String tooltip, int mnemonic) {
        JButton b = new JButton(glyph);
        b.setToolTipText(tooltip);
        b.setMnemonic(mnemonic);
        b.setFocusable(false);
        b.setMargin(new Insets(2, 8, 2, 8));
        b.setForeground(UiTheme.BRAND);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        return b;
    }

    /**
     * Forces every button in {@code others} to take on {@code reference}'s preferred height
     * while keeping each button's own preferred width. This is used to give the entire
     * dashboard a consistent button height (matched to the standard Search button).
     */
    private static void equalizeButtonHeights(JButton reference, JButton... others) {
        int h = reference.getPreferredSize().height;
        for (JButton b : others) {
            Dimension pref = b.getPreferredSize();
            b.setPreferredSize(new Dimension(pref.width, h));
        }
    }

    // ================= LOGOUT =================
    private void logout() {
        logger.info("Logout initiated by userId={}", userId);
        boolean confirm = UiTheme.showConfirm(this,
                Messages.get("dashboard.logout.title"),
                Messages.get("dashboard.logout.message"));
        if (confirm) {
            logger.info("User {} logged out successfully", userId);
            UserSession.clear();
            dispose();
            System.exit(0);
        } else {
            logger.info("Logout cancelled by userId={}", userId);
        }
    }

    /** 3px inset around the View button so it doesn't touch the cell edges. */
    private static final int VIEW_BUTTON_INSET = 5;

    /** Wraps the given button in a panel that pads it by {@link #VIEW_BUTTON_INSET} on every edge. */
    private static JPanel wrapWithInset(JButton button, Color background) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(background);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
                VIEW_BUTTON_INSET, VIEW_BUTTON_INSET, VIEW_BUTTON_INSET, VIEW_BUTTON_INSET));
        wrapper.add(button, BorderLayout.CENTER);
        return wrapper;
    }

    /** Renders the action column cell as a "View" button inset by 3px on every side. */
    private static class ViewButtonRenderer implements TableCellRenderer {
        private final JButton button = new JButton();
        private final JPanel wrapper;
        ViewButtonRenderer() {
            button.setFocusable(false);
            button.setMargin(new Insets(2, 8, 2, 8));
            wrapper = wrapWithInset(button, Color.WHITE);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText(value == null ? Messages.get("dashboard.action.view") : value.toString());
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return wrapper;
        }
    }

    /** Editor that converts a click on the action cell into a row-specific callback. */
    private static class ViewButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final JPanel wrapper;
        private final java.util.function.IntConsumer onClick;
        private int currentRow = -1;
        ViewButtonEditor(JTable table, java.util.function.IntConsumer onClick) {
            super(new JCheckBox());
            this.onClick = onClick;
            this.button = new JButton(Messages.get("dashboard.action.view"));
            button.setFocusable(false);
            button.setMargin(new Insets(2, 8, 2, 8));
            this.wrapper = wrapWithInset(button, Color.WHITE);
            button.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0) {
                    onClick.accept(currentRow);
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText(value == null ? Messages.get("dashboard.action.view") : value.toString());
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return wrapper;
        }
        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    // ================= HEADER REFRESH =================
    private void refreshHeader() {
        logger.info("Clinic header refreshed");
        applyClinicHeader(ClinicConfig.load());
        // Refresh the window/taskbar icon as well: the user may have just changed the clinic logo.
        AppIcon.applyAll(this);
    }

    /**
     * Renders the clinic name and logo in the dashboard header.
     * When either value is missing, shows a default placeholder (hospital icon + dimmed
     * "Your Clinic Name" text) with a tooltip prompting the user to open Clinic Settings.
     * This signals to first-time users that the header is customizable.
     */
    private void applyClinicHeader(ClinicInfo info) {
        final String customizeHint = Messages.get("dashboard.clinic.tooltip");
        // --- Name ---
        String name = (info != null) ? info.getName() : null;
        boolean hasName = name != null && !name.trim().isEmpty();
        lblTitle.setText(hasName ? name : Messages.get("dashboard.clinic.placeholder"));
        lblTitle.setFont(new Font("Segoe UI", hasName ? Font.BOLD : Font.ITALIC, 20));
        lblTitle.setForeground(hasName ? UiTheme.BRAND : UiTheme.PLACEHOLDER_GRAY);
        lblTitle.setToolTipText(hasName ? null : customizeHint);
        // --- Logo ---
        String logoPath = (info != null) ? info.getLogoPath() : null;
        boolean hasLogo = logoPath != null && !logoPath.isEmpty() && new java.io.File(logoPath).isFile();
        if (hasLogo) {
            try {
                ImageIcon icon = new ImageIcon(logoPath);
                Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(img));
                lblLogo.setText(null);
                lblLogo.setBorder(null);
                lblLogo.setToolTipText(null);
            } catch (Exception e) {
                logger.warn("Clinic logo load failed path={}", logoPath, e);
                hasLogo = false;
            }
        }
        if (!hasLogo) {
            // Default placeholder icon: hospital emoji rendered as text inside the label.
            lblLogo.setIcon(null);
            lblLogo.setText("\uD83C\uDFE5"); // 🏥
            lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            lblLogo.setForeground(UiTheme.PLACEHOLDER_GRAY);
            lblLogo.setBorder(BorderFactory.createDashedBorder(UiTheme.BRAND));
            lblLogo.setToolTipText(customizeHint);
        }
    }
}
