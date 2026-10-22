
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    // Ella products-um retrieve cheyyunnu
    public void getAllProducts() {
        String sql = "SELECT * FROM Products ORDER BY product_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("product_id")
                    + " | Name: " + rs.getString("name")
                    + " | Price: " + rs.getDouble("price")
                    + " | Discount: " + rs.getDouble("discount") + "%"
                    + " | Stock: " + rs.getInt("stock")
                    + " | Category: " + rs.getString("category")
                );
            }

        } catch (SQLException e) {
            System.out.println("Unable to retrieve products!");
            e.printStackTrace();
        }
    }

    // Product add cheyyunnu
    public void addProduct(int id, String name, double price,
                           double discount, int stock) {

        addProductDetails(id, name, price, discount, stock,
                          "General", "", "");
    }

    public void addProductDetails(int id, String name,
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

            ps.executeUpdate();
            System.out.println("Product added successfully!");

            // Stock undenkil existing waiting queue process cheyyuka
            if (stock > 0) {
                new QueueManager().processQueueForProduct(id);
            }

        } catch (SQLException e) {
            System.out.println("Unable to add product!");
            e.printStackTrace();
        }
    }

    // Products display cheyyunnu
    public void displayProducts() {
        getAllProducts();
    }

    // Product ID use cheythu search cheyyunnu
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
                        + " | Discount: " + rs.getDouble("discount")
                        + "%"
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

    // Product details update cheyyunnu
    public void updateProductDetails(int id, String name,
                                    double price, double discount,
                                    int stock, String category,
                                    String description, String imagePath) {

        String sql = """
            UPDATE Products
            SET name = ?, price = ?, discount = ?, stock = ?,
                category = ?, description = ?, image_path = ?
            WHERE product_id = ?
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

            int rows = ps.executeUpdate();

            if (rows > 0) {
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

    // Price, stock update cheyyunnu
    public void updateProduct(int id, double price, int stock) {

        if (price < 0 || stock < 0) {
            System.out.println("Invalid price or stock!");
            return;
        }

        String sql = """
            UPDATE Products
            SET price = ?, stock = ?
            WHERE product_id = ?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, stock);
            ps.setInt(3, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product updated successfully!");

                // Stock update kazhinjal queue automatic processing
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

    // Product delete cheyyunnu
    public void deleteProduct(int id) {

        String sql = "DELETE FROM Products WHERE product_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Product not found!");
            }

        } catch (SQLException e) {
            System.out.println(
                "Product delete failed. Product may be used in orders or queue."
            );
            e.printStackTrace();
        }
    }
}
