/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.repo.UserRepository;
import java.awt.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private static final Logger logger = LogManager.getLogger(LoginFrame.class);

    private final AppContext context;
    private final UserRepository userRepo;

    public LoginFrame(AppContext context) {
        this.context = context;
        this.userRepo = context.userRepo();
        setTitle("Clinic Login");
        setSize(450, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(250, 40));
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(250, 40));
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        // 🔹 Signup Button
        JButton btnSignup = new JButton("Create New Account");
        gbc.gridy = 3;
        panel.add(btnSignup, gbc);

        btnLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(btnLogin);

        // 🔥 THIS IS WHERE IT GOES
        btnSignup.addActionListener(e -> new SignupFrame(this, context).setVisible(true));

        add(panel);
    }

    private void login() {

        try {

            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            // empty credentials
            if (username.isEmpty() || password.isEmpty()) {
                logger.warn("Login attempted with empty credentials");
                JOptionPane.showMessageDialog(this, "Enter username and password");
                return;
            }

            // login attempt
            logger.info("Login attempt for username={}", username);

            if (userRepo.validateUser(username, password)) {

                int userId = userRepo.getUserId(username);

                // successful login
                logger.info("Login successful userId={} username={}", userId, username);

                logger.debug("Launching DoctorDashboard for userId={}", userId);

                // IMPORTANT: only once (you had it twice)
                new DoctorDashboard(userId, context).setVisible(true);

                dispose();

            } else {

                logger.warn("Login failed for username={}", username);

                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }

        } catch (Exception e) {

            logger.error("Login error for username={}", txtUsername.getText(), e);

            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
        }
    }
}
