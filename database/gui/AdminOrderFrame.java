
package database.gui;

import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminOrderFrame extends JFrame {

    private final DefaultTableModel orderModel =
        new DefaultTableModel(
            new String[] {
                "Order ID", "Customer ID", "Order Date",
                "Total Amount (Rs.)", "Status"
            }, 0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable orderTable = new JTable(orderModel);

    public AdminOrderFrame() {
        setTitle("BuyIt - Order Management");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel(
            "Order Management", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        orderTable.setRowHeight(25);
        orderTable.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        orderTable.setAutoCreateRowSorter(true);

        JButton viewItemsButton = new JButton("View Order Items");
        JButton refreshButton = new JButton("Refresh Orders");
        JButton backButton = new JButton("Back to Dashboard");

        viewItemsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = orderTable.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(
                        AdminOrderFrame.this,
                        "Please select an order first.");
                    return;
                }

                int row = orderTable.convertRowIndexToModel(
                    selectedRow);

                int orderId = Integer.parseInt(
                    orderModel.getValueAt(row, 0).toString());

                showOrderItems(orderId);
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadOrders();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new AdminDashboard();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(viewItemsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        setLayout(new BorderLayout(10, 10));
        add(heading, BorderLayout.NORTH);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadOrders();
        setVisible(true);
    }

    private void loadOrders() {
        orderModel.setRowCount(0);

        String sql =
            "SELECT order_id, customer_id, order_date, " +
            "total_amount, status FROM Orders " +
            "ORDER BY order_id DESC";

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                orderModel.addRow(new Object[] {
                    result.getInt("order_id"),
                    result.getInt("customer_id"),
                    result.getString("order_date"),
                    result.getDouble("total_amount"),
                    result.getString("status")
                });
            }

            if (orderModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "No orders have been placed yet.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load orders:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderItems(int orderId) {
        DefaultTableModel itemModel =
            new DefaultTableModel(
                new String[] {
                    "Product ID", "Product Name", "Quantity",
                    "Unit Price (Rs.)", "Discount (%)"
                }, 0
            ) {
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
            };

        String sql =
            "SELECT oi.product_id, p.name, oi.quantity, " +
            "oi.unit_price, oi.discount_at_purchase " +
            "FROM Order_Items oi " +
            "JOIN Products p ON oi.product_id = p.product_id " +
            "WHERE oi.order_id = ?";

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    itemModel.addRow(new Object[] {
                        result.getInt("product_id"),
                        result.getString("name"),
                        result.getInt("quantity"),
                        result.getDouble("unit_price"),
                        result.getDouble("discount_at_purchase")
                    });
                }
            }

            JTable itemTable = new JTable(itemModel);
            itemTable.setRowHeight(25);

            JScrollPane scrollPane =
                new JScrollPane(itemTable);

            scrollPane.setPreferredSize(
                new Dimension(650, 250));

            JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Items in Order #" + orderId,
                JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not load order items:\n" +
                ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
