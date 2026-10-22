
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReplaceProducts {

    public static void main(String[] args) {

        String deleteSQL =
                "DELETE FROM Products WHERE product_id = ? AND name = ?";

        String insertSQL =
                "INSERT INTO Products " +
                "(name, price, discount, stock, category, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Remove only the unwanted notebook record.
                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSQL)) {

                    deleteStatement.setInt(1, 101);
                    deleteStatement.setString(2, "notebook");

                    int deleted = deleteStatement.executeUpdate();

                    if (deleted != 1) {
                        throw new SQLException(
                                "Expected to find exactly one notebook record. " +
                                "No products were inserted."
                        );
                    }
                }

                // Insert the nine products.
                try (PreparedStatement insertStatement =
                             connection.prepareStatement(insertSQL)) {

                    addProduct(insertStatement, "Basic Crew Neck T-Shirt",
                            500, 8, 5, "Clothing",
                            "Images/Clothing/Clothing1.jpeg");

                    addProduct(insertStatement, "Casual Pullover Hoodie",
                            1642, 5, 7, "Clothing",
                            "Images/Clothing/Clothing2.jpeg");

                    addProduct(insertStatement, "Classic Blue Jeans",
                            1450, 6, 6, "Clothing",
                            "Images/Clothing/Clothing3.jpeg");

                    addProduct(insertStatement, "Nike Air Max Shoes",
                            7500, 9, 6, "Footwear",
                            "Images/Footwear/Footwear1.jpeg");

                    addProduct(insertStatement, "Crocs Classic Clog",
                            4500, 10, 5, "Footwear",
                            "Images/Footwear/Footwear2.jpeg");

                    addProduct(insertStatement, "Adidas Grand Court Sneakers",
                            5500, 4, 3, "Footwear",
                            "Images/Footwear/Footwear3.jpeg");

                    addProduct(insertStatement, "Apple AirPods Pro 2",
                            22000, 12, 3, "Gadget",
                            "Images/Gadgets/Gadget1.jpeg");

                    addProduct(insertStatement, "Samsung Galaxy Smartwatch",
                            18500, 5, 4, "Gadget",
                            "Images/Gadgets/Gadget2.jpeg");

                    addProduct(insertStatement,
                            "JBL Flip Series Bluetooth Speaker",
                            10200, 7, 5, "Gadget",
                            "Images/Gadgets/Gadget3.jpeg");
                }

                connection.commit();

                System.out.println(
                        "Success! The notebook was replaced with nine products."
                );

            } catch (SQLException e) {
                connection.rollback();
                System.out.println(
                        "Operation failed. Changes were rolled back."
                );
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database operation failed:");
            e.printStackTrace();
        }
    }

    private static void addProduct(
            PreparedStatement statement,
            String name,
            double price,
            double discount,
            int stock,
            String category,
            String imagePath
    ) throws SQLException {

        statement.setString(1, name);
        statement.setDouble(2, price);
        statement.setDouble(3, discount);
        statement.setInt(4, stock);
        statement.setString(5, category);
        statement.setString(6, imagePath);

        statement.executeUpdate();
    }
}

