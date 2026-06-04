/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.app.AppContext;
import com.neuro.constants.ErrorConstants;
import com.neuro.constants.MessageConstants;
import com.neuro.repo.UserRepository;
import java.awt.*;
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

    public SignupFrame(JFrame parent, AppContext context) {
        super(parent, "Create New Account", true); // modal dialog
        this.context = context;
        this.userRepo = context.userRepo();
        setSize(450, 320);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        initUI();
    }

    private void initUI() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Dimension fieldSize = new Dimension(250, 40);

        // 🔹 Title
        JLabel lblTitle = new JLabel("Create New Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        // 🔹 Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField();
        txtUsername.setPreferredSize(fieldSize);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // 🔹 Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(fieldSize);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // 🔹 Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Confirm Password:"), gbc);

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setPreferredSize(fieldSize);
        gbc.gridx = 1;
        panel.add(txtConfirmPassword, gbc);

        // 🔹 Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        JButton btnCreate = new JButton("Create Account");
        btnCreate.setPreferredSize(new Dimension(160, 40));

        JButton btnBack = new JButton("Back");
        btnBack.setPreferredSize(new Dimension(120, 40));

        btnPanel.add(btnCreate);
        btnPanel.add(btnBack);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnPanel, gbc);

        // 🔹 Actions
        btnCreate.addActionListener(e -> createAccount());
        btnBack.addActionListener(e -> dispose());

        add(panel);
    }

    private void createAccount() {
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirmPassword.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                DialogUtil.warning(this, ErrorConstants.ALL_FIELDS_REQD);
                return;
            }

            if (!password.equals(confirm)) {
                DialogUtil.warning(this, "Passwords do not match");
                return;
            }

            if (password.length() < 5) {
                DialogUtil.warning(this, ErrorConstants.PASSWORDS_LENGTH);
                return;
            }

            // ✅ CHECK FIRST (this is the missing piece)
            if (userRepo.userExists(username)) {
                DialogUtil.warning(this, ErrorConstants.USERNAME_EXISTS);
                return;
            }

            boolean success = userRepo.insertUser(username, password);
            int userId = userRepo.getUserId(username);
            if (success) {
                DialogUtil.info(this, MessageConstants.ACC_CREATED);
                dispose();
                new DoctorDashboard(userId, context).setVisible(true);
            } else {
                DialogUtil.error(this,ErrorConstants.UNABLE_TO_CREATE_ACCOUNT);
            }

        } catch (Exception e) {
            logger.error("Signup failed", e);
            DialogUtil.error(this, ErrorConstants.UNABLE_TO_CREATE_ACCOUNT);
        }
    }
}
