
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ProductManager manager = new ProductManager();
        Scanner scanner = new Scanner(System.in);

        int choice;

        do {
            System.out.println("\n===== E-COMMERCE PRODUCT MANAGER =====");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Remove Product");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();

            switch (choice) {

                case 1: {
                    System.out.println("\nSelect Product Type:");
                    System.out.println("1. General Product");
                    System.out.println("2. Clothing");
                    System.out.println("3. Gadget");
                    System.out.println("4. Footwear");
                    System.out.print("Enter product type: ");

                    int type = scanner.nextInt();

                    System.out.print("Enter Product ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Enter Product Name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter Price: ");
                    double price = scanner.nextDouble();

                    System.out.print("Enter Discount: ");
                    double discount = scanner.nextDouble();

                    System.out.print("Enter Stock: ");
                    int stock = scanner.nextInt();
                    scanner.nextLine();

                    Product product;

                    switch (type) {
                        case 1:
                            product = new Product(
                                    id, name, price, discount, stock);
                            break;

                        case 2:
                            System.out.print("Enter clothing size: ");
                            String clothingSize = scanner.nextLine();

                            System.out.print("Enter material: ");
                            String material = scanner.nextLine();

                            product = new Clothing(
                                    id, name, price, discount, stock,
                                    clothingSize, material);
                            break;

                        case 3:
                            System.out.print("Enter gadget brand: ");
                            String gadgetBrand = scanner.nextLine();

                            System.out.print("Enter warranty: ");
                            String warranty = scanner.nextLine();

                            product = new Gadget(
                                    id, name, price, discount, stock,
                                    gadgetBrand, warranty);
                            break;

                        case 4:
                            System.out.print("Enter footwear brand: ");
                            String footwearBrand = scanner.nextLine();

                            System.out.print("Enter footwear size: ");
                            int footwearSize = scanner.nextInt();

                            product = new Footwear(
                                    id, name, price, discount, stock,
                                    footwearBrand, footwearSize);
                            break;

                        default:
                            System.out.println("Invalid product type!");
                            continue;
                    }

                    manager.addProduct(product);
                    break;
                }

                case 2:
                    manager.viewProducts();
                    break;

                case 3:
                    System.out.print("Enter Product ID to search: ");
                    manager.searchProduct(scanner.nextInt());
                    break;

                case 4:
                    System.out.print("Enter Product ID to update: ");
                    int updateId = scanner.nextInt();

                    System.out.print("Enter new price: ");
                    double newPrice = scanner.nextDouble();

                    System.out.print("Enter new stock: ");
                    int newStock = scanner.nextInt();

                    manager.updateProduct(updateId, newPrice, newStock);
                    break;

                case 5:
                    System.out.print("Enter Product ID to remove: ");
                    manager.removeProduct(scanner.nextInt());
                    break;

                case 6:
                    System.out.println("Exiting Product Manager...");
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }

        } while (choice != 6);

        scanner.close();
    }
}
