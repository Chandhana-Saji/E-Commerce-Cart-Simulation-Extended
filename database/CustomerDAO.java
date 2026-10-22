
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    public boolean registerCustomer(String name, String email,
                                    String phone, String address,
                                    String password) {

        String sql = "INSERT INTO Customers "
                + "(name, email, phone, address, password_hash) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, password);

            ps.executeUpdate();
            System.out.println("Customer registered successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean loginCustomer(String email, String password) {

        String sql = "SELECT customer_id, name FROM Customers "
                + "WHERE email = ? AND password_hash = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Welcome, " + rs.getString("name") + "!");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }

        System.out.println("Invalid email or password.");
        return false;
    }
    
public int getCustomerId(String email, String password) {
    String sql = "SELECT customer_id FROM Customers "
               + "WHERE email = ? AND password_hash = ?";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, email);
        ps.setString(2, password);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        }

    } catch (SQLException e) {
        System.out.println("Error getting customer ID: "
                + e.getMessage());
    }

    return -1;
}

}
