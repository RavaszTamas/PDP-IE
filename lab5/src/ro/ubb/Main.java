package ro.ubb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Polynomial firstPolynomial = new Polynomial(0);
        Polynomial secondPolynomial = new Polynomial(0);

        System.out.println("first polynomial firstPolynomial:" + firstPolynomial + "\n");
        System.out.println("second polynomial secondPolynomial" + secondPolynomial + "\n");

        System.out.println(multiplicationSimpleNonParallel(firstPolynomial, secondPolynomial).toString() + "\n");
        System.out.println(multiplicationSimpleParallel(firstPolynomial, secondPolynomial).toString() + "\n");
        System.out.println(multiplicationKaratsubaSimple(firstPolynomial, secondPolynomial).toString() + "\n");
        System.out.println(multiplicationKaratsubaParallel(firstPolynomial, secondPolynomial).toString() + "\n");

//        runTests();
    }

    private static void runTests() {

        var testCases = IntStream.range(11,21).boxed().map(item -> item*1000).collect(Collectors.toList());

        for(Integer test:testCases){
            try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("testResults.txt",true)))) {
                Polynomial firstPolynomial = new Polynomial(test);
                Polynomial secondPolynomial = new Polynomial(test);
                writer.println("Test with size: " + test);
                long startingTime = System.currentTimeMillis();
                Polynomial result1 = ImplementationOfAlgorithms.multiplicationSequentialForm(firstPolynomial, secondPolynomial);
                long endingTime = System.currentTimeMillis();
                writer.println("simple sequential test:" +test+" time: " + (endingTime-startingTime) + " ms");
                startingTime = System.currentTimeMillis();
                Polynomial result2 = ImplementationOfAlgorithms.multiplicationParallelizedForm(firstPolynomial, secondPolynomial, 5);
                 endingTime = System.currentTimeMillis();
                writer.println("simple parallel test:" +test+" time: " + (endingTime-startingTime) + " ms");

                startingTime = System.currentTimeMillis();
                Polynomial result3 = ImplementationOfAlgorithms.multiplicationKaratsubaSequentialForm(firstPolynomial, secondPolynomial);
                 endingTime = System.currentTimeMillis();
                writer.println("karatsuba sequential test:" +test+" time: " + (endingTime-startingTime) + " ms");

                startingTime = System.currentTimeMillis();
                Polynomial result4 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(firstPolynomial, secondPolynomial, 0);
                endingTime = System.currentTimeMillis();
                writer.println("karatsuba parallel test:" +test+" time: " + (endingTime-startingTime) + " ms");

            }
            catch (IOException | InterruptedException | ExecutionException ignored){

            }
        }

    }

    /**
     * The sequential solution multiplies the polynomials in the traditional way
     * @param firstPolynomial first polynomial
     * @param secondPolynomial second polynomial
     * @return the result
     */
    static Polynomial multiplicationSimpleNonParallel(Polynomial firstPolynomial, Polynomial secondPolynomial) {
        long startingTime = System.currentTimeMillis();
        Polynomial result = ImplementationOfAlgorithms.multiplicationSequentialForm(firstPolynomial, secondPolynomial);
        long endingTime = System.currentTimeMillis();
        System.out.println("The result of the simple sequential multiplication of polynomials: ");
        System.out.println("Running time time : " + (endingTime - startingTime) + " ms");
        return result;
    }
    /**
     * The parallel solution for the traditional multiplication will split up the result polynomial into chuncks which are
     * dispersed to a given amount of threads
     * @param firstPolynomial first polynomial
     * @param secondPolynomial second polynomial
     * @return the result
     */

    static Polynomial multiplicationSimpleParallel(Polynomial firstPolynomial, Polynomial secondPolynomial) throws InterruptedException {
        long startingTime = System.currentTimeMillis();
        Polynomial result = ImplementationOfAlgorithms.multiplicationParallelizedForm(firstPolynomial, secondPolynomial, 16);
        long endingTime = System.currentTimeMillis();
        System.out.println("The result of the parallel multiplication of polynomials: ");
        System.out.println("Running time time : " + (endingTime - startingTime) + " ms");
        return result;
    }

    /**
     * The karatsuba implementation without parallelization will use the
     * @param firstPolynomial first polynomial
     * @param secondPolynomial second polynomial
     * @return the result
     */
    static Polynomial multiplicationKaratsubaSimple(Polynomial firstPolynomial, Polynomial secondPolynomial) {
        long startingTime = System.currentTimeMillis();
        Polynomial result = ImplementationOfAlgorithms.multiplicationKaratsubaSequentialForm(firstPolynomial, secondPolynomial);
        long endingTime = System.currentTimeMillis();
        System.out.println("The result of the Karatsuba sequential multiplication of polynomials: ");
        System.out.println("Running time time : " + (endingTime - startingTime) + " ms");
        return result;
    }

    static Polynomial multiplicationKaratsubaParallel(Polynomial firstPolynomial, Polynomial secondPolynomial) throws ExecutionException, InterruptedException {
        long startingTime = System.currentTimeMillis();
        Polynomial result = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(firstPolynomial, secondPolynomial,0);
        long endingTime = System.currentTimeMillis();
        System.out.println("The result of the Karatsuba parallel multiplication of polynomials: ");
        System.out.println("Running time time : " + (endingTime - startingTime) + " ms");
        return result;
    }

}
