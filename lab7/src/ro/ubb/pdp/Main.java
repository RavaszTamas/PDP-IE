package ro.ubb.pdp;

import mpi.MPI;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    public static final int ARRAY_TAG = 1;
    public static final int ARRAY_SIZE_TAG = 2;
    public static final int LIMIT_TAG = 3;
    public static final int ID_TAG = 4;
    public static final int DEST_SIZE_TAG = 5;


    public static void main(String[] args) {
        // write your code here
        MPI.Init(args);
        int size = MPI.COMM_WORLD.Size();
        int me = MPI.COMM_WORLD.Rank();


        if (me == 0) {
            //master process
            int lengthOfTheFirstPolynomial = 1000;
            int lengthOfTheSecondPolynomial = 1000;
            Polynomial firstPolynomial = new Polynomial(lengthOfTheFirstPolynomial);
            Polynomial secondPolynomial = new Polynomial(lengthOfTheSecondPolynomial);
            System.out.println(firstPolynomial);
            System.out.println(secondPolynomial);

//            if(lengthOfTheFirstPolynomial > lengthOfTheSecondPolynomial)
//            {
//                secondPolynomial.addZeroesToEnd(lengthOfTheFirstPolynomial-lengthOfTheSecondPolynomial);
//            }
//            else if(lengthOfTheSecondPolynomial > lengthOfTheFirstPolynomial)
//            {
//                firstPolynomial.addZeroesToEnd(lengthOfTheSecondPolynomial-lengthOfTheFirstPolynomial);
//            }


            System.out.println("Simple: " + MPIStandardMultiplicationMaster(firstPolynomial, secondPolynomial));
            System.out.println("========================================================================================");
            System.out.println("Karatsuba: " + MPIKaratsubaMultiplicationMaster(firstPolynomial, secondPolynomial));

        } else {
            MPIStandardMultiplicationWorker();
            MPIKaratsubaMultiplicationWorker();
        }


        MPI.Finalize();

    }


    private static Polynomial MPIStandardMultiplicationMaster(Polynomial firstPolynomial, Polynomial secondPolynomial) {

        if (MPI.COMM_WORLD.Size() == 1) {
            return ImplementationOfAlgorithms.multiplicationSequentialForm(firstPolynomial, secondPolynomial);
        }

        int numberOfProcesses = MPI.COMM_WORLD.Size();
        int begin = 0;
        int end = 0;
        int length = firstPolynomial.getLength() / numberOfProcesses;

        int[] firstArray = new int[firstPolynomial.getLength()];
        int[] secondArray = new int[secondPolynomial.getLength()];

        for (int i = 0; i < firstPolynomial.getLength(); i++) {
            firstArray[i] = firstPolynomial.getCoefficients().get(i);
        }
        for (int i = 0; i < secondPolynomial.getLength(); i++) {
            secondArray[i] = secondPolynomial.getCoefficients().get(i);
        }

        int[] sizeToSendFirst = {firstArray.length};
        int[] sizeToSendSecond = {secondArray.length};
        for (int i = 1; i < numberOfProcesses; i++) {
            begin = end;
            end = end + length;
            if (i == numberOfProcesses - 1)
                end = firstPolynomial.getCoefficients().size();

            int[] beginArr = {begin};
            int[] endArr = {end};

            MPI.COMM_WORLD.Send(sizeToSendFirst, 0, 1, MPI.INT, i, ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(firstArray, 0, sizeToSendFirst[0], MPI.INT, i, ARRAY_TAG);
            MPI.COMM_WORLD.Send(sizeToSendSecond, 0, 1, MPI.INT, i, ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(secondArray, 0, sizeToSendSecond[0], MPI.INT, i, ARRAY_TAG);
            MPI.COMM_WORLD.Send(beginArr, 0, 1, MPI.INT, i, LIMIT_TAG);
            MPI.COMM_WORLD.Send(endArr, 0, 1, MPI.INT, i, LIMIT_TAG);

        }

        int[][] results = new int[numberOfProcesses - 1][];
        int[] oneResultLength = new int[1];
        for (int i = 1; i < numberOfProcesses; i++) {
            MPI.COMM_WORLD.Recv(oneResultLength, 0, 1, MPI.INT, i, ARRAY_SIZE_TAG);
            results[i - 1] = new int[oneResultLength[0]];
            MPI.COMM_WORLD.Recv(results[i - 1], 0, oneResultLength[0], MPI.INT, i, ARRAY_TAG);
//            System.out.println("result: " + Arrays.toString(results[i - 1]));
        }


        return convertToFinalPolynomial(results);
    }

    public static Polynomial convertToFinalPolynomial(int[][] results) {
        Integer[] data = new Integer[results[0].length];
        Arrays.fill(data, 0);
        List<Integer> list = Arrays.asList(data);
        Polynomial resultPolynomial = new Polynomial(list);

//        System.out.println(results[0].length);
//
//        System.out.println(resultPolynomial);
//
//        System.out.println(resultPolynomial.getLength());

        for (int i = 0; i < resultPolynomial.getLength(); i++) {
            for (int j = 0; j < results.length; j++) {
//                System.out.println(i+ " " +j);
//                System.out.println(results[j][i]);
                resultPolynomial.getCoefficients().set(i, resultPolynomial.getCoefficients().get(i) + results[j][i]);
            }
        }
        return resultPolynomial;
    }

    private static void MPIStandardMultiplicationWorker() {

        int[] firstLength = new int[1];
        int[] secondLength = new int[1];
        int[] beginArr = new int[1];
        int[] endArr = new int[1];
        MPI.COMM_WORLD.Recv(firstLength, 0, 1, MPI.INT, 0, ARRAY_SIZE_TAG);

        int[] firstArray = new int[firstLength[0]];


        MPI.COMM_WORLD.Recv(firstArray, 0, firstLength[0], MPI.INT, 0, ARRAY_TAG);

        MPI.COMM_WORLD.Recv(secondLength, 0, 1, MPI.INT, 0, ARRAY_SIZE_TAG);

        int[] secondArray = new int[secondLength[0]];

        MPI.COMM_WORLD.Recv(secondArray, 0, secondLength[0], MPI.INT, 0, ARRAY_TAG);

        MPI.COMM_WORLD.Recv(beginArr, 0, 1, MPI.INT, 0, LIMIT_TAG);
        MPI.COMM_WORLD.Recv(endArr, 0, 1, MPI.INT, 0, LIMIT_TAG);


//        System.out.println(Arrays.toString(firstArray));
//        System.out.println(Arrays.toString(secondArray));

        Polynomial result = ImplementationOfAlgorithms.multiplyFromBeginToEnd(new Polynomial(firstArray), new Polynomial(secondArray), beginArr[0], endArr[0]);


        int[] resultLength = new int[1];

        resultLength[0] = result.getLength();

        int[] resultArray = new int[resultLength[0]];
        for (int i = 0; i < result.getLength(); i++) {
            resultArray[i] = result.getCoefficients().get(i);
        }

        MPI.COMM_WORLD.Send(resultLength, 0, 1, MPI.INT, 0, ARRAY_SIZE_TAG);
        MPI.COMM_WORLD.Send(resultArray, 0, resultLength[0], MPI.INT, 0, ARRAY_TAG);

    }

    private static Polynomial MPIKaratsubaMultiplicationMaster(Polynomial firstPolynomial, Polynomial secondPolynomial) {

        Polynomial result = null;

        if (MPI.COMM_WORLD.Size() == 1) {
            try {
                result = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(firstPolynomial, secondPolynomial, 0);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            int[] myIdBuffer = {0};
            //sending source
//            System.out.println("Sending source");
            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, 1, ID_TAG);
//            System.out.println("Source sent");
            //converting polynomials
            int[] firstArraySize = {firstPolynomial.getLength()};
            int[] secondArraySize = {secondPolynomial.getLength()};

            int[] firstArray = new int[firstPolynomial.getLength()];
            int[] secondArray = new int[secondPolynomial.getLength()];

            for (int i = 0; i < firstPolynomial.getLength(); i++) {
                firstArray[i] = firstPolynomial.getCoefficients().get(i);
            }
            for (int i = 0; i < secondPolynomial.getLength(); i++) {
                secondArray[i] = secondPolynomial.getCoefficients().get(i);
            }
            //sending arrays
//            System.out.println("Sending arrays");
            MPI.COMM_WORLD.Send(firstArraySize, 0, 1, MPI.INT, 1, ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(firstArray, 0, firstArraySize[0], MPI.INT, 1, ARRAY_TAG);
            MPI.COMM_WORLD.Send(secondArraySize, 0, 1, MPI.INT, 1, ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(secondArray, 0, secondArraySize[0], MPI.INT, 1, ARRAY_TAG);


            if (MPI.COMM_WORLD.Size() == 2) {
                int[] noNewDestinations = {0};
                MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, 1, DEST_SIZE_TAG);
            } else {
                int[] destinations = IntStream.range(2, MPI.COMM_WORLD.Size()).toArray();
                int[] destSize = {destinations.length};
                MPI.COMM_WORLD.Send(destSize, 0, 1, MPI.INT, 1, DEST_SIZE_TAG);
                MPI.COMM_WORLD.Send(destinations, 0, destSize[0], MPI.INT, 1, ARRAY_TAG);
            }

//            System.out.println("Main awaiting result");
            int[] resultSize = {0};

            MPI.COMM_WORLD.Recv(resultSize, 0, 1, MPI.INT, 1, ARRAY_SIZE_TAG);

            int[] resultArray = new int[resultSize[0]];

            MPI.COMM_WORLD.Recv(resultArray, 0, resultSize[0], MPI.INT, 1, ARRAY_TAG);

            result = new Polynomial(resultArray);

        }


        return result;
    }

    private static void MPIKaratsubaMultiplicationWorker() {

        int[] from = new int[1];
        int[] firstLength = new int[1];
        int[] secondLength = new int[1];
        int[] destinationLength = new int[1];
        int[] destinations = null;
//        System.out.println("Awaiting source " + MPI.COMM_WORLD.Rank());
        MPI.COMM_WORLD.Recv(from, 0, 1, MPI.INT, MPI.ANY_SOURCE, ID_TAG);
//        System.out.println("Source received "  + MPI.COMM_WORLD.Rank() + " value: " + from[0]);
        MPI.COMM_WORLD.Recv(firstLength, 0, 1, MPI.INT, MPI.ANY_SOURCE, ARRAY_SIZE_TAG);

        int[] firstArray = new int[firstLength[0]];


        MPI.COMM_WORLD.Recv(firstArray, 0, firstLength[0], MPI.INT, MPI.ANY_SOURCE, ARRAY_TAG);

        MPI.COMM_WORLD.Recv(secondLength, 0, 1, MPI.INT, MPI.ANY_SOURCE, ARRAY_SIZE_TAG);

        int[] secondArray = new int[secondLength[0]];

        MPI.COMM_WORLD.Recv(secondArray, 0, secondLength[0], MPI.INT, MPI.ANY_SOURCE, ARRAY_TAG);

        MPI.COMM_WORLD.Recv(destinationLength, 0, 1, MPI.INT, MPI.ANY_SOURCE, DEST_SIZE_TAG);

        if (destinationLength[0] != 0) {
            destinations = new int[destinationLength[0]];
            MPI.COMM_WORLD.Recv(destinations, 0, destinationLength[0], MPI.INT, MPI.ANY_SOURCE, ARRAY_TAG);
        }

        Polynomial first = new Polynomial(firstArray);
        Polynomial second = new Polynomial(secondArray);

        //case if polynomial too small
        if (first.getLength() == 1 || second.getLength() == 1) {
            Polynomial result = ImplementationOfAlgorithms.multiplicationKaratsubaSequentialForm(first, second);

            int[] resultArraySize = {result.getLength()};
            int[] resultArray = new int[result.getLength()];

            for (int i = 0; i < result.getLength(); i++) {
                resultArray[i] = result.getCoefficients().get(i);
            }
            MPI.COMM_WORLD.Send(resultArraySize, 0, 1, MPI.INT, from[0], ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(resultArray, 0, resultArraySize[0], MPI.INT, from[0], ARRAY_TAG);

            return;
        }

//        if(MPI.COMM_WORLD.Rank() == 4) {
//            System.out.println(Arrays.toString(destinations));
//        }



        int len = Math.max(first.getDegree(), second.getDegree()) / 2;
        Polynomial lowFirst = new Polynomial(first.getCoefficients().subList(0, len));
        Polynomial highFirst = new Polynomial(first.getCoefficients().subList(len, first.getLength()));
        Polynomial lowSecond = new Polynomial(second.getCoefficients().subList(0, len));
        Polynomial highSecond = new Polynomial(second.getCoefficients().subList(len, second.getLength()));

        Polynomial productLowz1=null, productHighz3 = null, productLowHighz2=null;

        if (destinationLength[0] == 0) {
            try {
                productLowz1 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(lowFirst, lowSecond, 0);
                productLowHighz2 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst), ImplementationOfAlgorithms.addPolynomials(lowSecond, highSecond), 0);
                productHighz3 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(highFirst, highSecond, 0);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


        }
        else if(destinationLength[0] == 1 && destinations != null){



            int[] myIdBuffer = {MPI.COMM_WORLD.Rank()};
            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[0], ID_TAG);
            int[] lowFirstSize = {lowFirst.getLength()};
            int[] lowSecondSize = {lowSecond.getLength()};

            int[] lowFirstArray = new int[lowFirst.getLength()];
            int[] lowSecondArray = new int[lowSecond.getLength()];

            for (int i = 0; i < lowFirst.getLength(); i++) {
                lowFirstArray[i] = lowFirst.getCoefficients().get(i);
            }
            for (int i = 0; i < lowSecond.getLength(); i++) {
                lowSecondArray[i] = lowSecond.getCoefficients().get(i);
            }

            MPI.COMM_WORLD.Send(lowFirstSize, 0, 1, MPI.INT, destinations[0], ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(lowFirstArray, 0, lowFirstSize[0], MPI.INT, destinations[0], ARRAY_TAG);
            MPI.COMM_WORLD.Send(lowFirstSize, 0, 1, MPI.INT, destinations[0], ARRAY_SIZE_TAG);
            MPI.COMM_WORLD.Send(lowSecondArray, 0, lowSecondSize[0], MPI.INT, destinations[0], ARRAY_TAG);
            int[] noNewDestinations = {0};
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[0], DEST_SIZE_TAG);

            try {
                productLowHighz2 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst), ImplementationOfAlgorithms.addPolynomials(lowSecond, highSecond), 0);
                productHighz3 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(highFirst, highSecond, 0);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


            int[] lowFirstProductSize = {0};

            MPI.COMM_WORLD.Recv(lowFirstProductSize, 0, 1, MPI.INT, destinations[0], ARRAY_SIZE_TAG);

            int[] lowFirstProduct = new int[lowFirstProductSize[0]];

            MPI.COMM_WORLD.Recv(lowFirstProduct, 0, lowFirstProductSize[0], MPI.INT, destinations[0], ARRAY_TAG);


            productLowz1 = new Polynomial(lowFirstProduct);


        }
        else if(destinationLength[0] == 2 && destinations != null) {


            int[] myIdBuffer = {MPI.COMM_WORLD.Rank()};
            int[] noNewDestinations = {0};

            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[0], ID_TAG);
            sendPolynomialToGivenDestination(lowFirst,destinations[0]);
            sendPolynomialToGivenDestination(lowSecond,destinations[0]);
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[0], DEST_SIZE_TAG);


            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[1], ID_TAG);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst),destinations[1]);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowSecond, highSecond),destinations[1]);
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[1], DEST_SIZE_TAG);

            try {
                productHighz3 = ImplementationOfAlgorithms.multiplicationKaratsubaParallelizedForm(highFirst, highSecond, 0);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            productLowz1 = readPolynomialFrom(destinations[0]);
            productLowHighz2 = readPolynomialFrom(destinations[1]);


        }
        else if(destinationLength[0] == 3 && destinations != null) {
            int[] myIdBuffer = {MPI.COMM_WORLD.Rank()};
            int[] noNewDestinations = {0};

            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[0], ID_TAG);
            sendPolynomialToGivenDestination(lowFirst,destinations[0]);
            sendPolynomialToGivenDestination(lowSecond,destinations[0]);
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[0], DEST_SIZE_TAG);


            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[1], ID_TAG);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst),destinations[1]);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowSecond, highSecond),destinations[1]);
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[1], DEST_SIZE_TAG);

            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[2], ID_TAG);
            sendPolynomialToGivenDestination(highFirst,destinations[2]);
            sendPolynomialToGivenDestination(highSecond,destinations[2]);
            MPI.COMM_WORLD.Send(noNewDestinations, 0, 1, MPI.INT, destinations[2], DEST_SIZE_TAG);

            productLowz1 = readPolynomialFrom(destinations[0]);
            productLowHighz2 = readPolynomialFrom(destinations[1]);
            productHighz3 = readPolynomialFrom(destinations[2]);


        }
        else if (destinations != null){
            int[] myIdBuffer = {MPI.COMM_WORLD.Rank()};
            int[] lengthOfDestination = {-1};
            int[] destinationsToSend;
            int[] noNewDestinationsCurrently = {0};


//            if(MPI.COMM_WORLD.Rank() == 1) {
//                System.out.println(Arrays.toString(destinations));
//            }

            IntStream auxilidaryDestinations = Arrays.stream(destinations);

            auxilidaryDestinations = auxilidaryDestinations.skip(3);
            long auxLength = (destinations.length-3)/3;


            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[0], ID_TAG);
            sendPolynomialToGivenDestination(lowFirst,destinations[0]);
            sendPolynomialToGivenDestination(lowSecond,destinations[0]);

            destinationsToSend = auxilidaryDestinations.limit(auxLength).toArray();

            auxilidaryDestinations = Arrays.stream(destinations).skip(3);

//            if(MPI.COMM_WORLD.Rank() == 1) {
//                System.out.println(Arrays.toString(destinationsToSend));
//            }

            if(destinationsToSend.length == 0) {
                MPI.COMM_WORLD.Send(noNewDestinationsCurrently, 0, 1, MPI.INT, destinations[0], DEST_SIZE_TAG);
            }
            else{
                lengthOfDestination[0] = destinationsToSend.length;
                MPI.COMM_WORLD.Send(lengthOfDestination, 0, 1, MPI.INT, destinations[0], DEST_SIZE_TAG);
                MPI.COMM_WORLD.Send(destinationsToSend, 0, lengthOfDestination[0], MPI.INT, destinations[0], ARRAY_TAG);
            }

            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[1], ID_TAG);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowFirst, highFirst),destinations[1]);
            sendPolynomialToGivenDestination(ImplementationOfAlgorithms.addPolynomials(lowSecond, highSecond),destinations[1]);


            destinationsToSend = auxilidaryDestinations.skip(auxLength).limit(auxLength).toArray();

            auxilidaryDestinations = Arrays.stream(destinations).skip(3);

//            if(MPI.COMM_WORLD.Rank() == 1) {
//                System.out.println(Arrays.toString(destinationsToSend));
//            }

            if(destinationsToSend.length == 0) {
                MPI.COMM_WORLD.Send(noNewDestinationsCurrently, 0, 1, MPI.INT, destinations[1], DEST_SIZE_TAG);
            }
            else{
                lengthOfDestination[0] = destinationsToSend.length;
                MPI.COMM_WORLD.Send(lengthOfDestination, 0, 1, MPI.INT, destinations[1], DEST_SIZE_TAG);
                MPI.COMM_WORLD.Send(destinationsToSend, 0, lengthOfDestination[0], MPI.INT, destinations[1], ARRAY_TAG);
            }

            MPI.COMM_WORLD.Send(myIdBuffer, 0, 1, MPI.INT, destinations[2], ID_TAG);
            sendPolynomialToGivenDestination(highFirst,destinations[2]);
            sendPolynomialToGivenDestination(highSecond,destinations[2]);


            destinationsToSend = auxilidaryDestinations.skip(2*auxLength).toArray();

//            if(MPI.COMM_WORLD.Rank() == 1) {
//                System.out.println(Arrays.toString(destinationsToSend));
//            }

            if(destinationsToSend.length == 0) {
                MPI.COMM_WORLD.Send(noNewDestinationsCurrently, 0, 1, MPI.INT, destinations[2], DEST_SIZE_TAG);
            }
            else{
                lengthOfDestination[0] = destinationsToSend.length;
                MPI.COMM_WORLD.Send(lengthOfDestination, 0, 1, MPI.INT, destinations[2], DEST_SIZE_TAG);
                MPI.COMM_WORLD.Send(destinationsToSend, 0, lengthOfDestination[0], MPI.INT, destinations[2], ARRAY_TAG);
            }

            productLowz1 = readPolynomialFrom(destinations[0]);
            productLowHighz2 = readPolynomialFrom(destinations[1]);
            productHighz3 = readPolynomialFrom(destinations[2]);

        }
        Polynomial r1 = ImplementationOfAlgorithms.shiftPolynomial(productHighz3, 2 * len);
        Polynomial r2 = ImplementationOfAlgorithms.shiftPolynomial(ImplementationOfAlgorithms.subtractPolynomial(ImplementationOfAlgorithms.subtractPolynomial(productLowHighz2, productHighz3), productLowz1), len);
        Polynomial  finalPolynomial = ImplementationOfAlgorithms.addPolynomials(ImplementationOfAlgorithms.addPolynomials(r1, r2), productLowz1);


        int[] resultArraySize = {finalPolynomial.getLength()};
        int[] resultArray = new int[finalPolynomial.getLength()];

        for (int i = 0; i < finalPolynomial.getLength(); i++) {
            resultArray[i] = finalPolynomial.getCoefficients().get(i);
        }


        MPI.COMM_WORLD.Send(resultArraySize, 0, 1, MPI.INT, from[0], ARRAY_SIZE_TAG);
        MPI.COMM_WORLD.Send(resultArray, 0, resultArraySize[0], MPI.INT, from[0], ARRAY_TAG);



    }


    public static void sendPolynomialToGivenDestination(Polynomial polynomialToSend, int destination)
    {
        int[] sizeOfPolynomial = {polynomialToSend.getLength()};
        int[] arrayOfCoeffiecients = new int[polynomialToSend.getLength()];
        for (int i = 0; i < polynomialToSend.getLength(); i++) {
            arrayOfCoeffiecients[i] = polynomialToSend.getCoefficients().get(i);
        }
        MPI.COMM_WORLD.Send(sizeOfPolynomial, 0, 1, MPI.INT, destination, ARRAY_SIZE_TAG);
        MPI.COMM_WORLD.Send(arrayOfCoeffiecients, 0, sizeOfPolynomial[0], MPI.INT, destination, ARRAY_TAG);

    }

    public static Polynomial readPolynomialFrom(int source)
    {
        int[] resultSize = {0};

        MPI.COMM_WORLD.Recv(resultSize, 0, 1, MPI.INT, source, ARRAY_SIZE_TAG);

        int[] result = new int[resultSize[0]];

        MPI.COMM_WORLD.Recv(result, 0, resultSize[0], MPI.INT, source, ARRAY_TAG);

        return new Polynomial(result);
    }


}
