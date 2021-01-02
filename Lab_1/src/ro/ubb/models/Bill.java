package ro.ubb.models;


import java.util.HashMap;

/**
 * The class which represents the bill created by a user
 */
public class Bill {
    private HashMap<Product, Integer> productList;
    private double totalAmount;

    public Bill() {
        this.productList = new HashMap<>();
        this.totalAmount = 0;
    }

    public HashMap<Product, Integer> getProductList() {
        return productList;
    }

    public void addProductBought(Product product, int amount) {
        if (amount <= 0)
            throw new RuntimeException("Can't have negative amount!");
        totalAmount += product.price * amount;
        if (productList.containsKey(product)) {
            this.productList.put(product, this.productList.get(product) + amount);
        } else {
            productList.put(product, amount);
        }
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
