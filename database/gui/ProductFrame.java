
package database.gui;

import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProductFrame extends JFrame {

    private int customerId;
    private String category;
    private JPanel productsPanel;

    public ProductFrame(int customerId, String category) {

        this.customerId = customerId;
        this.category = category;

        setTitle("BuyIt - " + category + " Products");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                category + " Collection", JLabel.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(18, 10, 18, 10));

        productsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        productsPanel.setBackground(new Color(245, 247, 250));
        productsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton refreshButton = new JButton("Refresh Products");
        JButton cartButton = new JButton("Open Shopping Cart");
        JButton categoriesButton = new JButton("Back to Categories");

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadProducts();
            }
        });

        cartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CartFrame(customerId);
                dispose();
            }
        });

        categoriesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CategoryFrame(customerId);
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(cartButton);
        buttonPanel.add(categoriesButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadProducts();
        setVisible(true);
    }

    private void loadProducts() {

        productsPanel.removeAll();

        String databaseCategory = category;

        if (category.equalsIgnoreCase("Gadgets")) {
            databaseCategory = "Gadget";
        }

        String sql =
                "SELECT product_id, name, price, discount, stock, " +
                "category, image_path FROM Products " +
                "WHERE LOWER(category) = LOWER(?)";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, databaseCategory);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    int productId = result.getInt("product_id");
                    String name = result.getString("name");
                    double price = result.getDouble("price");
                    double discount = result.getDouble("discount");
                    int stock = result.getInt("stock");
                    String imagePath = result.getString("image_path");

                    double finalPrice =
                            price - (price * discount / 100.0);

                    JPanel card = createProductCard(
                            productId, name, price, discount,
                            finalPrice, stock, imagePath
                    );

                    productsPanel.add(card);
                }
            }

            if (productsPanel.getComponentCount() == 0) {
                productsPanel.add(new JLabel(
                        "No products found in this category.",
                        JLabel.CENTER
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load products: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private JPanel createProductCard(
            int productId,
            String name,
            double price,
            double discount,
            double finalPrice,
            int stock,
            String imagePath
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 232)
                ),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(220, 170));
        imageLabel.setMinimumSize(new Dimension(220, 170));
        imageLabel.setMaximumSize(new Dimension(220, 170));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        File imageFile = new File(imagePath);

        if (imageFile.exists()) {

            ImageIcon originalIcon = new ImageIcon(
                    imageFile.getAbsolutePath()
            );

            Image scaledImage = originalIcon.getImage()
                    .getScaledInstance(
                            220, 170, Image.SCALE_SMOOTH
                    );

            imageLabel.setIcon(new ImageIcon(scaledImage));

        } else {
            imageLabel.setText("Image not found");
            imageLabel.setForeground(Color.GRAY);
        }

        JLabel nameLabel = new JLabel(
                "<html><div style='text-align:center;width:220px;'>"
                        + name + "</div></html>"
        );
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel originalPriceLabel = new JLabel(
                String.format("Price: Rs. %.2f", price)
        );
        originalPriceLabel.setForeground(Color.GRAY);
        originalPriceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel discountLabel = new JLabel(
                String.format("%.0f%% OFF", discount)
        );
        discountLabel.setForeground(new Color(0, 130, 80));
        discountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel finalPriceLabel = new JLabel(
                String.format("Rs. %.2f", finalPrice)
        );
        finalPriceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        finalPriceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stockLabel = new JLabel(
                stock > 0 ? "In stock: " + stock : "Out of stock"
        );
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addToCartButton.setEnabled(stock > 0);

        

addToCartButton.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        CartFrame.addProductFromListing(
            productId,
            name,
            price,
            discount,
            stock
        );
    }
});



        card.add(imageLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(originalPriceLabel);
        card.add(discountLabel);
        card.add(finalPriceLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(stockLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(addToCartButton);

        return card;
    }
}
