/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

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

    /** Subtle background used to indicate hover state on flat/borderless controls (e.g. custom tabs). */
    public static final Color HOVER_BG = BRAND;

    /** Light gray divider line used under tab bars and similar horizontal separators. */
    public static final Color DIVIDER = new Color(0xD0D0D0);

    /** Title font used for the bold header label in dialogs. */
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    /** Subtitle font used for the descriptive line under the header. */
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    /** Thickness in pixels of the brand-colored horizontal separators. */
    public static final int SEPARATOR_THICKNESS = 2;

    public static final Border BORDER = BorderFactory.createLineBorder(BRAND);

    /**
     * Standard input field border: brand line + 2-px vertical / 4-px horizontal inset so
     * text doesn't hug the border. Use via {@link #styleField(JTextComponent)} or
     * {@link #styleSpinner(JSpinner)} so every form looks the same.
     */
    public static final Border FIELD_BORDER =
            BorderFactory.createCompoundBorder(BORDER, BorderFactory.createEmptyBorder(2, 4, 2, 4));
    /** Red border used to indicate invalid input. */
    public static final Border ERROR_BORDER =
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xC62828)),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4));
    /** Standard danger/destructive button color (used by {@link #asDanger(JButton)}). */
    public static final Color DANGER = new Color(0xC62828);

    /** Hover background used for primary buttons ({@link #asPrimary(JButton)}). */
    public static final Color BRAND_HOVER = new Color(0x1565C0);

    /** Hover background used for danger buttons ({@link #asDanger(JButton)}). */
    public static final Color DANGER_HOVER = new Color(0xD32F2F);

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
     * Creates a low-emphasis light-gray horizontal {@link JSeparator}, colored from
     * {@link #DIVIDER}. Use this for the divider that sits above an action-button row
     * (Save / Close / Cancel etc.) where a brand-colored line would feel too heavy. The
     * brand separator stays reserved for the top-of-dialog header divider.
     */
    public static JSeparator newDividerSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);
        sep.setPreferredSize(new Dimension(0, 2));
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
        // Row 3: bottom separator (subtle gray, above the Close button)
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.gridy = 3;
        content.add(newDividerSeparator(), gbc);
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
        // Row 3: bottom separator (subtle gray, above the Yes/No row)
        gbc.insets = new java.awt.Insets(0, 8, 6, 8);
        gbc.gridy = 3;
        content.add(newDividerSeparator(), gbc);
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
    /**
     * Highlights a text field as invalid.
     */
    public static void showFieldError(JTextComponent component) {
        component.setBorder(ERROR_BORDER);
    }

    /**
     * Restores the standard field styling.
     */
    public static void clearFieldError(JTextComponent component) {
        component.setBorder(FIELD_BORDER);
    }

    /**
     * Applies or removes the error border depending on whether the text
     * contains only digits.
     */
    public static void validateNumericField(JTextComponent component) {
        String text = component.getText();

        if (text == null || text.isEmpty() || text.matches("\\d*")) {
            clearFieldError(component);
        } else {
            showFieldError(component);
        }
    }
    public static void attachNumericValidation(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            private void validateField() {
                if (field.getText().matches("\\d*")) {
                    clearFieldError(field);
                } else {
                    showFieldError(field);
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateField();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateField();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateField();
            }
        });
    }
    // ================= FIELD / SPINNER STYLING =================

    /**
     * Applies the standard {@link #FIELD_BORDER} and white background to a text component
     * (works for {@link JTextField} and {@link JTextArea}).
     */
    public static void styleField(JTextComponent component) {
        component.setBorder(FIELD_BORDER);
        component.setBackground(BG_WHITE);
    }

    /**
     * Applies the standard {@link #FIELD_BORDER} and white background to a {@link JSpinner}'s
     * editor text field (the spinner's own border wraps editor + arrow buttons, so we style
     * the editor instead so the result visually matches plain text fields).
     */
    public static void styleSpinner(JSpinner spinner) {
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setBorder(FIELD_BORDER);
            editor.getTextField().setBackground(BG_WHITE);
        }
    }

    // ================= BUTTON STYLING =================

    /**
     * Styles a button as a brand-colored primary call-to-action (white text on {@link #BRAND}).
     * Use for the single most important action on a form (Save, Add, Update, Export PDF).
     */
    public static void asPrimary(JButton button) {
        styleSolidButton(button, BRAND, BRAND_HOVER);
    }

    /**
     * Styles a button as a destructive action (white text on {@link #DANGER}, brightening to
     * {@link #DANGER_HOVER} on mouse-over). Use sparingly for delete / remove confirmations.
     */
    public static void asDanger(JButton button) {
        styleSolidButton(button, DANGER, DANGER_HOVER);
    }

    /**
     * Shared implementation for {@link #asPrimary(JButton)} / {@link #asDanger(JButton)}:
     * paints the button as a flat, opaque, white-on-{@code base} rectangle and brightens the
     * background to {@code hover} while the mouse is over the button (and the button is
     * enabled). The hover listener also re-applies the base color on disable so the visual
     * state always matches the model.
     */
    private static void styleSolidButton(JButton button, Color base, Color hover) {
        button.setBackground(base);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        // Drive hover state off the button model so keyboard rollover (e.g. via mnemonic focus)
        // produces the same highlight as a mouse hover, and disabled buttons don't light up.
        button.getModel().addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (!m.isEnabled()) {
                button.setBackground(base);
            } else if (m.isPressed()) {
                // A slightly darker shade on press would be nicer; keep base for simplicity.
                button.setBackground(base);
            } else if (m.isRollover()) {
                button.setBackground(hover);
            } else {
                button.setBackground(base);
            }
        });
    }

    // ================= TABLE STYLING =================

    /**
     * Applies the standard branded table header: BRAND background, white bold text, opaque
     * (so the color paints on macOS/Aqua L&F). Row styling (height, zebra, renderers) is
     * intentionally left to the caller because tables differ in row needs.
     */
    public static void brandTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(BRAND);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
    }

    // ================= DIALOG HEADER =================

    /**
     * Adds the standard dialog header (title in brand title font, brand separator, subtitle in
     * subtitle gray) as three sequential rows of a {@link GridBagLayout} container starting at
     * {@code startY}. Returns the next free row index ({@code startY + 3}) so callers can chain.
     * <p>Temporarily overrides {@code gbc.gridwidth} and {@code gbc.insets}, restoring them
     * before return.
     */
    public static int addDialogHeader(Container content, GridBagConstraints gbc, int startY,
            String title, String subtitle) {
        int oldGridWidth = gbc.gridwidth;
        Insets oldInsets = gbc.insets;
        // Row 0: title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BRAND);
        gbc.gridx = 0;
        gbc.gridy = startY;
        gbc.gridwidth = 4;
        content.add(titleLabel, gbc);
        // Row 1: brand separator
        gbc.gridy = startY + 1;
        content.add(newBrandSeparator(), gbc);
        // Row 2: subtitle (with a small top inset)
        gbc.insets = new Insets(6, oldInsets.left, oldInsets.bottom, oldInsets.right);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(SUBTITLE_GRAY);
        gbc.gridy = startY + 2;
        content.add(subtitleLabel, gbc);
        // Restore caller's gbc state.
        gbc.gridwidth = oldGridWidth;
        gbc.insets = oldInsets;
        return startY + 3;
    }

    /**
     * Builds a single branded "table-header"-style label cell (brand background, white bold,
     * 4/8 padding). Used by grid layouts that need a header row outside of a {@link JTable}
     * (e.g. the pain-matrix grids in session forms and patient-details dialogs).
     */
    public static JLabel brandHeaderLabel(String text) {
        JLabel cell = new JLabel(text, SwingConstants.CENTER);
        cell.setOpaque(true);
        cell.setBackground(BRAND);
        cell.setForeground(Color.WHITE);
        cell.setFont(cell.getFont().deriveFont(Font.BOLD));
        cell.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return cell;
    }

    // ================= TABLE ACTION BUTTON COLUMN =================

    /** Padding around a {@code View}-style button when rendered inside a table cell. */
    private static final int TABLE_BUTTON_INSET = 5;

    /** Wraps a button in an opaque panel with {@link #TABLE_BUTTON_INSET} padding on every edge. */
    private static JPanel wrapButtonInCell(JButton button, Color background) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(background);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
                TABLE_BUTTON_INSET, TABLE_BUTTON_INSET, TABLE_BUTTON_INSET, TABLE_BUTTON_INSET));
        wrapper.add(button, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Returns a {@link TableCellRenderer} that paints the cell as a flat button bearing the
     * given fixed {@code label}, with brand-matching padding. Cell values are ignored, so the
     * underlying model can store arbitrary payload (e.g. a raw pain string) without changing
     * the visible label.
     */
    public static TableCellRenderer viewButtonRenderer(String label) {
        return new ButtonCellRenderer(label);
    }

    /**
     * Returns a {@link TableCellEditor} that turns a click on the action cell into a call to
     * {@code onClick.accept(rowIndex)}. The button always shows the fixed {@code label}; the
     * caller is expected to read any payload from the model using the row index.
     */
    public static TableCellEditor viewButtonEditor(String label, IntConsumer onClick) {
        return new ButtonCellEditor(label, onClick);
    }

    /** Internal renderer used by {@link #viewButtonRenderer(String)}. */
    private static final class ButtonCellRenderer implements TableCellRenderer {
        private final JButton button = new JButton();
        private final JPanel wrapper;
        ButtonCellRenderer(String label) {
            button.setText(label);
            button.setFocusable(false);
            button.setMargin(new Insets(2, 8, 2, 8));
            wrapper = wrapButtonInCell(button, Color.WHITE);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return wrapper;
        }
    }

    /** Internal editor used by {@link #viewButtonEditor(String, IntConsumer)}. */
    private static final class ButtonCellEditor extends DefaultCellEditor {
        private final JButton button;
        private final JPanel wrapper;
        private final IntConsumer onClick;
        private int currentRow = -1;
        ButtonCellEditor(String label, IntConsumer onClick) {
            super(new JCheckBox());
            this.onClick = onClick;
            this.button = new JButton(label);
            button.setFocusable(false);
            button.setMargin(new Insets(2, 8, 2, 8));
            this.wrapper = wrapButtonInCell(button, Color.WHITE);
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
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return wrapper;
        }
        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    // ================= CUSTOM TAB BUTTON =================

    /**
     * Builds an IntelliJ-style custom tab button: flat background, brand underline when
     * selected, brand bold text when selected, hand cursor on hover. Place these in a
     * {@link ButtonGroup} so exactly one is selected at a time and pair with a
     * {@link java.awt.CardLayout} for tab switching.
     */
    public static JToggleButton createTabButton(String title) {
        final int underlineThickness = 2;
        JToggleButton tab = new JToggleButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background: same as form bg whether selected or not; hover gets a tint.
                if (getModel().isRollover() && !isSelected()) {
                    g2.setColor(HOVER_BG);
                } else {
                    g2.setColor(BG_WHITE);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Selection underline along the bottom edge.
                if (isSelected()) {
                    g2.setColor(BRAND);
                    g2.fillRect(0, getHeight() - underlineThickness, getWidth(), underlineThickness);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tab.setFont(tab.getFont().deriveFont(Font.PLAIN, 14f));
        tab.setForeground(SUBTITLE_GRAY);
        tab.setFocusPainted(false);
        tab.setBorderPainted(false);
        tab.setContentAreaFilled(false);
        tab.setOpaque(false);
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tab.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        tab.addChangeListener(e -> {
            tab.setForeground(tab.isSelected() ? BRAND : SUBTITLE_GRAY);
            tab.setFont(tab.getFont().deriveFont(tab.isSelected() ? Font.BOLD : Font.PLAIN, 14f));
        });
        return tab;
    }
}
