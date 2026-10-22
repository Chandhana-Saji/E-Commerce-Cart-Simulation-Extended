
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainFrame extends JFrame {

    private final ProductManager manager = new ProductManager();
    private final Cart cart = new Cart();
    private JPanel contentPanel;

    public MainFrame() {
        setTitle("E-Commerce Cart Simulation");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Temporary sample products
        manager.addProduct(new Product(1, "Notebook", 80.0, 5.0, 50));
        manager.addProduct(new Clothing(2, "T-Shirt", 500.0, 10.0, 20, "M", "Cotton"));
        manager.addProduct(new Gadget(3, "Headphones", 1500.0, 8.0, 10, "Boat", "1 year"));
        manager.addProduct(new Footwear(4, "Sports Shoes", 2000.0, 15.0, 15, "Puma", 8));

        JLabel heading = new JLabel("E-COMMERCE CART SIMULATION", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(heading, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(contentPanel, BorderLayout.CENTER);

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        JButton homeButton = new JButton("Home");
        JButton productsButton = new JButton("Shop Products");
        JButton addButton = new JButton("Add Product");
        JButton cartButton = new JButton("My Cart");
        JButton customerButton = new JButton("Customer");

        menuPanel.add(homeButton);
        menuPanel.add(productsButton);
        menuPanel.add(addButton);
        menuPanel.add(cartButton);
        menuPanel.add(customerButton);
        add(menuPanel, BorderLayout.SOUTH);

        homeButton.addActionListener(e -> showHome());
        productsButton.addActionListener(e -> showProducts());
        addButton.addActionListener(e -> showAddProduct());
        cartButton.addActionListener(e -> showCart());
        customerButton.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Customer screen will be added next."));

        showHome();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showHome() {
        contentPanel.removeAll();

        JLabel welcome = new JLabel("Welcome to our online store!", JLabel.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel description = new JLabel(
            "Browse products, add them to your cart, and shop with ease.",
            JLabel.CENTER);

        JButton shopButton = new JButton("Start Shopping");
        JButton cartButton = new JButton("View My Cart");
        JButton addProductButton = new JButton("Add a Product");

        shopButton.addActionListener(e -> showProducts());
        cartButton.addActionListener(e -> showCart());
        addProductButton.addActionListener(e -> showAddProduct());

        JPanel buttons = new JPanel();
        buttons.add(shopButton);
        buttons.add(cartButton);
        buttons.add(addProductButton);

        JPanel homePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        homePanel.add(welcome);
        homePanel.add(description);
        homePanel.add(buttons);

        contentPanel.add(homePanel, BorderLayout.CENTER);
        refreshContent();
    }

    private void showProducts() {
        contentPanel.removeAll();

        JLabel title = new JLabel("Available Products", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        String[] columns = {
            "Product ID", "Name", "Price (₹)", "Discount (%)",
            "Final Price (₹)", "Stock"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Product product : manager.getProducts()) {
            model.addRow(new Object[] {
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getDiscount(),
                product.calculateFinalPrice(),
                product.getStock()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);

        JButton addToCartButton = new JButton("Add Selected to Cart");
        JButton addProductButton = new JButton("Add Product");

        addToCartButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product.");
                return;
            }

            int productId = (int) model.getValueAt(selectedRow, 0);
            Product selectedProduct = findProduct(productId);

            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(this, "Product not found.");
                return;
            }

            if (selectedProduct.getStock() <= 0) {
                JOptionPane.showMessageDialog(this, "This product is out of stock.");
                return;
            }

            int quantity = (Integer) JOptionPane.showInputDialog(
                this,
                "Available stock: " + selectedProduct.getStock()
                    + "\nEnter quantity:",
                "Add to Cart",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                1
            );

            // The dialog may be cancelled; handle that below using a separate input.
        });

        // Use a quantity dialog that safely handles cancellation and invalid input.
        addToCartButton.addActionListener(e -> addSelectedProductToCart(table, model));
        addProductButton.addActionListener(e -> showAddProduct());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addToCartButton);
        bottomPanel.add(addProductButton);

        contentPanel.add(title, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        refreshContent();
    }

    private void addSelectedProductToCart(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }

        int productId = (int) model.getValueAt(selectedRow, 0);
        Product product = findProduct(productId);

        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found.");
            return;
        }

        int alreadyInCart = 0;
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getProductId() == productId) {
                alreadyInCart = item.getQuantity();
                break;
            }
        }

        int available = product.getStock() - alreadyInCart;

        if (available <= 0) {
            JOptionPane.showMessageDialog(
                this, "No additional stock is available for this product.");
            return;
        }

        JSpinner quantitySpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, available, 1));

        int choice = JOptionPane.showConfirmDialog(
            this,
            new Object[] {
                "Product: " + product.getName(),
                "Available to add: " + available,
                "Quantity:",
                quantitySpinner
            },
            "Add to Cart",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        cart.addItem(product, quantity);

        JOptionPane.showMessageDialog(this, "Product added to cart!");
        showProducts();
    }

    private void showCart() {
        contentPanel.removeAll();

        JLabel title = new JLabel("My Shopping Cart", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        String[] columns = {
            "Product ID", "Product", "Unit Price (₹)",
            "Quantity", "Line Total (₹)"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            model.addRow(new Object[] {
                product.getProductId(),
                product.getName(),
                String.format("%.2f", product.calculateFinalPrice()),
                item.getQuantity(),
                String.format("%.2f", item.getTotalPrice())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JLabel totalLabel = new JLabel(
            String.format("Subtotal: ₹%.2f", cart.calculateSubtotal()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton removeButton = new JButton("Remove Selected Item");
        JButton checkoutButton = new JButton("Checkout");
        JButton continueShoppingButton = new JButton("Continue Shopping");

        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item to remove.");
                return;
            }

            int productId = (int) model.getValueAt(row, 0);
            cart.removeItem(productId);
            showCart();
        });

        checkoutButton.addActionListener(e -> checkout());

        continueShoppingButton.addActionListener(e -> showProducts());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottomPanel.add(totalLabel);
        bottomPanel.add(removeButton);
        bottomPanel.add(continueShoppingButton);
        bottomPanel.add(checkoutButton);

        contentPanel.add(title, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        refreshContent();
    }

    private void checkout() {
        if (cart.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        // Validate all items before changing any stock.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (item.getQuantity() > product.getStock()) {
                JOptionPane.showMessageDialog(
                    this, "Insufficient stock for " + product.getName()
                        + ". Please review your cart.");
                return;
            }
        }

        double total = cart.calculateSubtotal();

        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Confirm purchase?\nTotal: ₹%.2f", total),
            "Checkout",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
        }

        cart.getItems().clear();

        // Cart.getItems() returns a copy, so clear the actual cart using removeItem.
        // The cart is emptied below by removing each product.
        JOptionPane.showMessageDialog(
            this,
            String.format("Order placed successfully!\nAmount paid: ₹%.2f", total));

        showProducts();
    }

    private Product findProduct(int productId) {
        for (Product product : manager.getProducts()) {
            if (product.getProductId() == productId) {
                return product;
            }
        }
        return null;
    }

    private void showAddProduct() {
        contentPanel.removeAll();

        JLabel title = new JLabel("Add New Product", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 60, 15, 60));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField discountField = new JTextField();
        JTextField stockField = new JTextField();

        String[] productTypes = {
            "General Product", "Clothing", "Gadget", "Footwear"
        };
        JComboBox<String> typeBox = new JComboBox<>(productTypes);

        JTextField detail1Field = new JTextField();
        JTextField detail2Field = new JTextField();

        JLabel detail1Label = new JLabel("Extra detail 1:");
        JLabel detail2Label = new JLabel("Extra detail 2:");

        form.add(new JLabel("Product Type:"));
        form.add(typeBox);
        form.add(new JLabel("Product ID:"));
        form.add(idField);
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Price:"));
        form.add(priceField);
        form.add(new JLabel("Discount (%):"));
        form.add(discountField);
        form.add(new JLabel("Stock:"));
        form.add(stockField);
        form.add(detail1Label);
        form.add(detail1Field);
        form.add(detail2Label);
        form.add(detail2Field);

        Runnable updateDetailLabels = () -> {
            int type = typeBox.getSelectedIndex();

            if (type == 1) {
                detail1Label.setText("Clothing size:");
                detail2Label.setText("Material:");
            } else if (type == 2) {
                detail1Label.setText("Brand:");
                detail2Label.setText("Warranty:");
            } else if (type == 3) {
                detail1Label.setText("Brand:");
                detail2Label.setText("Footwear size:");
            } else {
                detail1Label.setText("Extra detail 1:");
                detail2Label.setText("Extra detail 2:");
            }
        };

        typeBox.addActionListener(e -> updateDetailLabels.run());
        updateDetailLabels.run();

        JButton saveButton = new JButton("Save Product");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttons = new JPanel();
        buttons.add(saveButton);
        buttons.add(cancelButton);

        saveButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                double discount = Double.parseDouble(discountField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());

                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Product name cannot be empty.");
                }

                if (id <= 0 || price < 0 || stock < 0
                        || discount < 0 || discount > 100) {
                    throw new IllegalArgumentException(
                        "Enter a positive ID, non-negative price/stock, "
                            + "and a discount between 0 and 100.");
                }

                for (Product existing : manager.getProducts()) {
                    if (existing.getProductId() == id) {
                        throw new IllegalArgumentException(
                            "This Product ID already exists.");
                    }
                }

                String detail1 = detail1Field.getText().trim();
                String detail2 = detail2Field.getText().trim();
                int type = typeBox.getSelectedIndex();

                Product product;

                switch (type) {
                    case 1:
                        if (detail1.isEmpty() || detail2.isEmpty()) {
                            throw new IllegalArgumentException(
                                "Enter clothing size and material.");
                        }
                        product = new Clothing(
                            id, name, price, discount, stock, detail1, detail2);
                        break;

                    case 2:
                        if (detail1.isEmpty() || detail2.isEmpty()) {
                            throw new IllegalArgumentException(
                                "Enter gadget brand and warranty.");
                        }
                        product = new Gadget(
                            id, name, price, discount, stock, detail1, detail2);
                        break;

                    case 3:
                        if (detail1.isEmpty() || detail2.isEmpty()) {
                            throw new IllegalArgumentException(
                                "Enter footwear brand and size.");
                        }
                        int footwearSize = Integer.parseInt(detail2);
                        product = new Footwear(
                            id, name, price, discount, stock,
                            detail1, footwearSize);
                        break;

                    default:
                        product = new Product(
                            id, name, price, discount, stock);
                        break;
                }

                manager.addProduct(product);
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                showProducts();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this, "Enter valid numbers. Footwear size must be an integer.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Invalid Product Details", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> showProducts());

        contentPanel.add(title, BorderLayout.NORTH);
        contentPanel.add(form, BorderLayout.CENTER);
        contentPanel.add(buttons, BorderLayout.SOUTH);
        refreshContent();
    }

    private void refreshContent() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
