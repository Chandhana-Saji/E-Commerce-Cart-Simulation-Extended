

public class Gadget extends Product {

    private String brand;
    private String warranty;

    public Gadget(int productId, String name, double price,
                  double discount, int stock,
                  String brand, String warranty) {

        super(productId, name, price, discount, stock);
        this.brand = brand;
        this.warranty = warranty;
    }

    public String getBrand() {
        return brand;
    }

    public String getWarranty() {
        return warranty;
    }
}
