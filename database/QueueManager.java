package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueueManager {

    // Add the reservation column without deleting existing cart data.
    private void ensureCartSchema(Connection con) throws SQLException {
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

    // Customer joins the waiting queue only when stock is zero.
    public boolean joinQueue(int customerId, int productId,
                             int quantity) {

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return false;
        }

        String stockSql =
            "SELECT stock FROM Products WHERE product_id = ?";

        String duplicateSql = """
            SELECT queue_id FROM Waiting_Queue
            WHERE customer_id = ?
              AND product_id = ?
              AND status = 'WAITING'
            """;

        String insertSql = """
            INSERT INTO Waiting_Queue
                (customer_id, product_id, quantity, status)
            VALUES (?, ?, ?, 'WAITING')
            """;

        try (Connection con = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(stockSql)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product not found!");
                        return false;
                    }
                    if (rs.getInt("stock") > 0) {
                        System.out.println(
                            "Product is available. No need to join queue.");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps =
                     con.prepareStatement(duplicateSql)) {
                ps.setInt(1, customerId);
                ps.setInt(2, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                            "You are already waiting for this product.");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, customerId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);
                if (ps.executeUpdate() == 1) {
                    System.out.println(
                        "Customer added to waiting queue successfully!");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Unable to join queue!");
            e.printStackTrace();
        }

        return false;
    }

    // Find the customer's position in the queue.
    public int getQueuePosition(int queueId) {
        String sql = """
            SELECT COUNT(*) AS position
            FROM Waiting_Queue current
            JOIN Waiting_Queue earlier
              ON current.product_id = earlier.product_id
             AND earlier.queue_id <= current.queue_id
             AND earlier.status = 'WAITING'
            WHERE current.queue_id = ?
              AND current.status = 'WAITING'
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, queueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("position");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Display waiting queue entries for a product.
    public void showWaitingQueue(int productId) {
        String sql = """
            SELECT queue_id, customer_id, quantity, status, joined_at
            FROM Waiting_Queue
            WHERE product_id = ?
            ORDER BY queue_id ASC
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int queueId = rs.getInt("queue_id");
                    System.out.println(
                        "Queue ID: " + queueId
                        + " | Customer ID: " + rs.getInt("customer_id")
                        + " | Quantity: " + rs.getInt("quantity")
                        + " | Status: " + rs.getString("status")
                        + " | Position: " + getQueuePosition(queueId)
                        + " | Joined: " + rs.getString("joined_at"));
                }
                if (!found) {
                    System.out.println("No queue entries for this product.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Allocate restocked items to waiting customers in FIFO order.
    public void processQueueForProduct(int productId) {
        String stockSql =
            "SELECT stock FROM Products WHERE product_id = ?";

        String queueSql = """
            SELECT queue_id, customer_id, quantity
            FROM Waiting_Queue
            WHERE product_id = ? AND status = 'WAITING'
            ORDER BY queue_id ASC
            """;

        String cartSql = """
            INSERT INTO Cart_Items
                (customer_id, product_id, quantity, reserved_quantity)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(customer_id, product_id)
            DO UPDATE SET
                quantity = Cart_Items.quantity + excluded.quantity,
                reserved_quantity =
                    Cart_Items.reserved_quantity + excluded.reserved_quantity
            """;

        String updateStockSql = """
            UPDATE Products
            SET stock = stock - ?
            WHERE product_id = ? AND stock >= ?
            """;

        String fulfillSql = """
            UPDATE Waiting_Queue
            SET status = 'FULFILLED',
                fulfilled_at = CURRENT_TIMESTAMP
            WHERE queue_id = ? AND status = 'WAITING'
            """;

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                ensureCartSchema(con);

                int stock;
                try (PreparedStatement ps = con.prepareStatement(stockSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return;
                        }
                        stock = rs.getInt("stock");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(queueSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int queueId = rs.getInt("queue_id");
                            int customerId = rs.getInt("customer_id");
                            int quantity = rs.getInt("quantity");

                            // Preserve FIFO: don't skip the oldest request.
                            if (quantity > stock) {
                                break;
                            }

                            try (PreparedStatement cart =
                                     con.prepareStatement(cartSql)) {
                                cart.setInt(1, customerId);
                                cart.setInt(2, productId);
                                cart.setInt(3, quantity);
                                cart.setInt(4, quantity);
                                cart.executeUpdate();
                            }

                            try (PreparedStatement update =
                                     con.prepareStatement(updateStockSql)) {
                                update.setInt(1, quantity);
                                update.setInt(2, productId);
                                update.setInt(3, quantity);
                                if (update.executeUpdate() != 1) {
                                    throw new SQLException(
                                        "Stock update failed.");
                                }
                            }

                            try (PreparedStatement fulfill =
                                     con.prepareStatement(fulfillSql)) {
                                fulfill.setInt(1, queueId);
                                if (fulfill.executeUpdate() != 1) {
                                    throw new SQLException(
                                        "Queue status update failed.");
                                }
                            }

                            stock -= quantity;
                            System.out.println(
                                "Queue fulfilled successfully! Customer ID: "
                                + customerId + " | Product ID: " + productId
                                + " | Quantity: " + quantity);
                        }
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Queue processing failed!");
            e.printStackTrace();
        }
    }
}
