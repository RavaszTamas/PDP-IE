package ro.ubb.models;

import java.util.Objects;


/**
 * The representation for a product
 */
public class Product {
    double price;
    String nameOfPProduct;
    boolean isLocked;

    public Product(String nameOfPProduct, double price) {
        this.price = price;
        this.nameOfPProduct = nameOfPProduct;
        this.isLocked = false;
    }

    synchronized public boolean lock() {
        try {
            while (this.isLocked) {
                System.out.println("waiting");
                wait();
                System.out.println("awake");

            }
            this.isLocked = true;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    synchronized public void unlock() {
        this.isLocked = false;
        notify();
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNameOfPProduct() {
        return nameOfPProduct;
    }

    public void setNameOfPProduct(String nameOfPProduct) {
        this.nameOfPProduct = nameOfPProduct;
    }

    @Override
    public String toString() {
        return "Product{" +
                "price=" + price +
                ", nameOfPProduct='" + nameOfPProduct + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.price, price) == 0 &&
                nameOfPProduct.equals(product.nameOfPProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, nameOfPProduct);
    }
}
