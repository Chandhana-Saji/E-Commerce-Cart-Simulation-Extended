
package database.gui;

import database.CustomerDAO;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JPasswordField passwordField;

    public RegisterFrame() {

        setTitle("E-Commerce Cart - Registration");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();
        passwordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        panel.add(new JLabel("Address:"));
        panel.add(addressField);

        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        panel.add(registerButton);
        panel.add(backButton);

        add(panel);

        registerButton.addActionListener(e -> registerCustomer());

        backButton.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }

    private void registerCustomer() {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty()
                || phone.isEmpty() || address.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please fill in all fields.");
            return;
        }

        CustomerDAO dao = new CustomerDAO();

        boolean success = dao.registerCustomer(
                name, email, phone, address, password);

        if (success) {
            JOptionPane.showMessageDialog(
                    this, "Registration successful! Please login.");
            new LoginFrame();
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this, "Registration failed. Check the email or database.");
        }
    }
}

