
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    public int authenticate(String username, String password) {

        String sql =
            "SELECT admin_id FROM Admins " +
            "WHERE username = ? AND password_hash = ?";

        try (Connection connection =
                    DatabaseConnection.getConnection();
             PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("admin_id");
                }
            }

        } catch (SQLException e) {
            System.out.println("Admin login failed:");
            e.printStackTrace();
        }

        return -1;
    }
}
