
import database.ProductDAO;

public class ProductManager {

    private ProductDAO productDAO = new ProductDAO();

    public void addProduct(Product product) {
        productDAO.addProduct(
            product.getProductId(),
            product.getName(),
            product.getPrice(),
            product.getDiscount(),
            product.getStock()
        );
    }

    public void displayAllProducts() {
        productDAO.displayProducts();
    }

    public void searchProduct(int productId) {
        productDAO.searchProduct(productId);
    }

    public void viewProducts() {
        displayAllProducts();
    }

    public void updateProduct(int productId, double price, int stock) {
        productDAO.updateProduct(productId, price, stock);
    }

    public void removeProduct(int productId) {
        productDAO.deleteProduct(productId);
    }
}
