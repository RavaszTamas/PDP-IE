package ro.ubb.models;

import java.util.HashMap;
import java.util.Map;

/**
 * The customer is the independent process that executes the shopping
 */
public class Customer implements Runnable {

    private final Shop shop;
    private final String name;


    HashMap<Product, Integer> shoppingList = new HashMap<>();

    public Customer(Shop shop, String name) {
        this.shop = shop;
        this.name = name;
    }

    public void addItemToShoppingList(Product product, int amount) {
        shoppingList.put(product, amount);
    }

    @Override
    public void run() {

        Bill billOfCustomer = new Bill();
        for (Map.Entry<Product, Integer> entryInList : shoppingList.entrySet()) {
            try {
                shop.removeAProductWithAmount(entryInList.getKey(), entryInList.getValue());
                System.out.println(name + " has found the product \"" + entryInList.getKey().getNameOfPProduct() + "\" with the correct amount " + entryInList.getValue());
                billOfCustomer.addProductBought(entryInList.getKey(), entryInList.getValue());

            } catch (NotEnoughProductsException ex) {
                System.out.println(name + " has not found product \"" + entryInList.getKey().getNameOfPProduct() + "\" with amount " + entryInList.getValue());
            }
        }
        /** The customer pays the bill*/
        shop.addBill(billOfCustomer);
        System.out.println(name + " pays the bill");

    }
}
