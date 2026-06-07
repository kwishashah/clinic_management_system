/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.repo.UserRepository;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignupFrame extends JDialog {
    private static final Logger logger = LogManager.getLogger(SignupFrame.class);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    private final AppContext context;
    private final UserRepository userRepo;

    public SignupFrame(AppContext context) {
        this(null, context);
    }

    public SignupFrame(Window parent, AppContext context) {
        super(parent, Messages.get("signup.dialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
        this.context = context;
        this.userRepo = context.userRepo();
        setSize(480, 340); // ~1.41 width:height ratio (matches LoginFrame)
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
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
        JLabel lblTitle = new JLabel(Messages.get("signup.header"));
        lblTitle.setFont(UiTheme.TITLE_FONT);
        lblTitle.setForeground(UiTheme.BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        content.add(lblTitle, gbc);
        // Row 1: top separator spans all 4 columns
        gbc.gridy = 1;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 2: subtitle/instruction label spans all 4 columns
        gbc.insets = new Insets(6, 8, 6, 8);
        JLabel subtitleLabel = new JLabel(Messages.get("signup.subtitle"));
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.SUBTITLE_GRAY);
        gbc.gridy = 2;
        content.add(subtitleLabel, gbc);
        // Row 3: Username
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("signup.field.username")), gbc);
        txtUsername = new JTextField();
        txtUsername.setBorder(UiTheme.BORDER);
        txtUsername.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(txtUsername, gbc);
        // Row 4: Password
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("signup.field.password")), gbc);
        txtPassword = new JPasswordField();
        txtPassword.setBorder(UiTheme.BORDER);
        txtPassword.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(txtPassword, gbc);
        // Row 5: Confirm Password
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("signup.field.confirm")), gbc);
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setBorder(UiTheme.BORDER);
        txtConfirmPassword.setPreferredSize(new Dimension(250, 28));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(txtConfirmPassword, gbc);
        // Row 6: vertical filler so form sticks to top
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(Box.createGlue(), gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Row 7: bottom separator spans all 4 columns
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 8: buttons right-aligned in cols 2 & 3; cols 0-1 absorb slack
        JButton btnBack = new JButton(Messages.get("signup.button.back"));
        btnBack.setMnemonic(KeyEvent.VK_B);
        JButton btnCreate = new JButton(Messages.get("signup.button.create"));
        btnCreate.setMnemonic(KeyEvent.VK_C);
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(Box.createHorizontalGlue(), gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 2;
        content.add(btnBack, gbc);
        gbc.gridx = 3;
        content.add(btnCreate, gbc);
        getRootPane().setDefaultButton(btnCreate);
        // ESC disposes the dialog (returns to Login).
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeSignup");
        root.getActionMap().put("closeSignup", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                logger.debug("Signup dialog dismissed via ESC");
                dispose();
            }
        });
        btnCreate.addActionListener(e -> createAccount());
        btnBack.addActionListener(e -> dispose());
    }

    private void createAccount() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirmPassword.getPassword());
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                UiTheme.showInfo(this, Messages.get("common.validation.title"), Messages.get("signup.error.required"));
                return;
            }
            if (!password.equals(confirm)) {
                UiTheme.showInfo(this, Messages.get("common.validation.title"), Messages.get("signup.error.mismatch"));
                return;
            }
            if (password.length() < 5) {
                UiTheme.showInfo(this, Messages.get("common.validation.title"), Messages.get("signup.error.length"));
                return;
            }
            if (userRepo.userExists(username)) {
                UiTheme.showInfo(this, Messages.get("signup.error.exists.title"), Messages.get("signup.error.exists"));
                return;
            }
            boolean success = userRepo.insertUser(username, password);
            int userId = userRepo.getUserId(username);
            if (success) {
                UiTheme.showInfo(this, Messages.get("signup.success.title"), Messages.get("signup.success"));
                dispose();
                new DoctorDashboard(userId, context).setVisible(true);
            } else {
                UiTheme.showInfo(this, Messages.get("common.error.title"), Messages.get("signup.error.failed"));
            }
        } catch (Exception e) {
            logger.error("Signup failed", e);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("signup.error.generic", e.getMessage()));
        }
    }
}
