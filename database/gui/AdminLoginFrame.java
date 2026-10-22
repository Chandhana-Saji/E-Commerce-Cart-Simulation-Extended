
package database.gui;

import database.AdminDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminLoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLoginFrame() {

        setTitle("BuyIt - Admin Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel heading = new JLabel("Admin Login");
        heading.setFont(new Font("Arial", Font.BOLD, 20));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        panel.add(heading);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(backButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginAdmin();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HomeFrame();
                dispose();
            }
        });

        setVisible(true);
    }

    private void loginAdmin() {

        String username = usernameField.getText().trim();
        String password =
            new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter username and password."
            );
            return;
        }

        AdminDAO dao = new AdminDAO();
        int adminId = dao.authenticate(username, password);

        if (adminId != -1) {
            JOptionPane.showMessageDialog(
                this, "Admin login successful!"
            );

            new AdminDashboard();
            dispose();

        } else {
            JOptionPane.showMessageDialog(
                this, "Invalid admin credentials."
            );
        }
    }
}
