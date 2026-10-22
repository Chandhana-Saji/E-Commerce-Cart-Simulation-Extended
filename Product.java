public class Product {

    private int productId;
    private String name;
    private double price;
    private double discount;
    private int stock;

    public Product(int productId, String name, double price, double discount, int stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.stock = stock;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscount() {
        return discount;
    }

    public int getStock() {
        return stock;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double calculateFinalPrice() {
        return price - (price * discount / 100);
    }

    public void displayDetails() {
        System.out.println("Product ID: " + productId);
        System.out.println("Name: " + name);
        System.out.println("Price: ₹" + price);
        System.out.println("Discount: " + discount + "%");
        System.out.println("Final Price: ₹" + calculateFinalPrice());
        System.out.println("Stock: " + stock);
    }
}