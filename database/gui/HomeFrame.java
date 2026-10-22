
package database.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeFrame extends JFrame {

    public HomeFrame() {
        setTitle("BuyIt - Home");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel(
            "Welcome to BuyIt!",
            JLabel.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel subtitle = new JLabel(
            "Your shopping journey starts here.",
            JLabel.CENTER
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 17));

        JButton shopButton =
            new JButton("Customer Login / Shop");

        JButton adminButton =
            new JButton("Admin Login");

        shopButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );
        adminButton.setFont(
            new Font("Arial", Font.BOLD, 16)
        );

        shopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LoginFrame();
                dispose();
            }
        });

        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminLoginFrame();
                dispose();
            }
        });

        JPanel centerPanel =
            new JPanel(new GridLayout(2, 1, 10, 10));

        centerPanel.add(heading);
        centerPanel.add(subtitle);

        JPanel buttonPanel =
            new JPanel(new GridLayout(2, 1, 15, 15));

        buttonPanel.add(shopButton);
        buttonPanel.add(adminButton);

        JPanel mainPanel =
            new JPanel(new BorderLayout(20, 30));

        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(
                50, 60, 50, 60
            )
        );

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}
