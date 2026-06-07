/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.repo.UserRepository;
import com.neuro.ui.i18n.Messages;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginFrame extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private static final Logger logger = LogManager.getLogger(LoginFrame.class);

    private final AppContext context;
    private final UserRepository userRepo;

    public LoginFrame(AppContext context) {
        super((Frame) null, Messages.get("login.dialog.title"), false);
        this.context = context;
        this.userRepo = context.userRepo();
        setSize(480, 340); // ~1.41 width:height ratio
        setResizable(false); // disables maximize/zoom; keeps a fixed-size dialog (no minimize button on most OS L&Fs)
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Closing the login dialog (user clicks the X) should terminate the app,
        // since this is the entry-point window. Programmatic dispose() after a
        // successful login does NOT fire windowClosing, so the dashboard stays open.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("Login dialog closed by user; exiting application");
                System.exit(0);
            }
        });
        Container content = getContentPane();
        content.setBackground(UiTheme.BG_WHITE);
        content.setLayout(new GridBagLayout());
        ((JComponent) content).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        // Row 0: "Login" header label spans all 4 columns
        JLabel loginLabel = new JLabel(Messages.get("login.header"));
        loginLabel.setFont(UiTheme.TITLE_FONT);
        loginLabel.setForeground(UiTheme.BRAND);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        content.add(loginLabel, gbc);
        // Row 1: top separator spans all 4 columns
        gbc.gridy = 1;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 2: subtitle/instruction label spans all 4 columns
        gbc.insets = new Insets(6, 8, 6, 8);
        JLabel subtitleLabel = new JLabel(Messages.get("login.subtitle"));
        subtitleLabel.setFont(UiTheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UiTheme.SUBTITLE_GRAY);
        gbc.gridy = 2;
        content.add(subtitleLabel, gbc);
        // Row 3: Username label (col 0) + text field (cols 1-3)
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("login.field.username")), gbc);
        txtUsername = new JTextField();
        txtUsername.setBorder(UiTheme.BORDER);
        txtUsername.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(txtUsername, gbc);
        // Row 4: Password label (col 0) + password field (cols 1-3)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JLabel(Messages.get("login.field.password")), gbc);
        txtPassword = new JPasswordField();
        txtPassword.setBorder(UiTheme.BORDER);
        txtPassword.setPreferredSize(new Dimension(250, 32));
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        content.add(txtPassword, gbc);
        // Row 5: vertical filler so form sticks to top
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(Box.createGlue(), gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Row 6: bottom separator spans all 4 columns
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        content.add(UiTheme.newBrandSeparator(), gbc);
        // Row 7: buttons right-aligned in cols 2 & 3; cols 0-1 absorb slack
        JButton btnSignup = new JButton(Messages.get("login.button.signup"));
        btnSignup.setMnemonic(java.awt.event.KeyEvent.VK_N);
        JButton btnLogin = new JButton(Messages.get("login.button.login"));
        btnLogin.setMnemonic(java.awt.event.KeyEvent.VK_L);
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(Box.createHorizontalGlue(), gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 2;
        content.add(btnSignup, gbc);
        gbc.gridx = 3;
        content.add(btnLogin, gbc);
        getRootPane().setDefaultButton(btnLogin);
        // ESC closes the login dialog (routed through windowClosing so the app exits cleanly).
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeLogin");
        root.getActionMap().put("closeLogin", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispatchEvent(new WindowEvent(LoginFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnLogin.addActionListener(e -> login());
        btnSignup.addActionListener(e -> new SignupFrame(this, context).setVisible(true));
    }

    private void login() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            // empty credentials
            if (username.isEmpty() || password.isEmpty()) {
                logger.warn("Login attempted with empty credentials");
                UiTheme.showInfo(this, Messages.get("common.validation.title"), Messages.get("login.error.empty"));
                return;
            }
            // login attempt
            logger.info("Login attempt for username={}", username);
            if (userRepo.validateUser(username, password)) {
                int userId = userRepo.getUserId(username);
                // successful login
                logger.info("Login successful userId={} username={}", userId, username);
                logger.debug("Launching DoctorDashboard for userId={}", userId);
                new DoctorDashboard(userId, context).setVisible(true);
                dispose();
            } else {
                logger.warn("Login failed for username={}", username);
                UiTheme.showInfo(this,
                        Messages.get("login.error.failed.title"),
                        Messages.get("login.error.invalid"));
            }
        } catch (Exception e) {
            logger.error("Login error for username={}", txtUsername.getText(), e);
            UiTheme.showInfo(this,
                    Messages.get("common.error.title"),
                    Messages.format("login.error.generic", e.getMessage()));
        }
    }
}
