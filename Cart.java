
import java.util.ArrayList;

public class Cart {

    private ArrayList<CartItem> items;

    public Cart() {
        items = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        if (quantity <= 0) {
            System.out.println("Invalid quantity!");
            return;
        }

        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        items.add(new CartItem(product, quantity));
    }

    public void removeItem(int productId) {
        items.removeIf(item ->
            item.getProduct().getProductId() == productId
        );
    }

    public double calculateSubtotal() {
        double subtotal = 0;

        for (CartItem item : items) {
            subtotal += item.getTotalPrice();
        }

        return subtotal;
    }

    public void displayCart() {
        if (items.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }

        for (CartItem item : items) {
            System.out.println(
                item.getProduct().getName()
                + " | Quantity: " + item.getQuantity()
                + " | Total: " + item.getTotalPrice()
            );
        }

        System.out.println("Final Bill: Rs. " + calculateSubtotal());
    }
public ArrayList<CartItem> getItems() {
    return new ArrayList<>(items);
}
}
