
package database;

public class CustomerDAOTest {
    public static void main(String[] args) {

        CustomerDAO dao = new CustomerDAO();

        boolean registered = dao.registerCustomer(
                "Test Customer",
                "testcustomer@example.com",
                "9876543210",
                "Test Address",
                "test123"
        );

        if (registered) {
            System.out.println("Registration test passed!");

            boolean loggedIn = dao.loginCustomer(
                    "testcustomer@example.com",
                    "test123"
            );

            System.out.println("Login test result: " + loggedIn);
        }
    }
}

