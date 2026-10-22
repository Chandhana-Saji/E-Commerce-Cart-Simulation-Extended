

public class Footwear extends Product {

    private String brand;
    private int shoeSize;

    public Footwear(int productId, String name, double price,
                    double discount, int stock,
                    String brand, int shoeSize) {

        super(productId, name, price, discount, stock);
        this.brand = brand;
        this.shoeSize = shoeSize;
    }

    public String getBrand() {
        return brand;
    }

    public int getShoeSize() {
        return shoeSize;
    }
}
