package ro.ubb.models;

public class TestSample {

    private int numberOfThreads;
    private int numberOfProducts;

    public TestSample(int numberOfThreads, int numberOfProducts) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfProducts = numberOfProducts;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfProducts() {
        return numberOfProducts;
    }

    public void setNumberOfProducts(int numberOfProducts) {
        this.numberOfProducts = numberOfProducts;
    }
}
