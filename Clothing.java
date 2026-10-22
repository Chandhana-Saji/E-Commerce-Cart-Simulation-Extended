

public class Clothing extends Product {

    private String size;
    private String material;

    public Clothing(int productId, String name, double price,
                    double discount, int stock,
                    String size, String material) {
        super(productId, name, price, discount, stock);
        this.size = size;
        this.material = material;
    }

    public String getSize() {
        return size;
    }

    public String getMaterial() {
        return material;
    }
}
