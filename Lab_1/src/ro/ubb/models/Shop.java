package ro.ubb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The shop which manages the access for the items in the storage,
 * each desired item has a lock associated to it so that at one given time
 * only one customer can access it.
 */
public class Shop {

    private final HashMap<Product, Integer> products;
    private final HashMap<Product, Integer> prodcutsRecorded;
    private final HashMap<Product, Integer> initialProductList;
    /**
     * Each item has its own mutex, so concurrent access to other items is allowed, while if a user wants the same
     * product it has to wait
     */
    private final HashMap<Product, ReentrantLock> locksForProducts;

    private final ReentrantLock billLock = new ReentrantLock();
    double totalIncome = 0;
    List<Bill> productsBought = new ArrayList<>();
    public Shop() {
        products = new HashMap<>();
        initialProductList = new HashMap<>();
        locksForProducts = new HashMap<>();
        prodcutsRecorded = new HashMap<>();
    }

    public ReentrantLock getBillLock() {
        return billLock;
    }

    public List<Bill> getProductsBought() {
        return productsBought;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public HashMap<Product, Integer> getInitialProductList() {
        return initialProductList;
    }

    /**
     * The customer adds a bill to the shop
     * Only one customer can add a bill at a given time
     * and locks this method from other customers
     * Only one bill is added, and after the operation the bill is part of the list of bills and the money is increased
     * by the amount that is on the bill and the recorded amount for each amount is decreased
     *
     * @param billToAdd the bill to add
     */
    public void addBill(Bill billToAdd) {
        billLock.lock();
        productsBought.add(billToAdd);
        totalIncome += billToAdd.getTotalAmount();
        for (Map.Entry<Product, Integer> productIntegerEntry : billToAdd.getProductList().entrySet()) {
            prodcutsRecorded.replace(
                    productIntegerEntry.getKey(),
                    prodcutsRecorded.get(productIntegerEntry.getKey()) - productIntegerEntry.getValue()
            );
        }
        billLock.unlock();
    }

    /**
     * Lock the shop in exclusive mode to verify it
     */

    /**
     * Unlock the shop in exclusive mode to verify it
     */

    public HashMap<Product, Integer> getProducts() {
        return prodcutsRecorded;
    }

    public List<Product> getProductNames() {
        return new ArrayList<>(products.keySet());
    }

    /**
     * The items are added at the initial phase of the applciation
     *
     * @param product the product to add
     * @param amount  the quantity present
     */
    public void addAProductWithAmount(Product product, int amount) {

        if (amount < 0)
            throw new RuntimeException("Invalid, can't have negative amount");
        if (this.products.containsKey(product)) {
            this.products.put(product, this.products.get(product) + amount);
            this.prodcutsRecorded.put(product, this.products.get(product) + amount);
            this.initialProductList.put(product, this.products.get(product) + amount);
        } else {
            this.products.put(product, amount);
            this.prodcutsRecorded.put(product, amount);
            this.initialProductList.put(product, amount);
            this.locksForProducts.put(product, new ReentrantLock());
        }
    }

    public HashMap<Product, ReentrantLock> getLocksForProducts() {
        return locksForProducts;
    }

    /**
     * To remove a given amount of a certain product
     *
     * @param product the given product
     * @param amount  the amount to be removed
     * @throws NotEnoughProductsException if the customer wants to remove more items than there are items
     */
    public void removeAProductWithAmount(Product product, int amount) throws NotEnoughProductsException {

        if (amount < 0)
            throw new RuntimeException("Invalid, can't have negative amount");
        if (!this.products.containsKey(product))
            throw new RuntimeException("No item of that name!");

        /*
         * The customer has to lock only the product it wants to obtain
         */
        ReentrantLock mutexForItem = locksForProducts.get(product);

        mutexForItem.lock();
        try {
            if (this.products.get(product) < amount) {
                throw new NotEnoughProductsException("You are requesting too many items");
            } else {
                this.products.replace(product, this.products.get(product) - amount);
            }
        } finally {
            mutexForItem.unlock();
        }

    }

}
