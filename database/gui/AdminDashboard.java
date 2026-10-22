
package database.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {

        setTitle("BuyIt - Admin Dashboard");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel(
            "Admin Dashboard", JLabel.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JButton productsButton =
            new JButton("Product Management");

        JButton ordersButton =
            new JButton("View Orders");

        JButton logoutButton =
            new JButton("Logout");

        productsButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        ordersButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        logoutButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        // Open Product Management
        productsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminProductFrame();
                dispose();
            }
        });

        // Open Order Management
        ordersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminOrderFrame();
                dispose();
            }
        });

        // Return to the Home screen
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HomeFrame();
                dispose();
            }
        });

        JPanel buttons = new JPanel(
            new GridLayout(3, 1, 15, 15)
        );

        buttons.setBorder(
            BorderFactory.createEmptyBorder(30, 60, 30, 60)
        );

        buttons.add(productsButton);
        buttons.add(ordersButton);
        buttons.add(logoutButton);

        setLayout(new BorderLayout(20, 20));

        add(heading, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);

        setVisible(true);
    }
}
