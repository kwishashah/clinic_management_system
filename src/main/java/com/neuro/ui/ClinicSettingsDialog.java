/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClinicSettingsDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(ClinicSettingsDialog.class);

    private static final int PREVIEW_LOGO_W = 120;
    private static final int PREVIEW_LOGO_H = 60;

    private JTextField nameField;
    private JLabel logoFileLabel;
    private JButton clearLogoBtn;
    private JLabel previewLogo;
    private JLabel previewName;
    private String logoPath;

    public ClinicSettingsDialog(Window owner) {
        super(owner, Messages.get("clinic.dialog.title"), ModalityType.APPLICATION_MODAL);
        logger.info("Opening Clinic Settings dialog");
        setSize(650, 460); // ~1.41 width:height ratio
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        loadExisting();
        refreshPreview();
    }

    private void initUI() {
        Container content = getContentPane();
        content.setBackground(UiTheme.BG_WHITE);
        content.setLayout(new GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Row 0: header label spans all 4 columns
        JLabel headerLabel = new JLabel(Messages.get("clinic.header"));
        headerLabel.setFont(UiTheme.TITLE_FONT);
        headerLabel.setForeground(UiTheme.BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        content.add(headerLabel, gbc);
        // Row 1: top separator spans all 4 columns
        gbc.gridy = 1;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 2: subtitle/instruction label spans all 4 columns
        gbc.insets = new Insets(6, 8, 6, 8);
        JLabel subtitleLabel = new JLabel(Messages.get("clinic.subtitle"));
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.SUBTITLE_GRAY);
        gbc.gridy = 2;
        content.add(subtitleLabel, gbc);
        // Row 3: Clinic Name label (col 0) + text field (cols 1-3)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("clinic.field.name")), gbc);
        nameField = new JTextField();
        nameField.setBorder(UiTheme.BORDER);
        nameField.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(nameField, gbc);
        // Row 4: Logo label (col 0) + upload button (col 1) + filename (cols 2-3)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("clinic.field.logo")), gbc);
        JButton uploadBtn = new JButton(Messages.get("clinic.button.upload"));
        uploadBtn.setMnemonic(KeyEvent.VK_U);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        content.add(uploadBtn, gbc);
        logoFileLabel = new JLabel(Messages.get("clinic.logo.none"));
        logoFileLabel.setForeground(UiTheme.SUBTITLE_GRAY);
        clearLogoBtn = new JButton("\u2715"); // ✕
        clearLogoBtn.setToolTipText(Messages.get("clinic.logo.clear.tooltip"));
        clearLogoBtn.setMargin(new Insets(0, 6, 0, 6));
        clearLogoBtn.setFocusable(false);
        clearLogoBtn.setForeground(new Color(180, 40, 40));
        clearLogoBtn.setVisible(false); // shown only when a logo is present
        clearLogoBtn.addActionListener(e -> clearLogo());
        JPanel logoFileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoFileRow.setOpaque(false);
        logoFileRow.add(logoFileLabel);
        logoFileRow.add(clearLogoBtn);
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(logoFileRow, gbc);
        // Row 5: Preview label (col 0) + preview panel (cols 1-3)
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        content.add(new JLabel(Messages.get("clinic.field.preview")), gbc);
        JPanel preview = buildPreviewPanel();
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(preview, gbc);
        // Row 6: vertical glue so Save row stays at the bottom
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(Box.createGlue(), gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Row 7: separator across all 4 columns (visual divider above the Save button)
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        content.add(UiTheme.newDividerSeparator(), gbc);
        // Row 8: Save button right-aligned in col 3; cols 0-2 absorb slack
        JButton saveBtn = new JButton(Messages.get("clinic.button.save"));
        saveBtn.setMnemonic(KeyEvent.VK_S); // Alt+S on Win/Linux
        saveBtn.setToolTipText(Messages.get("clinic.button.save.tooltip"));
        UiTheme.asPrimary(saveBtn);
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(Box.createHorizontalGlue(), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        content.add(saveBtn, gbc);
        getRootPane().setDefaultButton(saveBtn);
        // ESC disposes the dialog without saving.
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        root.getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                logger.debug("Clinic Settings dialog dismissed via ESC");
                dispose();
            }
        });
        // ================= ACTIONS =================
        uploadBtn.addActionListener(e -> chooseLogo());
        saveBtn.addActionListener(e -> save());
        // Live preview when the user edits the clinic name.
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshPreviewName(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshPreviewName(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshPreviewName(); }
        });
    }

    private JPanel buildPreviewPanel() {
        JPanel preview = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        preview.setBackground(Color.WHITE);
        preview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BRAND),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        previewLogo = new JLabel();
        previewLogo.setPreferredSize(new Dimension(PREVIEW_LOGO_W, PREVIEW_LOGO_H));
        previewLogo.setHorizontalAlignment(SwingConstants.CENTER);
        previewLogo.setVerticalAlignment(SwingConstants.CENTER);
        previewLogo.setBorder(BorderFactory.createDashedBorder(UiTheme.BRAND));
        previewName = new JLabel(Messages.get("clinic.preview.name"));
        previewName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        previewName.setForeground(UiTheme.BRAND);
        preview.add(previewLogo);
        preview.add(previewName);
        return preview;
    }

    private void refreshPreview() {
        refreshPreviewName();
        refreshPreviewLogo();
    }

    /** Clears the currently selected logo (does not delete the file on disk). */
    private void clearLogo() {
        logger.info("Clearing selected logo path={}", logoPath);
        logoPath = null;
        logoFileLabel.setText(Messages.get("clinic.logo.none"));
        clearLogoBtn.setVisible(false);
        refreshPreviewLogo();
    }

    private void refreshPreviewName() {
        String name = nameField.getText().trim();
        previewName.setText(name.isEmpty() ? Messages.get("clinic.preview.name") : name);
        previewName.setForeground(name.isEmpty() ? Color.GRAY : UiTheme.BRAND);
    }

    private void refreshPreviewLogo() {
        if (logoPath == null || logoPath.isEmpty() || !new File(logoPath).isFile()) {
            previewLogo.setIcon(null);
            previewLogo.setText(Messages.get("clinic.preview.noLogo"));
            previewLogo.setForeground(Color.GRAY);
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(logoPath);
            Image img = icon.getImage().getScaledInstance(PREVIEW_LOGO_W, PREVIEW_LOGO_H, Image.SCALE_SMOOTH);
            previewLogo.setText(null);
            previewLogo.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            logger.warn("Failed to render logo preview path={}", logoPath, e);
            previewLogo.setIcon(null);
            previewLogo.setText(Messages.get("clinic.preview.invalid"));
        }
    }

    private void chooseLogo() {
        logger.info("Logo upload initiated");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                Messages.get("clinic.filechooser.filter"), "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            logger.info("User selected logo file={}", file.getAbsolutePath());
            String path = file.getAbsolutePath().toLowerCase();
            if (!(path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"))) {
                logger.warn("Invalid logo format selected={}", file.getName());
                UiTheme.showInfo(this, Messages.get("clinic.error.image.title"),
                        Messages.get("clinic.error.image.message"));
                return;
            }
            try {
                logoPath = file.getAbsolutePath();
                logoFileLabel.setText(file.getName());
                clearLogoBtn.setVisible(true);
                refreshPreviewLogo();
                logger.info("Logo preview loaded successfully path={}", logoPath);
            } catch (Exception e) {
                logger.error("Failed loading logo preview", e);
                UiTheme.showInfo(this, Messages.get("common.error.title"), Messages.get("clinic.error.load"));
            }
        } else {
            logger.debug("Logo upload cancelled by user");
        }
    }

    private void save() {
        try {
            String clinicName = nameField.getText().trim();
            if (clinicName.isEmpty()) {
                logger.warn("Save blocked: clinic name empty");
                UiTheme.showInfo(this,
                        Messages.get("common.validation.title"),
                        Messages.get("clinic.validation.name"));
                return;
            }
            logger.info("Saving clinic settings name={} logo={}", clinicName, logoPath);
            ClinicInfo info = new ClinicInfo(clinicName, logoPath);
            ClinicConfig.save(info);
            logger.info("Clinic settings saved successfully");
            UiTheme.showInfo(this, Messages.get("common.saved.title"), Messages.get("clinic.success"));
            dispose();
        } catch (Exception e) {
            logger.error("Clinic settings save failed", e);
            UiTheme.showInfo(this, Messages.get("common.error.title"), Messages.get("clinic.error.save"));
        }
    }

    private void loadExisting() {
        try {
            logger.debug("Loading existing clinic settings");
            ClinicInfo info = ClinicConfig.load();
            if (info != null) {
                nameField.setText(info.getName());
                logoPath = info.getLogoPath();
                logger.info("Loaded clinic settings name={} logo={}", info.getName(), logoPath);
                if (logoPath != null && !logoPath.isEmpty()) {
                    File file = new File(logoPath);
                    logoFileLabel.setText(file.getName());
                    clearLogoBtn.setVisible(true);
                }
            }
        } catch (Exception e) {
            logger.error("Failed loading clinic settings", e);
        }
    }
}
