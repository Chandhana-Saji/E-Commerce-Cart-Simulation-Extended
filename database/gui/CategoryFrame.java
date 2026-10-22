
package database.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CategoryFrame extends JFrame {

    private int customerId;

    public CategoryFrame(int customerId) {
        this.customerId = customerId;

        setTitle("BuyIt - Categories");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(248, 247, 255));
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JLabel heading = new JLabel(
            "Explore Our Categories", JLabel.CENTER
        );
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(new Color(111, 76, 219));

        JPanel categoryPanel = new JPanel(
            new GridLayout(1, 3, 20, 20)
        );
        categoryPanel.setOpaque(false);

        JButton clothingButton = new JButton("Clothing");
        JButton footwearButton = new JButton("Footwear");
        JButton gadgetsButton = new JButton("Gadgets");

        JButton[] buttons = {
            clothingButton, footwearButton, gadgetsButton
        };

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setFont(new Font("Arial", Font.BOLD, 18));
            buttons[i].setBackground(Color.WHITE);
            buttons[i].setFocusPainted(false);
            categoryPanel.add(buttons[i]);
        }

        clothingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openCategory("Clothing");
            }
        });

        footwearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openCategory("Footwear");
            }
        });

        gadgetsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openCategory("Gadgets");
            }
        });

        JButton backButton = new JButton("Back to Home");

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HomeFrame();
                dispose();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);

        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(categoryPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void openCategory(String category) {
        new ProductFrame(customerId, category);
        dispose();
    }
}
