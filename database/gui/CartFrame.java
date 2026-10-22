package database.gui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class CartFrame extends JFrame {

    private final int customerId;
    private final JComboBox<String> productBox = new JComboBox<>();
    private final ArrayList<Integer> productIds = new ArrayList<>();
    private final ArrayList<String> productNames = new ArrayList<>();
    private final ArrayList<Double> prices = new ArrayList<>();
    private final ArrayList<Double> discounts = new ArrayList<>();
    private final ArrayList<Integer> stocks = new ArrayList<>();

    private final DefaultListModel<String> cartModel =
            new DefaultListModel<>();
    private final JList<String> cartList = new JList<>(cartModel);
    private final ArrayList<Integer> cartItemIds = new ArrayList<>();
    private final ArrayList<Integer> cartProductIds = new ArrayList<>();
    private final ArrayList<String> cartNames = new ArrayList<>();
    private final ArrayList<Double> cartPrices = new ArrayList<>();
    private final ArrayList<Double> cartDiscounts = new ArrayList<>();
    private final ArrayList<Integer> cartQuantities = new ArrayList<>();
    private final ArrayList<Integer> reservedQuantities = new ArrayList<>();
    private final JLabel totalLabel = new JLabel("Total: Rs. 0.00");

    public CartFrame(int customerId) {
        this.customerId = customerId;

        setTitle("BuyIt - Shopping Cart");
        setSize(750, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Shopping Cart", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton addButton = new JButton("Add to Cart");
        JButton removeButton = new JButton("Remove Selected");
        JButton refreshButton = new JButton("Refresh Products");
        JButton checkoutButton = new JButton("Checkout");
        JButton backButton = new JButton("Back to Categories");

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
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
        refreshButton.addActionListener(e -> {
            loadProducts();
            loadCart();
        });
        checkoutButton.addActionListener(e -> checkout());
        backButton.addActionListener(e -> {
            new CategoryFrame(customerId);
            dispose();
        });

        ensureCartSchema();
        loadProducts();
        loadCart();
        setVisible(true);
    }

    // Compatibility overload for any old call site; use the customer-aware
    // overload from ProductFrame so items are saved to the correct account.
    public static void addProductFromListing(
            int productId, String name, double price,
            double discount, int stock) {
        JOptionPane.showMessageDialog(
                null,
                "Please open the shopping cart from your logged-in account "
                + "before adding this product.");
    }

    public static void addProductFromListing(
            int customerId, int productId, String name,
            double price, double discount, int stock) {

        if (customerId <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Please log in before adding products to your cart.");
            return;
        }

        if (stock <= 0) {
            JOptionPane.showMessageDialog(null,
                    "This product is out of stock. Join the waiting queue.");
            return;
        }

        String input = JOptionPane.showInputDialog(
                null, "Enter quantity for " + name + ":", "1");
        if (input == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(input);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null,
                        "Quantity must be greater than zero.");
                return;
            }

            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    ensureCartSchema(con);
                    int currentStock;
                    int currentQuantity = 0;
                    int reservedQuantity = 0;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT stock FROM Products WHERE product_id=?")) {
                        ps.setInt(1, productId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException("Product not found.");
                            }
                            currentStock = rs.getInt("stock");
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement("""
                            SELECT quantity, reserved_quantity
                            FROM Cart_Items
                            WHERE customer_id=? AND product_id=?
                            """)) {
                        ps.setInt(1, customerId);
                        ps.setInt(2, productId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                currentQuantity = rs.getInt("quantity");
                                reservedQuantity =
                                        rs.getInt("reserved_quantity");
                            }
                        }
                    }

                    int normalQuantity = currentQuantity - reservedQuantity;
                    if (normalQuantity + quantity > currentStock) {
                        throw new SQLException(
                                "Not enough stock available. Current stock: "
                                + currentStock);
                    }

                    try (PreparedStatement ps = con.prepareStatement("""
                            INSERT INTO Cart_Items
                                (customer_id, product_id, quantity,
                                 reserved_quantity)
                            VALUES (?, ?, ?, 0)
                            ON CONFLICT(customer_id, product_id)
                            DO UPDATE SET
                                quantity = Cart_Items.quantity + excluded.quantity
                            """)) {
                        ps.setInt(1, customerId);
                        ps.setInt(2, productId);
                        ps.setInt(3, quantity);
                        ps.executeUpdate();
                    }

                    con.commit();
                    JOptionPane.showMessageDialog(null,
                            name + " added to your cart!");
                } catch (SQLException ex) {
                    con.rollback();
                    JOptionPane.showMessageDialog(null, ex.getMessage(),
                            "Unable to Add Product",
                            JOptionPane.WARNING_MESSAGE);
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Database error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "Please enter a valid whole number.");
        }
    }

    private static void ensureCartSchema(Connection con) throws SQLException {
        boolean hasReservedQuantity = false;
        try (PreparedStatement ps =
                     con.prepareStatement("PRAGMA table_info(Cart_Items)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if ("reserved_quantity".equalsIgnoreCase(
                        rs.getString("name"))) {
                    hasReservedQuantity = true;
                    break;
                }
            }
        }
        if (!hasReservedQuantity) {
            try (PreparedStatement ps = con.prepareStatement(
                    "ALTER TABLE Cart_Items ADD COLUMN "
                    + "reserved_quantity INTEGER NOT NULL DEFAULT 0")) {
                ps.executeUpdate();
            }
        }
    }

    private void ensureCartSchema() {
        try (Connection con = DatabaseConnection.getConnection()) {
            ensureCartSchema(con);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to prepare shopping cart: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        productBox.removeAllItems();
        productIds.clear();
        productNames.clear();
        prices.clear();
        discounts.clear();
        stocks.clear();

        String sql = """
            SELECT product_id, name, price, discount, stock
            FROM Products ORDER BY product_id
            """;

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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

                double finalPrice = price * (1 - discount / 100.0);
                productBox.addItem(name + " (Rs. "
                        + String.format("%.2f", finalPrice)
                        + ", Stock: " + stock + ")");
            }
            if (productBox.getItemCount() == 0) {
                productBox.addItem("No products found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load products: " + e.getMessage());
        }
    }

    private void addToCart() {
        int index = productBox.getSelectedIndex();
        if (index < 0 || index >= productIds.size()) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }

        addProductFromListing(customerId, productIds.get(index),
                productNames.get(index), prices.get(index),
                discounts.get(index), stocks.get(index));
        loadCart();
        loadProducts();
    }

    private void loadCart() {
        cartModel.clear();
        cartItemIds.clear();
        cartProductIds.clear();
        cartNames.clear();
        cartPrices.clear();
        cartDiscounts.clear();
        cartQuantities.clear();
        reservedQuantities.clear();

        String sql = """
            SELECT ci.cart_item_id, ci.product_id, ci.quantity,
                   ci.reserved_quantity, p.name, p.price, p.discount
            FROM Cart_Items ci
            JOIN Products p ON p.product_id = ci.product_id
            WHERE ci.customer_id = ?
            ORDER BY ci.cart_item_id
            """;

        double total = 0;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ensureCartSchema(con);
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("cart_item_id");
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");
                    int reserved = rs.getInt("reserved_quantity");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    double discount = rs.getDouble("discount");
                    double finalPrice = price * (1 - discount / 100.0);
                    double lineTotal = finalPrice * quantity;
                    total += lineTotal;

                    cartItemIds.add(itemId);
                    cartProductIds.add(productId);
                    cartNames.add(name);
                    cartPrices.add(price);
                    cartDiscounts.add(discount);
                    cartQuantities.add(quantity);
                    reservedQuantities.add(reserved);

                    String note = reserved > 0
                            ? " | Queue reserved: " + reserved : "";
                    cartModel.addElement(name + " | Qty: " + quantity
                            + note + " | Unit price: Rs. "
                            + String.format("%.2f", finalPrice)
                            + " | Total: Rs. "
                            + String.format("%.2f", lineTotal));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load cart: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
    }

    private void removeSelected() {
        int index = cartList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to remove.");
            return;
        }

        int itemId = cartItemIds.get(index);
        int productId = cartProductIds.get(index);
        int reserved = reservedQuantities.get(index);

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (reserved > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE Products SET stock = stock + ? "
                            + "WHERE product_id = ?")) {
                        ps.setInt(1, reserved);
                        ps.setInt(2, productId);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM Cart_Items WHERE cart_item_id=? "
                        + "AND customer_id=?")) {
                    ps.setInt(1, itemId);
                    ps.setInt(2, customerId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
            loadCart();
            loadProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to remove item: " + e.getMessage());
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < cartQuantities.size(); i++) {
            double finalPrice = cartPrices.get(i)
                    * (1 - cartDiscounts.get(i) / 100.0);
            total += finalPrice * cartQuantities.get(i);
        }
        return total;
    }

    private void checkout() {
        loadCart();
        if (cartProductIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        double total = calculateTotal();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Place order for Rs. " + String.format("%.2f", total) + "?",
                "Confirm Order", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String orderSql = """
            INSERT INTO Orders (customer_id, total_amount, status)
            VALUES (?, ?, 'PLACED')
            """;
        String itemSql = """
            INSERT INTO Order_Items
                (order_id, product_id, quantity, unit_price,
                 discount_at_purchase)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                ensureCartSchema(con);
                int orderId;
                try (PreparedStatement ps = con.prepareStatement(
                        orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not create order ID.");
                        }
                        orderId = keys.getInt(1);
                    }
                }

                for (int i = 0; i < cartProductIds.size(); i++) {
                    int productId = cartProductIds.get(i);
                    int quantity = cartQuantities.get(i);
                    int reserved = reservedQuantities.get(i);
                    int normalQuantity = quantity - reserved;

                    // Queue-reserved units already reduced stock during
                    // queue processing. Only deduct non-reserved units now.
                    if (normalQuantity > 0) {
                        try (PreparedStatement ps = con.prepareStatement("""
                                UPDATE Products SET stock = stock - ?
                                WHERE product_id = ? AND stock >= ?
                                """)) {
                            ps.setInt(1, normalQuantity);
                            ps.setInt(2, productId);
                            ps.setInt(3, normalQuantity);
                            if (ps.executeUpdate() != 1) {
                                throw new SQLException(
                                        "Insufficient stock for "
                                        + cartNames.get(i)
                                        + ". Refresh and try again.");
                            }
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, productId);
                        ps.setInt(3, quantity);
                        ps.setDouble(4, cartPrices.get(i));
                        ps.setDouble(5, cartDiscounts.get(i));
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM Cart_Items WHERE customer_id=?")) {
                    ps.setInt(1, customerId);
                    ps.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this,
                        "Order placed successfully!\nOrder ID: " + orderId
                        + "\nTotal: Rs. " + String.format("%.2f", total));
                loadCart();
                loadProducts();
            } catch (SQLException e) {
                con.rollback();
                JOptionPane.showMessageDialog(this,
                        "Order failed. Changes were rolled back.\n"
                        + e.getMessage(),
                        "Checkout Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
