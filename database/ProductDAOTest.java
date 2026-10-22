
package database;

public class ProductDAOTest {
    public static void main(String[] args) {

        ProductDAO dao = new ProductDAO();

        System.out.println("Before delete:");
        dao.displayProducts();

        dao.deleteProduct(1001);

        System.out.println("After delete:");
        dao.displayProducts();
    }
}