
package database.gui;

import database.CustomerDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("BuyIt - Customer Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel titleLabel = new JLabel("Customer Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        emailField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(titleLabel);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginCustomer();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame();
                dispose();
            }
        });

        setVisible(true);
    }

    private void loginCustomer() {
        String email = emailField.getText().trim();
        String password =
            new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter email and password."
            );
            return;
        }

        CustomerDAO dao = new CustomerDAO();
        int customerId = dao.getCustomerId(email, password);

        if (customerId != -1) {
            JOptionPane.showMessageDialog(
                this, "Login successful!"
            );

            new CategoryFrame(customerId);
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                this, "Invalid email or password."
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }
}
