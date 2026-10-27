
package com.cinematch.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("SoWhatsNext - Login");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color background = new Color(22, 22, 30);
        Color accent = new Color(229, 9, 20);
        Color white = Color.WHITE;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 5, 9, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel title = new JLabel("SoWhatsNext", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(accent);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel(
                "Your Movie Universe", SwingConstants.CENTER);
        subtitle.setForeground(white);

        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        addField(panel, gbc, "Username", usernameField, 2, white);
        addField(panel, gbc, "Password", passwordField, 3, white);

        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(accent);
        loginButton.setForeground(white);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        JButton registerButton = new JButton("Create Account");
        registerButton.setBackground(background);
        registerButton.setForeground(white);
        registerButton.setFocusPainted(false);

        gbc.gridy = 5;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> loginUser());

        passwordField.addActionListener(e -> loginUser());

        registerButton.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame();
            registerFrame.setVisible(true);
        });

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

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password.");
            return;
        }

        String sql =
                "SELECT password_hash FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String storedHash = result.getString("password_hash");

                    if (BCrypt.checkpw(password, storedHash)) {
                        HomeFrame homeFrame = new HomeFrame(username);
                        homeFrame.setVisible(true);
                        dispose();
                    } else {
                        showInvalidLogin();
                    }
                } else {
                    showInvalidLogin();
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database connection failed. Please try again.");
            ex.printStackTrace();
        }
    }

    private void showInvalidLogin() {
        JOptionPane.showMessageDialog(this,
                "Invalid username or password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
    }
}
