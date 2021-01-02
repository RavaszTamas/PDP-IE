package ro.ubb.pdp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImplementationOfAlgorithms {

    /**
     * The sequential solution for the multiplication of polynomials
     * In this case the each new item is calculated sequentially
     * Time complexity: O(n^2)
     *
     * @param p1 - Polynomial
     * @param p2 - Polynomial
     * @return the resulting polynomial after computing the solution
     */
    public static Polynomial multiplicationSequentialForm(Polynomial p1, Polynomial p2) {
        int sizeOfResultCoefficientList = p1.getDegree() + p2.getDegree() + 1;
        List<Integer> result = new ArrayList<>();
        //initialize result polynomial with coefficients = 0
        for (int i = 0; i < sizeOfResultCoefficientList; i++) {
            result.add(0);
        }

        for (int i = 0; i < p1.getCoefficients().size(); i++) {
            for (int j = 0; j < p2.getCoefficients().size(); j++) {
                int indexOfResult = i + j;
                int value = p1.getCoefficients().get(i) * p2.getCoefficients().get(j);
                result.set(indexOfResult, result.get(indexOfResult) + value);
            }
        }
        return new Polynomial(result);
    }

    /**
     * Simple multiplication operation over 2 polynomials, using threads
     * Time complexity: O(n^2)
     * This is similar to the sequential one but in this case all items are calculated in a parallel way
     * Each thread is given a part of the resulting polynomial to be calculated, like a trackt of numbers
     * @param first - Polynomial
     * @param second - Polynomial
     * @return the resulted polynomials after the multiplication
     */
    public static Polynomial multiplicationParallelizedForm(Polynomial first, Polynomial second, int nrOfThreads) throws
            InterruptedException {
        //initialize result polynomial with coefficients = 0
        int sizeOfResultCoefficientList = first.getDegree() + second.getDegree() + 1;
        List<Integer> coefficients = IntStream.of(new int[sizeOfResultCoefficientList]).boxed().collect(Collectors
                .toList());
        Polynomial result = new Polynomial(coefficients);

        //allocate for each thread the number of items to be executed
        // if the ratio is smaller than 1 each thread gets one position

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(nrOfThreads);
        int step = result.getLength() / nrOfThreads;
        if (step == 0) {
            step = 1;
        }
        int end;
        // for each step one thread is launched where a step is calculated before
        // then the thread is launched

        for (int i = 0; i < result.getLength(); i += step) {

            end = i + step;
            // the task is submitted
            // and it will calculate the result
            MultiplyPartOfPolinomial multiplicationTask = new MultiplyPartOfPolinomial(i, end, first, second, result);

            executorService.execute(multiplicationTask);
        }

        executorService.shutdown();
        // this is to wait for the service to stop
        executorService.awaitTermination(500, TimeUnit.SECONDS);
        // result is returned
        return result;
    }

    /**
     * Sequential version of the karatsuba algorithm, the polynomials are split into lower and higher part
     * the parts have equal length if even in case of odd one part is longer by one degree, for the lower part the algorithm
     * is called recursibely and for the upper part too. For the sums of the lower and higher parts the algorithm is
     * also called recursively when these are calculated the result is calculated based on the formula:
     * (P1(X)+P2(X)) * (Q1(X)+Q2(X)) - 1(X)* Q1(X) - P2(X)*Q2(X)
     * @param first Polynomial
     * @param second Polynomial
     * @return Polynomial, the result
     */
    public static Polynomial multiplicationKaratsubaSequentialForm(Polynomial first, Polynomial second) {
        //sequential version of the karatsuba algorithm
        // if the degree is too small simple algorithm is called for the polynomial
        if (first.getDegree() < 2 || second.getDegree() < 2) {
            return multiplicationSequentialForm(first, second);
        }

        // splitting the polynomial into lower and higher parts for each polynomial
        int len = Math.max(first.getDegree(), second.getDegree()) / 2;
        Polynomial lowFirst = new Polynomial(first.getCoefficients().subList(0, len));
        Polynomial heightFirst = new Polynomial(first.getCoefficients().subList(len, first.getLength()));
        Polynomial lowSecond = new Polynomial(second.getCoefficients().subList(0, len));
        Polynomial highSecond = new Polynomial(second.getCoefficients().subList(len, second.getLength()));

        // the result is called recursively to the lower parts
        Polynomial z1 = multiplicationKaratsubaSequentialForm(lowFirst, lowSecond);
        // the result is called recursively to the sum of the lower and higher parts
        Polynomial z2 = multiplicationKaratsubaSequentialForm(addPolynomials(lowFirst, heightFirst), addPolynomials(lowSecond, highSecond));
        // the result is called recursively to the higher parts
        Polynomial z3 = multiplicationKaratsubaSequentialForm(heightFirst, highSecond);

        //calculate the final result
        //shift is to move the higher part to it's position in the polynomial
        Polynomial r1 = shiftPolynomial(z3, 2 * len);
        //subtracting the a*c and b*d from the result of z2
        Polynomial r2 = shiftPolynomial(subtractPolynomial(subtractPolynomial(z2, z3), z1), len);
        //summing up the final result
        return addPolynomials(addPolynomials(r1, r2), z1);
    }

    /**
     * Parallel version of the karatsuba algorithm, the polynomials are split into lower and higher part
     * the parts have equal length if even in case of odd one part is longer by one degree, for the lower part the algorithm
     * is called recursively and for the upper part too. For the sums of the lower and higher parts the algorithm is
     * also called recursively when these are calculated the result is calculated based on the formula:
     * (P1(X)+P2(X)) * (Q1(X)+Q2(X)) - 1(X)* Q1(X) - P2(X)*Q2(X)
     * Parallelization occurs when the lower, higher and middle parts are calculated
     * The depth parameter is added so that the virtual machine won't crash
     * @param first  Polynomial
     * @param second Polynomial
     * @param depth Integer, maximum depth algorithm can go to
     * @return Polynomial, result polynomial
     * @throws ExecutionException just an error
     * @throws InterruptedException just an error
     */
    public static Polynomial multiplicationKaratsubaParallelizedForm(Polynomial first, Polynomial second, int depth)
            throws ExecutionException, InterruptedException {

        //Because of a java issue i had to put this here
        //The number of possible threads is exceded and the virtual machine crashes
        if(depth > 5){
            return multiplicationKaratsubaSequentialForm(first,second);
        }
        // if the degree is too small simple algorithm is called for the polynomial
        if (first.getDegree() < 2 || second.getDegree() < 2) {
            return multiplicationKaratsubaSequentialForm(first, second);
        }
        // the splitting is still sequential
        int len = Math.max(first.getDegree(), second.getDegree()) / 2;
        Polynomial lowFirst = new Polynomial(first.getCoefficients().subList(0, len));
        Polynomial highFirst = new Polynomial(first.getCoefficients().subList(len, first.getLength()));
        Polynomial lowSecond = new Polynomial(second.getCoefficients().subList(0, len));
        Polynomial highSecond = new Polynomial(second.getCoefficients().subList(len, second.getLength()));

        //the summations and multiplications are called asynchronously but are essentially the same functions
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Callable<Polynomial> task1 = () -> multiplicationKaratsubaParallelizedForm(lowFirst, lowSecond,depth+1);
        Callable<Polynomial> task2 = () -> multiplicationKaratsubaParallelizedForm(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst), ImplementationOfAlgorithms
                .addPolynomials(lowSecond, highSecond),depth+1);
        Callable<Polynomial> task3 = () -> multiplicationKaratsubaParallelizedForm(highFirst, highSecond,depth+1);

        Future<Polynomial> f1 = executor.submit(task1);
        Future<Polynomial> f2 = executor.submit(task2);
        Future<Polynomial> f3 = executor.submit(task3);

        executor.shutdown();

        //the results are obtained after the multiplication is finished
        Polynomial z1 = f1.get();
        Polynomial z2 = f2.get();
        Polynomial z3 = f3.get();

        //to make sure everything stops
        executor.awaitTermination(60, TimeUnit.SECONDS);

        //calculate the final result which is called in a seqential way
        Polynomial r1 = shiftPolynomial(z3, 2 * len);
        Polynomial r2 = shiftPolynomial(subtractPolynomial(subtractPolynomial(z2, z3), z1), len);
        return addPolynomials(addPolynomials(r1, r2), z1);
    }


    ///
    ///These are only helper functions to make the code more readable
    ///
    /**
     * Will increase the degree of the polynomial and will add a number of {offset} of 0s for the coefficients with
     * the smallest degree.
     *
     * @param p      - Polynomial
     * @param offset - Integer value
     * @return - the shifted polynomial
     */
    public static Polynomial shiftPolynomial(Polynomial p, int offset) {
        List<Integer> coefficients = new ArrayList<>();
        for (int i = 0; i < offset; i++) {
            coefficients.add(0);
        }
        for (int i = 0; i < p.getLength(); i++) {
            coefficients.add(p.getCoefficients().get(i));
        }
        return new Polynomial(coefficients);
    }

    /**
     * Simple sequenctial addition operation over 2 polynomials.
     *
     * @param p1 - Polynomial
     * @param p2 - Polynomial
     * @return the resulted polynomials after the addition
     */
    public static Polynomial addPolynomials(Polynomial p1, Polynomial p2) {
        int minDegree = Math.min(p1.getDegree(), p2.getDegree());
        int maxDegree = Math.max(p1.getDegree(), p2.getDegree());
        List<Integer> coefficients = new ArrayList<>(maxDegree + 1);

        //Add the 2 polynomials
        for (int i = 0; i <= minDegree; i++) {
            coefficients.add(p1.getCoefficients().get(i) + p2.getCoefficients().get(i));
        }

        addRemainingCoefficients(p1, p2, minDegree, maxDegree, coefficients);

        return new Polynomial(coefficients);
    }

    /**
     * Complete the remaining part with the coefficients, from a certain polynomial (the one with the bigger degree)
     */
    private static void addRemainingCoefficients(Polynomial p1, Polynomial p2, int minDegree, int maxDegree,
                                                 List<Integer> coefficients) {
        if (minDegree != maxDegree) {
            if (maxDegree == p1.getDegree()) {
                for (int i = minDegree + 1; i <= maxDegree; i++) {
                    coefficients.add(p1.getCoefficients().get(i));
                }
            } else {
                for (int i = minDegree + 1; i <= maxDegree; i++) {
                    coefficients.add(p2.getCoefficients().get(i));
                }
            }
        }
    }

    /**
     * Simple sequenctial subtraction operation over 2 polynomials.
     *
     * @param p1 - Polynomial
     * @param p2 - Polynomial
     * @return the resulted polynomials after the addition
     */
    public static Polynomial subtractPolynomial(Polynomial p1, Polynomial p2) {
        int minDegree = Math.min(p1.getDegree(), p2.getDegree());
        int maxDegree = Math.max(p1.getDegree(), p2.getDegree());
        List<Integer> coefficients = new ArrayList<>(maxDegree + 1);

        //Subtract the 2 polynomials
        for (int i = 0; i <= minDegree; i++) {
            coefficients.add(p1.getCoefficients().get(i) - p2.getCoefficients().get(i));
        }

        addRemainingCoefficients(p1, p2, minDegree, maxDegree, coefficients);

        //remove coefficients starting from biggest power if coefficient is 0

        int i = coefficients.size() - 1;
        while (coefficients.get(i) == 0 && i > 0) {
            coefficients.remove(i);
            i--;
        }

        return new Polynomial(coefficients);
    }

    public static Polynomial multiplyFromBeginToEnd(Polynomial p1, Polynomial p2, int begin, int end) {

        int sizeOfResultCoefficientList = p1.getDegree() + p2.getDegree() + 1;
        List<Integer> result = new ArrayList<>();
        //initialize result polynomial with coefficients = 0
        for (int i = 0; i < sizeOfResultCoefficientList; i++) {
            result.add(0);
        }

        for (int i = begin; i < end; i++) {
            for (int j = 0; j < p2.getCoefficients().size(); j++) {
                int indexOfResult = i + j;
                int value = p1.getCoefficients().get(i) * p2.getCoefficients().get(j);
                result.set(indexOfResult, result.get(indexOfResult) + value);
            }
        }
        return new Polynomial(result);

    }
}
