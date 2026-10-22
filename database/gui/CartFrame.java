
package database.gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class CartFrame extends JFrame {

    private int customerId;

    private JComboBox<String> productBox;

    private ArrayList<Integer> productIds = new ArrayList<>();
    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<Double> discounts = new ArrayList<>();
    private ArrayList<Integer> stocks = new ArrayList<>();

    private DefaultListModel<String> cartModel =
            new DefaultListModel<>();

    private JList<String> cartList = new JList<>(cartModel);

    // Shared cart data so products remain when screens change.
    private static ArrayList<Integer> cartProductIds =
            new ArrayList<>();

    private static ArrayList<String> cartNames =
            new ArrayList<>();

    // Stores the discounted unit price.
    private static ArrayList<Double> cartPrices =
            new ArrayList<>();

    private static ArrayList<Integer> cartQuantities =
            new ArrayList<>();

    private JLabel totalLabel = new JLabel("Total: Rs. 0.00");

    public CartFrame(int customerId) {

        this.customerId = customerId;

        setTitle("BuyIt - Shopping Cart");
        setSize(750, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "Shopping Cart", JLabel.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 24));

        productBox = new JComboBox<>();

        JButton addButton = new JButton("Add to Cart");
        JButton removeButton = new JButton("Remove Selected");
        JButton refreshButton = new JButton("Refresh Products");
        JButton checkoutButton = new JButton("Checkout");
        JButton backButton = new JButton("Back to Categories");

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10)
        );
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(productBox, BorderLayout.CENTER);
        topPanel.add(addButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(totalLabel);
        bottomPanel.add(removeButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(checkoutButton);
        bottomPanel.add(backButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(cartList), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeSelected());
        refreshButton.addActionListener(e -> loadProducts());
        checkoutButton.addActionListener(e -> checkout());

        backButton.addActionListener(e -> {
            new CategoryFrame(customerId);
            dispose();
        });

        loadProducts();
        updateCartDisplay();

        setVisible(true);
    }

    // Add a product selected from the product listing screen.
    public static void addProductFromListing(
            int productId,
            String name,
            double price,
            double discount,
            int stock
    ) {

        if (stock <= 0) {
            JOptionPane.showMessageDialog(
                    null, "This product is out of stock."
            );
            return;
        }

        String input = JOptionPane.showInputDialog(
                null,
                "Enter quantity for " + name + ":",
                "1"
        );

        if (input == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(input);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Quantity must be greater than zero."
                );
                return;
            }

            if (quantity > stock) {
                JOptionPane.showMessageDialog(
                        null,
                        "Not enough stock available."
                );
                return;
            }

            int index = cartProductIds.indexOf(productId);

            if (index != -1) {

                int newQuantity =
                        cartQuantities.get(index) + quantity;

                if (newQuantity > stock) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Not enough stock available."
                    );
                    return;
                }

                cartQuantities.set(index, newQuantity);

            } else {

                cartProductIds.add(productId);
                cartNames.add(name);

                double finalPrice =
                        price * (1 - discount / 100.0);

                cartPrices.add(finalPrice);
                cartQuantities.add(quantity);
            }

            JOptionPane.showMessageDialog(
                    null,
                    name + " added to your cart!"
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid whole number."
            );
        }
    }

    private void loadProducts() {

        productBox.removeAllItems();

        productIds.clear();
        productNames.clear();
        prices.clear();
        discounts.clear();
        stocks.clear();

        String sql =
                "SELECT product_id, name, price, discount, stock "
                + "FROM Products ORDER BY product_id";

        try (
                Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount");
                int stock = rs.getInt("stock");

                productIds.add(id);
                productNames.add(name);
                prices.add(price);
                discounts.add(discount);
                stocks.add(stock);

                double finalPrice =
                        price * (1 - discount / 100.0);

                productBox.addItem(
                        name + " (Rs. "
                        + String.format("%.2f", finalPrice)
                        + ", Stock: " + stock + ")"
                );
            }

            if (productBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No products found in database."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load products: " + e.getMessage()
            );
        }
    }

    private void addToCart() {

        int index = productBox.getSelectedIndex();

        if (index == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a product."
            );
            return;
        }

        addProductFromListing(
                productIds.get(index),
                productNames.get(index),
                prices.get(index),
                discounts.get(index),
                stocks.get(index)
        );

        updateCartDisplay();
    }

    private void removeSelected() {

        int index = cartList.getSelectedIndex();

        if (index == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an item to remove."
            );
            return;
        }

        cartProductIds.remove(index);
        cartNames.remove(index);
        cartPrices.remove(index);
        cartQuantities.remove(index);

        updateCartDisplay();
    }

    private double calculateTotal() {

        double total = 0;

        for (int i = 0; i < cartNames.size(); i++) {
            total += cartPrices.get(i)
                    * cartQuantities.get(i);
        }

        return total;
    }

    private void updateCartDisplay() {

        cartModel.clear();

        for (int i = 0; i < cartNames.size(); i++) {

            double itemTotal =
                    cartPrices.get(i) * cartQuantities.get(i);

            cartModel.addElement(
                    cartNames.get(i)
                    + " | Qty: " + cartQuantities.get(i)
                    + " | Unit price: Rs. "
                    + String.format("%.2f", cartPrices.get(i))
                    + " | Total: Rs. "
                    + String.format("%.2f", itemTotal)
            );
        }

        totalLabel.setText(
                "Total: Rs. "
                + String.format("%.2f", calculateTotal())
        );
    }

    private void checkout() {

        if (customerId <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please log in again before checkout."
            );
            return;
        }

        if (cartProductIds.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Your cart is empty."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Place order for Rs. "
                + String.format("%.2f", calculateTotal()) + "?",
                "Confirm Order",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String orderSql =
                "INSERT INTO Orders "
                + "(customer_id, total_amount, status) "
                + "VALUES (?, ?, ?)";

        String itemSql =
                "INSERT INTO Order_Items "
                + "(order_id, product_id, quantity, unit_price, "
                + "discount_at_purchase) "
                + "VALUES (?, ?, ?, ?, ?)";

        String stockSql =
                "UPDATE Products SET stock = stock - ? "
                + "WHERE product_id = ? AND stock >= ?";

        try (Connection con = DatabaseConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int orderId;

                try (PreparedStatement ps = con.prepareStatement(
                        orderSql, Statement.RETURN_GENERATED_KEYS
                )) {

                    ps.setInt(1, customerId);
                    ps.setDouble(2, calculateTotal());
                    ps.setString(3, "PLACED");
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not create order ID."
                            );
                        }

                        orderId = keys.getInt(1);
                    }
                }

                for (int i = 0; i < cartProductIds.size(); i++) {

                    int productId = cartProductIds.get(i);
                    int quantity = cartQuantities.get(i);

                    // Update stock only when sufficient stock exists.
                    try (PreparedStatement ps =
                                 con.prepareStatement(stockSql)) {

                        ps.setInt(1, quantity);
                        ps.setInt(2, productId);
                        ps.setInt(3, quantity);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Insufficient stock for "
                                    + cartNames.get(i)
                                    + ". Please refresh and try again."
                            );
                        }
                    }

                    int productIndex = productIds.indexOf(productId);

                    // Read the current product price and discount
                    // from the database for the order record.
                    double originalPrice;
                    double discount;

                    String priceSql =
                            "SELECT price, discount FROM Products "
                            + "WHERE product_id = ?";

                    try (PreparedStatement ps =
                                 con.prepareStatement(priceSql)) {

                        ps.setInt(1, productId);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Product no longer exists: "
                                        + cartNames.get(i)
                                );
                            }

                            originalPrice = rs.getDouble("price");
                            discount = rs.getDouble("discount");
                        }
                    }

                    // Store the original price and discount
                    // at the time of purchase.
                    try (PreparedStatement ps =
                                 con.prepareStatement(itemSql)) {

                        ps.setInt(1, orderId);
                        ps.setInt(2, productId);
                        ps.setInt(3, quantity);
                        ps.setDouble(4, originalPrice);
                        ps.setDouble(5, discount);

                        ps.executeUpdate();
                    }
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "Order placed successfully!\n"
                        + "Order ID: " + orderId + "\n"
                        + "Total: Rs. "
                        + String.format("%.2f", calculateTotal())
                );

                cartProductIds.clear();
                cartNames.clear();
                cartPrices.clear();
                cartQuantities.clear();

                updateCartDisplay();
                loadProducts();

            } catch (SQLException e) {

                con.rollback();

                JOptionPane.showMessageDialog(
                        this,
                        "Order failed. Changes were rolled back.\n"
                        + e.getMessage(),
                        "Checkout Error",
                        JOptionPane.ERROR_MESSAGE
                );

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error: " + e.getMessage()
            );
        }
    }
}
