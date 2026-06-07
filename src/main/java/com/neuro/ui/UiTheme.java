/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Centralized look-and-feel constants for all dialogs/frames in the application.
 * Keeping these in one place ensures every window picks up theme changes automatically
 * and eliminates duplicated magic numbers (color literals, font tuples) across the UI layer.
 */
public final class UiTheme {
    /** Primary brand color used for headers, separators, and accents. */
    public static final Color BRAND = new Color(13, 71, 161);

    /** Standard background color for dialogs and form panels. */
    public static final Color BG_WHITE = Color.WHITE;

    /** Subtle gray used for subtitle/instructional text. */
    public static final Color SUBTITLE_GRAY = new Color(90, 90, 90);

    /** Lighter gray used for placeholder/customizable hints (e.g. unset clinic header). */
    public static final Color PLACEHOLDER_GRAY = new Color(140, 140, 140);

    /** Title font used for the bold header label in dialogs. */
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    /** Subtitle font used for the descriptive line under the header. */
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    /** Thickness in pixels of the brand-colored horizontal separators. */
    public static final int SEPARATOR_THICKNESS = 2;

    public static final Border BORDER = BorderFactory.createLineBorder(BRAND);

    private UiTheme() {
        // Constants holder; not instantiable.
    }

    /**
     * Creates a horizontal {@link JSeparator} painted in the brand color at the standard
     * thickness. Used as the top divider under a header and the bottom divider above action
     * button rows.
     */
    public static JSeparator newBrandSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BRAND);
        sep.setBackground(BRAND);
        sep.setPreferredSize(new Dimension(0, SEPARATOR_THICKNESS));
        return sep;
    }

    /**
     * Shows a small APPLICATION_MODAL info dialog styled with the brand header, separator and
     * a single Close button. Use this in place of {@link javax.swing.JOptionPane#showMessageDialog}
     * when a themed look is desired.
     */
    public static void showInfo(java.awt.Window parent, String title, String message) {
        JDialog dialog = new JDialog(parent, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        Container content = dialog.getContentPane();
        content.setBackground(BG_WHITE);
        content.setLayout(new java.awt.GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        // Row 0: title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(titleLabel, gbc);
        // Row 2: message
        gbc.insets = new java.awt.Insets(10, 8, 10, 8);
        JLabel msgLabel = new JLabel("<html><body style='width:280px'>" + message + "</body></html>");
        msgLabel.setFont(SUBTITLE_FONT.deriveFont(13f));
        msgLabel.setForeground(new Color(33, 33, 33));
        gbc.gridy = 2;
        content.add(msgLabel, gbc);
        // Row 3: bottom separator
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.gridy = 3;
        content.add(newBrandSeparator(), gbc);
        // Row 4: Close button right-aligned
        JButton btnClose = new JButton("Close");
        btnClose.setMnemonic(java.awt.event.KeyEvent.VK_C);
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel buttonRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        buttonRow.setBackground(BG_WHITE);
        buttonRow.add(btnClose);
        gbc.gridy = 4;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        content.add(buttonRow, gbc);
        dialog.getRootPane().setDefaultButton(btnClose);
        // ESC closes the dialog.
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeInfo");
        root.getActionMap().put("closeInfo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Shows a small APPLICATION_MODAL confirmation dialog styled with the brand header,
     * separator and Yes/No buttons. Returns {@code true} when the user confirms (Yes),
     * {@code false} otherwise (No / ESC / close).
     */
    public static boolean showConfirm(java.awt.Window parent, String title, String message) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog(parent, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container content = dialog.getContentPane();
        content.setBackground(BG_WHITE);
        content.setLayout(new java.awt.GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        // Row 0: title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(titleLabel, gbc);
        // Row 2: message
        gbc.insets = new java.awt.Insets(10, 8, 10, 8);
        JLabel msgLabel = new JLabel("<html><body style='width:280px'>" + message + "</body></html>");
        msgLabel.setFont(SUBTITLE_FONT.deriveFont(13f));
        msgLabel.setForeground(new Color(33, 33, 33));
        gbc.gridy = 2;
        content.add(msgLabel, gbc);
        // Row 3: bottom separator
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.gridy = 3;
        content.add(newBrandSeparator(), gbc);
        // Row 4: Yes / No buttons right-aligned
        JButton btnYes = new JButton("Yes");
        btnYes.setMnemonic(java.awt.event.KeyEvent.VK_Y);
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        JButton btnNo = new JButton("No");
        btnNo.setMnemonic(java.awt.event.KeyEvent.VK_N);
        btnNo.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        JPanel buttonRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(BG_WHITE);
        buttonRow.add(btnYes);
        buttonRow.add(btnNo);
        gbc.gridy = 4;
        gbc.anchor = java.awt.GridBagConstraints.EAST;
        content.add(buttonRow, gbc);
        dialog.getRootPane().setDefaultButton(btnNo);
        // ESC cancels the dialog (treated as No).
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "cancelConfirm");
        root.getActionMap().put("cancelConfirm", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                result[0] = false;
                dialog.dispose();
            }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
}
