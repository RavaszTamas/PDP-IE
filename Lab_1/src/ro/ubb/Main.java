package ro.ubb;

import ro.ubb.models.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * What needs to be in the documentation: Text file in which FUll NAME, GROUP, SEMIGROUP, NUMBER OF LAB, IMPLEMENTATION DETAILS
 * HOW DID YOU DO IT, TEST CASES, HARDWARE SPECIFICATIONS, ANY OTHER ANSWERS TO THE REQUIREMENTS GIVEN BY THE PROFESSOR
 * <p>
 * Hardware specification:
 * 8 cores, 16 threads AMD Ryzen 4900h 3.30(4.40)Ghz, 16 gb of RAM DDR4
 * <p>
 * Test with 1000 products:
 * Total program duration:
 * Test case: 1 threads -> 32 milliseconds
 * Test case: 2 threads -> 34 milliseconds
 * Test case: 4 threads -> 45 milliseconds
 * Test case: 8 threads -> 47 milliseconds
 * Test case: 16 threads -> 58 milliseconds
 * Test case: 50 threads -> 74 milliseconds
 * Test case: 100 threads -> 97 milliseconds
 * Test case: 500 threads -> 206 milliseconds
 * Test case: 1000 threads -> 379 milliseconds
 * Test case: 10000 threads -> 2108 milliseconds
 */

/**
 * The verification is performed by this class
 * the class has access to the Shop and behaves like a thread
 * It tries periodically to access the shop and verify the bills with the money the shop has
 * it stops when the execution ends
 */

class Verifier extends Thread {

    private static final double THRESHOLD = .0001; /// for floating point comparison
    Thread t;
    Shop shop;
    private boolean exit;

    Verifier(String threadName, Shop shop) {
        this.shop = shop;
        t = new Thread(this, threadName);
        System.out.println("New thread: " + t);
        exit = false;
        t.start();

    }

    static void performCheck(Shop shop, double threshold) {


        List<Product> products = shop.getProductNames();
        Map<Product, ReentrantLock> productReentrantLockMap = shop.getLocksForProducts();
        ReentrantLock billLock = shop.getBillLock();

        for (Product product : products) {
            productReentrantLockMap.get(product).lock();
        }
        billLock.lock();
        List<Bill> productsBought = shop.getProductsBought();

        HashMap<Product, Integer> currentStorage = shop.getProducts();

        HashMap<Product, Integer> initialProductList = shop.getInitialProductList();

        double totalIncomeFromBills = productsBought.stream().mapToDouble(Bill::getTotalAmount).sum();

        double totalIncomeRecordedByShop = shop.getTotalIncome();


        String stringToOutput = "\n\n\n\n\nRecorded in store: " + totalIncomeRecordedByShop + "\n";
        stringToOutput += "Recorded in bills: " + totalIncomeFromBills + "\n";


        for (Map.Entry<Product, Integer> entry : initialProductList.entrySet()) {


            Integer amountOfSpecificProductSold = productsBought
                    .stream()
                    .mapToInt(e -> {
                        Integer value = e.getProductList().get(entry.getKey());
                        return Objects.requireNonNullElse(value, 0);
                    }).sum();

            Integer amountInStorage = currentStorage.get(entry.getKey());
            if (entry.getValue() != amountInStorage + amountOfSpecificProductSold) {
                stringToOutput += "Inconsistency found with product: " + entry.getKey().getNameOfPProduct() + " initial: " + entry.getValue() + ", actual: " + (amountInStorage + amountOfSpecificProductSold) + "\n";
            }
        }

        if (Math.abs(totalIncomeFromBills - totalIncomeRecordedByShop) > threshold) {
            stringToOutput += "Verification failed, some money went missing or maybe floating point error..., please verify it manually\n";
        } else {
            stringToOutput += "Successful verification of costs and the inventory\n\n\n\n";
        }
        System.out.println(stringToOutput);

        billLock.unlock();
        for (Product product : products) {
            productReentrantLockMap.get(product).unlock();
        }

    }

    @Override
    public void run() {
        while (!exit) {
            verifyTheTransactions(this.shop);
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopTheProcess() {
        System.out.println("verifier stops");
        exit = true;
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void verifyTheTransactions(Shop shop) {

        performCheck(shop, THRESHOLD);

    }

}


public class Main {


    public static final String dataLocation = "src\\ro\\ubb\\data\\products.txt"; // location of the records of products in the shop

    public static final int QUANTITY_LIMIT = 10; // the number of items a customer can have at max on his/her shopping list

    public static final int NUMBER_OF_THREADS = 1; //  the number of customers

    private static final double THRESHOLD = .0001; // for floating point comparison


    public static Random randomGenerator = new Random(System.currentTimeMillis());

    public static void simulateShop(TestSample inputParameters, int testNumber) {
        writeInFile(inputParameters);

        Shop shop = new Shop();
        readDataFromFile(shop);


        List<Customer> customers = new ArrayList<>();
        List<Product> products = shop.getProductNames();
        List<Thread> threads = new ArrayList<>();

        //random initialization for the customers
        for (int i = 0; i < inputParameters.getNumberOfThreads(); i++) {
            Customer newCustomer = new Customer(shop, "customer-" + i);

            int productsToChoose = randomGenerator.nextInt(products.size()) + 1;

            for (int j = 0; j < productsToChoose; j++) {
                int productIndex = randomGenerator.nextInt(products.size());
                int quantity = randomGenerator.nextInt(QUANTITY_LIMIT) + 1;
                newCustomer.addItemToShoppingList(products.get(productIndex), quantity);
            }

            customers.add(newCustomer);
        }

        customers.forEach(customer -> threads.add(new Thread(customer)));

        Verifier verifier = new Verifier("Verifier", shop);

        for (Thread thread : threads) {
            thread.start();
        }

        long startTime = System.currentTimeMillis();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        verifier.stopTheProcess();

        verifyTheTransactions(shop);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        if (testNumber > 0)
            writeResultToFile(totalTime, inputParameters, testNumber);

        System.out.println("=========================================================================================");

        System.out.println(totalTime + " milliseconds");
    }

    private static void writeResultToFile(long totalTime, TestSample inputParameters, int testCount) {

        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter("testResult.txt", true)))) {
            output.println("The number " + testCount + " test, with thread count of " + inputParameters.getNumberOfThreads() + " and product count of " + inputParameters.getNumberOfProducts() + " the resulting time is: " + totalTime + " milliseconds");
        } catch (IOException exception) {
            System.out.println("error writing result");
        }

    }

    public static void main(String[] args) {


        List<Integer> threadNumbers = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8196, 10000));
        List<Integer> productNumbers = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50, 150, 350, 500, 750, 1250, 2000, 2500, 5000, 10000, 20000));

        List<TestSample> testSamples = new ArrayList<>();

        for (Integer threadNum : threadNumbers) {
            for (Integer prodNum : productNumbers) {
                testSamples.add(new TestSample(threadNum, prodNum));
            }
        }


        for (int i = 0 ; i < testSamples.size(); i++) {
            simulateShop(testSamples.get(i),i+1);
        }

        //simulateShop(new TestSample(100, 1000), -1);


    }

    private static void verifyTheTransactions(Shop shop) {

        Verifier.performCheck(shop, THRESHOLD);

    }

    public static void readDataFromFile(Shop shop) {


        try (BufferedReader dataFile = new BufferedReader(new FileReader(dataLocation))) {
            String line;
            while ((line = dataFile.readLine()) != null) {
                String[] params = line.split(";");
                shop.addAProductWithAmount(new Product(params[0], Double.parseDouble(params[1])), Integer.parseInt(params[2]));
            }
        } catch (IOException exception) {
            System.out.println("Can't read the file, shutting down");
            System.exit(1);
        }
    }

    //generating test data
    private static void writeInFile(TestSample testSample) {
        int i = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataLocation))) {
            while (i < testSample.getNumberOfProducts()) {
                Random r = new Random();
                Integer amount = r.nextInt();
                if (amount < 0)
                    amount = (amount * -1) % 100;
                else
                    amount %= 100;
                String s = new RandomString().generateRandomString() + ';' + r.nextDouble() * 10.0 + ';' + amount + '\n';
                writer.write(s);
                i += 1;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


}

class RandomString {
    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 10;

    String generateRandomString() {
        StringBuilder randStr = new StringBuilder();
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
}
