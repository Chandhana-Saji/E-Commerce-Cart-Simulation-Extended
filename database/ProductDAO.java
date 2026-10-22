
package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // GUI-kku products list return cheyyunnu
    public List<Object[]> getAllProducts() {
        List<Object[]> products = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY product_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(new Object[] {
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getDouble("discount"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("image_path")
                });
            }
        } catch (SQLException e) {
            System.out.println("Unable to retrieve products!");
            e.printStackTrace();
        }
        return products;
    }

    public void addProduct(int id, String name, double price,
                           double discount, int stock) {
        addProductDetails(id, name, price, discount, stock,
                          "General", "", "");
    }

    // true = add success
    public boolean addProductDetails(int id, String name,
                                     double price, double discount,
                                     int stock, String category,
                                     String description, String imagePath) {
        String sql = """
            INSERT INTO Products
            (product_id, name, price, discount, stock,
             category, description, image_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setDouble(3, price);
            ps.setDouble(4, discount);
            ps.setInt(5, stock);
            ps.setString(6, category);
            ps.setString(7, description);
            ps.setString(8, imagePath);

            boolean success = ps.executeUpdate() > 0;

            if (success && stock > 0) {
                new QueueManager().processQueueForProduct(id);
            }
            return success;

        } catch (SQLException e) {
            System.out.println("Unable to add product!");
            e.printStackTrace();
            return false;
        }
    }

    public void displayProducts() {
        for (Object[] p : getAllProducts()) {
            System.out.println(
                "ID: " + p[0] + " | Name: " + p[1]
                + " | Price: " + p[2] + " | Discount: " + p[3]
                + "% | Stock: " + p[4] + " | Category: " + p[5]
            );
        }
    }

    public void searchProduct(int id) {
        String sql = "SELECT * FROM Products WHERE product_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(
                        "ID: " + rs.getInt("product_id")
                        + " | Name: " + rs.getString("name")
                        + " | Price: " + rs.getDouble("price")
                        + " | Stock: " + rs.getInt("stock")
                    );
                } else {
                    System.out.println("Product not found!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Product search failed!");
            e.printStackTrace();
        }
    }

    public boolean updateProductDetails(int id, String name,
                                        double price, double discount,
                                        int stock, String category,
                                        String description, String imagePath) {
        String sql = """
            UPDATE Products
            SET name=?, price=?, discount=?, stock=?,
                category=?, description=?, image_path=?
            WHERE product_id=?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setDouble(3, discount);
            ps.setInt(4, stock);
            ps.setString(5, category);
            ps.setString(6, description);
            ps.setString(7, imagePath);
            ps.setInt(8, id);

            boolean success = ps.executeUpdate() > 0;

            if (success && stock > 0) {
                new QueueManager().processQueueForProduct(id);
            }
            return success;

        } catch (SQLException e) {
            System.out.println("Product update failed!");
            e.printStackTrace();
            return false;
        }
    }

    public void updateProduct(int id, double price, int stock) {
        if (price < 0 || stock < 0) {
            System.out.println("Invalid price or stock!");
            return;
        }

        String sql = "UPDATE Products SET price=?, stock=? WHERE product_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, stock);
            ps.setInt(3, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Product updated successfully!");
                if (stock > 0) {
                    new QueueManager().processQueueForProduct(id);
                }
            } else {
                System.out.println("Product not found!");
            }
        } catch (SQLException e) {
            System.out.println("Product update failed!");
            e.printStackTrace();
        }
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            boolean success = ps.executeUpdate() > 0;

            if (success) {
                System.out.println("Product deleted successfully!");
            }
            return success;

        } catch (SQLException e) {
            System.out.println("Delete failed: product may be used in orders or queue.");
            e.printStackTrace();
            return false;
        }
    }
}
