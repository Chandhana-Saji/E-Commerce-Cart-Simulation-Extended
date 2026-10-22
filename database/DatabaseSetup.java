
package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {

        String customers = """
            CREATE TABLE IF NOT EXISTS Customers (
                customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                phone TEXT,
                address TEXT,
                password_hash TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String admins = """
            CREATE TABLE IF NOT EXISTS Admins (
                admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            )
            """;

        String products = """
            CREATE TABLE IF NOT EXISTS Products (
                product_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL NOT NULL CHECK(price >= 0),
                discount REAL NOT NULL DEFAULT 0
                    CHECK(discount >= 0 AND discount <= 100),
                stock INTEGER NOT NULL DEFAULT 0
                    CHECK(stock >= 0),
                category TEXT NOT NULL,
                description TEXT,
                image_path TEXT
            )
            """;

        String orders = """
            CREATE TABLE IF NOT EXISTS Orders (
                order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                order_date TEXT DEFAULT CURRENT_TIMESTAMP,
                total_amount REAL NOT NULL
                    CHECK(total_amount >= 0),
                status TEXT NOT NULL DEFAULT 'PLACED',
                FOREIGN KEY (customer_id)
                    REFERENCES Customers(customer_id)
            )
            """;

        String orderItems = """
            CREATE TABLE IF NOT EXISTS Order_Items (
                order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                unit_price REAL NOT NULL CHECK(unit_price >= 0),
                discount_at_purchase REAL NOT NULL DEFAULT 0
                    CHECK(discount_at_purchase >= 0
                    AND discount_at_purchase <= 100),
                FOREIGN KEY (order_id)
                    REFERENCES Orders(order_id) ON DELETE CASCADE,
                FOREIGN KEY (product_id)
                    REFERENCES Products(product_id)
            )
            """;

        String waitingQueue = """
            CREATE TABLE IF NOT EXISTS Waiting_Queue (
                queue_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1
                    CHECK(quantity > 0),
                status TEXT NOT NULL DEFAULT 'WAITING'
                    CHECK(status IN (
                        'WAITING', 'FULFILLED', 'CANCELLED'
                    )),
                joined_at TEXT DEFAULT CURRENT_TIMESTAMP,
                fulfilled_at TEXT,
                FOREIGN KEY (customer_id)
                    REFERENCES Customers(customer_id),
                FOREIGN KEY (product_id)
                    REFERENCES Products(product_id)
            )
            """;

        String cartItems = """
            CREATE TABLE IF NOT EXISTS Cart_Items (
                cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                UNIQUE(customer_id, product_id),
                FOREIGN KEY (customer_id)
                    REFERENCES Customers(customer_id),
                FOREIGN KEY (product_id)
                    REFERENCES Products(product_id)
            )
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             Statement statement =
                 connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            statement.execute(customers);
            statement.execute(admins);
            statement.execute(products);
            statement.execute(orders);
            statement.execute(orderItems);
            statement.execute(waitingQueue);
            statement.execute(cartItems);

            System.out.println(
                "All 7 tables created successfully!"
            );

        } catch (SQLException e) {
            System.out.println("Table creation failed!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}
