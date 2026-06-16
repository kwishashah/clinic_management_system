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
import javax.swing.table.TableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com .neuro.ui.UiTheme;
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
        // Vertical gap is 0 so the table section (table + footer) reads as one tightly-grouped block;
        // the search row gets its own bottom inset below to preserve breathing room above the table.
        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);
        // SEARCH (search controls on the left, patient actions pinned to the right)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchControls.setBackground(Color.WHITE);
        JLabel lblSearch = new JLabel(Messages.get("dashboard.search.label"));
        searchControls.add(lblSearch);
        txtSearchMobile = new JTextField(15);
        UiTheme.styleField(txtSearchMobile);
        UiTheme.attachNumericValidation(txtSearchMobile);
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
        // TABLE + PAGINATION as a single brand-bordered section so the pager reads as the table's footer.
        mainPanel.add(buildTableSection(), BorderLayout.CENTER);
        // Normalize every button on the dashboard to the standard search-button height so the
        // header, search row and pagination bar share a consistent vertical rhythm.
        equalizeButtonHeights(
                btnSearch,
                settingsBtn, btnLogout,
                btnAddPatient, btnRemovePatient,
                btnFirst, btnPrev, btnNext, btnLast);
        // Match the search field and its label height to the search-button height so the entire
        // search row shares a single baseline.
        int rowHeight = btnSearch.getPreferredSize().height;
        Dimension fieldPref = txtSearchMobile.getPreferredSize();
        txtSearchMobile.setPreferredSize(new Dimension(fieldPref.width, rowHeight));
        Dimension lblPref = lblSearch.getPreferredSize();
        lblSearch.setPreferredSize(new Dimension(lblPref.width, rowHeight));
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
        // ENTER on the selected row opens the patient details (overrides the default
        // "move to next cell" behavior of JTable).
        tblPatients.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewSelectedPatient");
        tblPatients.getActionMap().put("viewSelectedPatient", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewPatientDetails();
            }
        });
        // Window-level mnemonic: Alt+V (Option+V on macOS) triggers View for the selected row.
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_DOWN_MASK), "viewSelectedPatient");
        rootPane.getActionMap().put("viewSelectedPatient", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewPatientDetails();
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

    // ================= TABLE UI =================

    /** Background for odd-indexed (zebra) rows; subtle enough to scan without distracting. */
    private static final Color ZEBRA_BG = new Color(0xFAFAFA);

    /**
     * Builds the patient-list table inside its scroll pane. Adds zebra striping,
     * right-aligned numeric columns, and the per-row "View" action button.
     * <p>The outer brand border lives on {@link #buildTableSection()} so the table and the
     * pagination footer share a single visual frame.
     * <p>Side-effects: initializes {@link #tableModel} and {@link #tblPatients}.
     */
    private JScrollPane buildPatientsTable() {
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
        tblPatients = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                // Zebra striping for unselected, non-action cells (the action column renders its own button).
                if (!isRowSelected(row) && column != 5) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA_BG);
                }
                return c;
            }
        };
        tblPatients.setBackground(Color.WHITE);
        tblPatients.setRowHeight(28);
        UiTheme.brandTableHeader(tblPatients);
        // Left-align every data column with a 6-px left inset so text doesn't hug the grid line.
        // NOTE: DefaultTableCellRenderer resets the border on every render to the focus / no-focus
        // border, so we must re-apply the padding inside getTableCellRendererComponent and wrap
        // (not replace) the L&F-provided border.
        final javax.swing.border.Border cellPadding = BorderFactory.createEmptyBorder(0, 3, 0, 0);
        javax.swing.table.DefaultTableCellRenderer leftAlign = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                javax.swing.border.Border base = getBorder();
                setBorder(base == null ? cellPadding : BorderFactory.createCompoundBorder(base, cellPadding));
                return this;
            }
        };
        leftAlign.setHorizontalAlignment(SwingConstants.LEFT);
        for (int col = 0; col < 5; col++) {
            tblPatients.getColumnModel().getColumn(col).setCellRenderer(leftAlign);
        }
        TableColumn actionCol = tblPatients.getColumnModel().getColumn(5);
        actionCol.setMinWidth(110);
        actionCol.setMaxWidth(140);
        // The action column is self-describing (buttons say "View"); blank its header to reduce visual noise.
        actionCol.setHeaderValue("");
        final String viewLabel = Messages.get("dashboard.action.view");
        actionCol.setCellRenderer(UiTheme.viewButtonRenderer(viewLabel));
        actionCol.setCellEditor(UiTheme.viewButtonEditor(viewLabel, this::viewPatientDetailsForRow));
        JScrollPane tableScroll = new JScrollPane(tblPatients);
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setBorder(BorderFactory.createEmptyBorder()); // outer border lives on the section wrapper
        // Show scrollbars only when the content actually overflows, so the empty state stays clean.
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return tableScroll;
    }

    /**
     * Wraps the patients table and the pagination footer in a single brand-bordered
     * panel so the footer reads as part of the table (like a spreadsheet's sheet strip).
     * A thin {@link UiTheme#DIVIDER} line separates the rows from the footer.
     */
    private JPanel buildTableSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(UiTheme.BORDER);
        JScrollPane scroll = buildPatientsTable();
        JPanel footer = buildPaginationBar();
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.DIVIDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        section.add(scroll, BorderLayout.CENTER);
        section.add(footer, BorderLayout.SOUTH);
        return section;
    }

    // ================= PAGINATION UI =================

    /**
     * Builds the bottom pagination bar with First / Previous / Next / Last buttons
     * (Unicode glyphs) and a "Page X of Y (N patients)" status label, all themed
     * via {@link UiTheme}.
     */
    private JPanel buildPaginationBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(Color.WHITE);
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
        // Status (left): "Page X of Y" followed by a dim, discoverability-focused keyboard hint.
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statusPanel.setBackground(Color.WHITE);
        JLabel lblHints = new JLabel(Messages.get("dashboard.hint.keys"));
        lblHints.setForeground(UiTheme.PLACEHOLDER_GRAY);
        lblHints.setFont(lblHints.getFont().deriveFont(Font.PLAIN, 11f));
        statusPanel.add(lblPageInfo);
        statusPanel.add(lblHints);
        // Pager buttons (right).
        JPanel pagerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pagerButtons.setBackground(Color.WHITE);
        pagerButtons.add(btnFirst);
        pagerButtons.add(btnPrev);
        pagerButtons.add(btnNext);
        pagerButtons.add(btnLast);
        bar.add(statusPanel, BorderLayout.WEST);
        bar.add(pagerButtons, BorderLayout.EAST);
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
