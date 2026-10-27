
package com.cinematch.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.mindrot.jbcrypt.BCrypt;

import com.cinematch.util.DatabaseConnection;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame() {
        setTitle("SoWhatsNext - Create Account");
        setSize(450, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color background = new Color(22, 22, 30);
        Color accent = new Color(229, 9, 20);
        Color white = Color.WHITE;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel title = new JLabel("Create Your Account",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(accent);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        addField(panel, gbc, "Username", usernameField, 1, white);
        addField(panel, gbc, "Email", emailField, 2, white);
        addField(panel, gbc, "Mobile Number", phoneField, 3, white);
        addField(panel, gbc, "Password", passwordField, 4, white);
        addField(panel, gbc, "Confirm Password",
                confirmPasswordField, 5, white);

        JButton registerButton = new JButton("Create Account");
        registerButton.setBackground(accent);
        registerButton.setForeground(white);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        JButton backButton = new JButton("Back to Login");
        backButton.setBackground(background);
        backButton.setForeground(white);
        backButton.setFocusPainted(false);

        gbc.gridy = 7;
        panel.add(backButton, gbc);

        registerButton.addActionListener(e -> registerUser());
        backButton.addActionListener(e -> dispose());

        setContentPane(panel);
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          String labelText, JComponent field,
                          int row, Color textColor) {

        JLabel label = new JLabel(labelText);
        label.setForeground(textColor);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void registerUser() {

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        String password = new String(passwordField.getPassword());
        String confirmPassword =
                new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty()
                || (email.isEmpty() && phone.isEmpty())) {

            JOptionPane.showMessageDialog(this,
                    "Enter username, password and either email or mobile.");
            return;
        }

        if (!email.isEmpty()
                && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid email address.");
            return;
        }

        if (!phone.isEmpty()
                && !phone.matches("\\+?[0-9]{10,15}")) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid mobile number (10-15 digits).");
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.");
            return;
        }

        String hashedPassword =
                BCrypt.hashpw(password, BCrypt.gensalt(12));

        String sql = """
                INSERT INTO users
                    (username, email, phone_number, password_hash)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2,
                    email.isEmpty() ? null : email);
            statement.setString(3,
                    phone.isEmpty() ? null : phone);
            statement.setString(4, hashedPassword);

            statement.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Account created successfully!");

            dispose();

        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                JOptionPane.showMessageDialog(this,
                        "Username, email or mobile number already exists.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration failed. Check your database setup.");
                ex.printStackTrace();
            }
        }
    }
}
