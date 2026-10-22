
package database.gui;

import database.ProductDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminProductFrame extends JFrame {

    private final ProductDAO dao = new ProductDAO();

    private final DefaultTableModel model = new DefaultTableModel(
        new String[] {
            "ID", "Name", "Price", "Discount (%)", "Stock",
            "Category", "Description", "Image Path"
        }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public AdminProductFrame() {
        setTitle("BuyIt - Product Management");
        setSize(1100, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel(
            "Product Management", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        table.setRowHeight(25);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);

        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Dashboard");

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showProductForm(null);
            }
        });

        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int viewRow = table.getSelectedRow();

                if (viewRow == -1) {
                    JOptionPane.showMessageDialog(
                        AdminProductFrame.this,
                        "Please select a product to edit.");
                    return;
                }

                int row = table.convertRowIndexToModel(viewRow);
                Object[] product = new Object[8];

                for (int i = 0; i < 8; i++) {
                    product[i] = model.getValueAt(row, i);
                }

                showProductForm(product);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int viewRow = table.getSelectedRow();

                if (viewRow == -1) {
                    JOptionPane.showMessageDialog(
                        AdminProductFrame.this,
                        "Please select a product to delete.");
                    return;
                }

                int row = table.convertRowIndexToModel(viewRow);
                int id = Integer.parseInt(
                    model.getValueAt(row, 0).toString());

                int choice = JOptionPane.showConfirmDialog(
                    AdminProductFrame.this,
                    "Are you sure you want to delete product ID "
                        + id + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    if (dao.deleteProduct(id)) {
                        JOptionPane.showMessageDialog(
                            AdminProductFrame.this,
                            "Product deleted successfully.");
                        loadProducts();
                    } else {
                        JOptionPane.showMessageDialog(
                            AdminProductFrame.this,
                            "Could not delete the product. It may be "
                            + "linked to an existing order, or a "
                            + "database error may have occurred.");
                    }
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadProducts();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new AdminDashboard();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        setLayout(new BorderLayout(10, 10));
        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadProducts();
        setVisible(true);
    }

    private void loadProducts() {
        model.setRowCount(0);

        List<Object[]> products = dao.getAllProducts();

        for (Object[] product : products) {
            model.addRow(product);
        }
    }

    private void showProductForm(Object[] product) {
        final boolean editing = product != null;

        JTextField idField = new JTextField(
            editing ? String.valueOf(product[0]) : "");
        JTextField nameField = new JTextField(
            editing ? String.valueOf(product[1]) : "");
        JTextField priceField = new JTextField(
            editing ? String.valueOf(product[2]) : "");
        JTextField discountField = new JTextField(
            editing ? String.valueOf(product[3]) : "0");
        JTextField stockField = new JTextField(
            editing ? String.valueOf(product[4]) : "0");
        JTextField categoryField = new JTextField(
            editing ? String.valueOf(product[5]) : "");
        JTextField descriptionField = new JTextField(
            editing && product[6] != null
                ? String.valueOf(product[6]) : "");
        JTextField imageField = new JTextField(
            editing && product[7] != null
                ? String.valueOf(product[7]) : "");

        if (editing) {
            idField.setEditable(false);
        }

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));

        form.add(new JLabel("Product ID:"));
        form.add(idField);
        form.add(new JLabel("Product Name:"));
        form.add(nameField);
        form.add(new JLabel("Price (Rs.):"));
        form.add(priceField);
        form.add(new JLabel("Discount (%):"));
        form.add(discountField);
        form.add(new JLabel("Stock:"));
        form.add(stockField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Description:"));
        form.add(descriptionField);
        form.add(new JLabel("Image Path:"));
        form.add(imageField);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            editing ? "Edit Product" : "Add Product",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String idText = idField.getText().trim();
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String discountText = discountField.getText().trim();
            String stockText = stockField.getText().trim();
            String category = categoryField.getText().trim();
            String description = descriptionField.getText().trim();
            String imagePath = imageField.getText().trim();

            if (idText.isEmpty() || name.isEmpty()
                    || priceText.isEmpty() || discountText.isEmpty()
                    || stockText.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all required fields.");
                return;
            }

            int id = Integer.parseInt(idText);
            double price = Double.parseDouble(priceText);
            double discount = Double.parseDouble(discountText);
            int stock = Integer.parseInt(stockText);

            if (id <= 0 || !Double.isFinite(price)
                    || !Double.isFinite(discount)
                    || price < 0 || discount < 0 || discount > 100
                    || stock < 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Enter valid values: ID must be positive, price "
                    + "and stock cannot be negative, and discount "
                    + "must be between 0 and 100.");
                return;
            }

            boolean success;

            if (editing) {
                success = dao.updateProductDetails(
                    id, name, price, discount, stock,
                    category, description, imagePath);
            } else {
                success = dao.addProductDetails(
                    id, name, price, discount, stock,
                    category, description, imagePath);
            }

            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    editing
                        ? "Product updated successfully."
                        : "Product added successfully.");

                loadProducts();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "The operation failed. Check the console for "
                    + "database details. The product ID may already "
                    + "exist, or the database may reject the change.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Enter valid numeric values for ID, price, "
                + "discount and stock.");
        }
    }
}
