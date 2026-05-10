package com.neuro.ui;

import com.neuro.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private static final Logger logger =
            LoggerFactory.getLogger(LoginFrame.class);
    public LoginFrame() {
        setTitle("Clinic Login");
        setSize(450, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(250, 40));
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(250, 40));
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        // 🔹 Signup Button
        JButton btnSignup = new JButton("Create New Account");
        gbc.gridy = 3;
        panel.add(btnSignup, gbc);

        btnLogin.addActionListener(e -> login());

        // 🔥 THIS IS WHERE IT GOES
        btnSignup.addActionListener(e ->
                new SignupFrame(this).setVisible(true)
        );

        add(panel);
    }

    private void login() {

        try {

            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            // empty credentials
            if(username.isEmpty() || password.isEmpty()){
                logger.warn("Login attempted with empty credentials");
                JOptionPane.showMessageDialog(this,"Enter username and password");
                return;
            }

            // login attempt
            logger.info("Login attempt for username={}", username);

            if (UserDAO.validateUser(username, password)) {

                int userId = UserDAO.getUserId(username);

                // successful login
                logger.info(
                        "Login successful userId={} username={}",
                        userId,
                        username
                );

                logger.debug(
                        "Launching DoctorDashboard for userId={}",
                        userId
                );

                // IMPORTANT: only once (you had it twice)
                new DoctorDashboard(userId).setVisible(true);

                dispose();

            } else {

                logger.warn(
                        "Login failed for username={}",
                        username
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid credentials"
                );
            }

        } catch (Exception e) {

            logger.error(
                    "Login error for username={}",
                    txtUsername.getText(),
                    e
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Login error: " + e.getMessage()
            );
        }
    }
}