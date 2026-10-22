
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueueManager {

    // Customer waiting queue-il join cheyyunnu
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

            // Product stock check
            try (PreparedStatement ps =
                     con.prepareStatement(stockSql)) {

                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product not found!");
                        return false;
                    }

                    if (rs.getInt("stock") > 0) {
                        System.out.println(
                            "Product is available. No need to join queue."
                        );
                        return false;
                    }
                }
            }

            // Duplicate waiting request check
            try (PreparedStatement ps =
                     con.prepareStatement(duplicateSql)) {

                ps.setInt(1, customerId);
                ps.setInt(2, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                            "You are already waiting for this product."
                        );
                        return false;
                    }
                }
            }

            // Add customer to queue
            try (PreparedStatement ps =
                     con.prepareStatement(insertSql)) {

                ps.setInt(1, customerId);
                ps.setInt(2, productId);
                ps.setInt(3, quantity);

                if (ps.executeUpdate() == 1) {
                    System.out.println(
                        "Customer added to waiting queue successfully!"
                    );
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Unable to join queue!");
            e.printStackTrace();
        }

        return false;
    }


    // Queue position kandupidikkunnu
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


    // Oru product-inte waiting queue display cheyyunnu
    public void showWaitingQueue(int productId) {

        String sql = """
            SELECT queue_id, customer_id, quantity,
                   status, joined_at
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
                        + " | Customer ID: "
                        + rs.getInt("customer_id")
                        + " | Quantity: " + rs.getInt("quantity")
                        + " | Status: " + rs.getString("status")
                        + " | Position: " + getQueuePosition(queueId)
                        + " | Joined: " + rs.getString("joined_at")
                    );
                }

                if (!found) {
                    System.out.println(
                        "No queue entries for this product."
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Stock update aakumbol waiting queue process cheyyunnu
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
            INSERT INTO Cart_Items (customer_id, product_id, quantity)
            VALUES (?, ?, ?)
            ON CONFLICT(customer_id, product_id)
            DO UPDATE SET quantity = quantity + excluded.quantity
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
                int stock;

                // Current stock check
                try (PreparedStatement ps =
                         con.prepareStatement(stockSql)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return;
                        }

                        stock = rs.getInt("stock");
                    }
                }

                // Queue FIFO order-il read cheyyunnu
                try (PreparedStatement ps =
                         con.prepareStatement(queueSql)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            int queueId = rs.getInt("queue_id");
                            int customerId =
                                rs.getInt("customer_id");
                            int quantity = rs.getInt("quantity");

                            // Ee request fulfill cheyyan stock pora
                            if (quantity > stock) {
                                continue;
                            }

                            // Customer cart-il product add/update
                            try (PreparedStatement cart =
                                     con.prepareStatement(cartSql)) {

                                cart.setInt(1, customerId);
                                cart.setInt(2, productId);
                                cart.setInt(3, quantity);
                                cart.executeUpdate();
                            }

                            // Stock kurakkunnu
                            try (PreparedStatement update =
                                     con.prepareStatement(
                                         updateStockSql)) {

                                update.setInt(1, quantity);
                                update.setInt(2, productId);
                                update.setInt(3, quantity);

                                if (update.executeUpdate() != 1) {
                                    throw new SQLException(
                                        "Stock update failed."
                                    );
                                }
                            }

                            // Queue status update
                            try (PreparedStatement fulfill =
                                     con.prepareStatement(fulfillSql)) {

                                fulfill.setInt(1, queueId);

                                if (fulfill.executeUpdate() != 1) {
                                    throw new SQLException(
                                        "Queue status update failed."
                                    );
                                }
                            }

                            stock -= quantity;

                            System.out.println(
                                "Queue fulfilled successfully!"
                                + " Customer ID: " + customerId
                                + " | Product ID: " + productId
                                + " | Quantity: " + quantity
                            );
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
