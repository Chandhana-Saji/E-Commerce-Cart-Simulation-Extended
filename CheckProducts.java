
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class CheckProducts {

    public static void main(String[] args) {

        String sql =
            "SELECT product_id, name, category, price, stock FROM Products";

        try (
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            int count = 0;

            System.out.println("\n--- PRODUCTS IN BUYIT DATABASE ---");

            while (result.next()) {
                count++;

                System.out.println(
                    result.getInt("product_id") + " | " +
                    result.getString("name") + " | " +
                    result.getString("category") + " | ₹" +
                    result.getDouble("price") + " | Stock: " +
                    result.getInt("stock")
                );
            }

            System.out.println("\nTotal products: " + count);

            if (count == 0) {
                System.out.println(
                    "The Products table is empty."
                );
            }

        } catch (SQLException e) {
            System.out.println("Could not read the products.");
            e.printStackTrace();
        }
    }
}

