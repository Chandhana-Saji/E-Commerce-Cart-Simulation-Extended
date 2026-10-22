
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseConnection {

    private static final Path DB_PATH =
        Paths.get("database", "ecommerce.db")
             .toAbsolutePath()
             .normalize();

    private static final String URL =
        "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection()
            throws SQLException {

        try {
            Files.createDirectories(DB_PATH.getParent());
        } catch (Exception e) {
            throw new SQLException(
                "Unable to create database directory: "
                + DB_PATH.getParent(), e
            );
        }

        return DriverManager.getConnection(URL);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println(
                "Database connected successfully!"
            );
            System.out.println("Database path: " + DB_PATH);
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}

