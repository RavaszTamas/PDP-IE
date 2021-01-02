package ro.ubb;

import ro.ubb.models.MultiplicationByKth;
import ro.ubb.models.MultiplicationColumns;
import ro.ubb.models.MultiplicationRows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

    private static final int LOWER_LIMIT = 100;
    private static final int UPPER_LIMIT = 900;
    private static final int NUMBER_OF_THREADS = 1000;
    private static final int RESULT_ROW = 1000;
    private static final int RESULT_COLUMN = 2000;
    private static final int COMMON_SIZE = 5000;

//    public static short[][] accesCount = new short[RESULT_ROW][RESULT_COLUMN];

    private static ArrayList<Integer> split(int x, int n) throws Exception {

        ArrayList<Integer> quantites = new ArrayList<>();
        if (x < n)
            throw new Exception("Can't split the array correctly");

        else if (x % n == 0) {
            for (int i = 0; i < n; i++)
                quantites.add((x / n));
        } else {

            int zp = n - (x % n);
            int pp = x / n;
            for (int i = 0; i < n; i++) {

                if (i >= zp)
                    quantites.add(pp + 1);
                else
                    quantites.add(pp);
            }
        }
        return quantites;
    }

    public static void computeAtPosition(int[][] a, int[][] b, int[][] result, int i, int j) throws Exception {
        if (a[0].length != b.length)
            throw new Exception("Number of columns in a must be equal to number of columns in b");
        if (a.length != result.length && b[0].length != result[0].length)
            throw new Exception("Result matrix in incorrectly formated");
        if (i >= result.length || j >= result[0].length || j < 0 || i < 0)
            throw new Exception("Operation out of bounds");

        for (int k = 0; k < b.length; ++k) {
            result[i][j] += a[i][k] * b[k][j];
        }

    }

    public static void initalizeMatrices(int[][] matrix) {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = random.nextInt(random.nextInt((UPPER_LIMIT - LOWER_LIMIT) + 1) + LOWER_LIMIT);
    }

    public static void main(String[] args) {
        float start,end;
        start =  System.nanoTime() / 1000000;
        solutionRowConsectuive();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication row: " + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        solutionColumnConsecutive();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication column:" + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        solutionKth();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication kth element:" + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        solutionRowConsectuiveExecutor();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication row with thread pool: " + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        solutionColumnConsecutiveExecutor();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication column with thread pool:" + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        solutionKthExecutor();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication kth element with thread pool:" + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");
        start =  System.nanoTime() / 1000000;
        linearSolution();
        end = System.nanoTime() / 1000000;
        System.out.println("End work multiplication with no parallelism:" + (end - start) / 1000 + " seconds, " + NUMBER_OF_THREADS + " threads");

    }

    private static void linearSolution() {
        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        initalizeMatrices(a);
        initalizeMatrices(b);

        for(int i = 0; i < RESULT_ROW; i++)
            for (int j = 0; j < RESULT_COLUMN;j++) {
                try {
                    computeAtPosition(a, b, result, i, j);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }
    private static void solutionKthExecutor() {
        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        initalizeMatrices(a);
        initalizeMatrices(b);

        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            pool.execute(new MultiplicationByKth(a, b, result, si, sj, NUMBER_OF_THREADS));

            sj++;
            if(sj>= RESULT_COLUMN){
                sj = 0;
                si++;
            }

        }

        pool.shutdown();

//        for(int i = 0; i < result.length; i++)
//            System.out.println(Arrays.toString(accesCount[i]));
    }

    private static void solutionColumnConsecutiveExecutor() {

        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        initalizeMatrices(a);
        initalizeMatrices(b);

        int numberOfTotalPositions = RESULT_ROW * RESULT_COLUMN;


        List<Integer> quantity = null;
        try {
            quantity = split(numberOfTotalPositions, NUMBER_OF_THREADS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            int val = quantity.get(i);
            pool.execute(new MultiplicationColumns(a, b, result, si, sj, val));

            int asd = si + val;
            si = asd % RESULT_ROW;
            sj += asd / RESULT_ROW;

        }

        pool.shutdown();

//        for(int i = 0; i < result.length; i++)
//            System.out.println(Arrays.toString(accesCount[i]));
    }

    private static void solutionRowConsectuiveExecutor() {

        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        initalizeMatrices(a);
        initalizeMatrices(b);

        int numberOfTotalPositions = RESULT_ROW * RESULT_COLUMN;


        List<Integer> quantity = null;
        try {
            quantity = split(numberOfTotalPositions, NUMBER_OF_THREADS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            int val = quantity.get(i);
            pool.execute(new MultiplicationRows(a, b, result, si, sj, val));

            int asd = sj + val;
            sj = asd % RESULT_COLUMN;
            si += asd / RESULT_COLUMN;

        }

        pool.shutdown();

    }

    private static void solutionKth() {
        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        ArrayList<Thread> threads = new ArrayList<>();

        initalizeMatrices(a);
        initalizeMatrices(b);

        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS && si < RESULT_ROW; i++) {
            threads.add(new Thread(new MultiplicationByKth(a, b, result, si, sj, NUMBER_OF_THREADS)));

            sj++;
            if(sj>= RESULT_COLUMN){
                sj = 0;
                si++;
            }

        }

        for (Thread thread : threads) {
            thread.start();
        }
        for(Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private static void solutionColumnConsecutive() {

        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        ArrayList<Thread> threads = new ArrayList<>();

        initalizeMatrices(a);
        initalizeMatrices(b);

        int numberOfTotalPositions = RESULT_ROW * RESULT_COLUMN;


        List<Integer> quantity = null;
        try {
            quantity = split(numberOfTotalPositions, NUMBER_OF_THREADS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            int val = quantity.get(i);
            threads.add(new Thread(new MultiplicationColumns(a, b, result, si, sj, val)));

            int asd = si + val;
            si = asd % RESULT_ROW;
            sj += asd / RESULT_ROW;

        }

        for (Thread thread : threads) {
            thread.start();
        }
        for(Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

//        for(int i = 0; i < result.length; i++)
//            System.out.println(Arrays.toString(accesCount[i]));
    }

    private static void solutionRowConsectuive() {

        int[][] a = new int[RESULT_ROW][COMMON_SIZE];
        int[][] b = new int[COMMON_SIZE][RESULT_COLUMN];
        int[][] result = new int[RESULT_ROW][RESULT_COLUMN];

        ArrayList<Thread> threads = new ArrayList<>();

        initalizeMatrices(a);
        initalizeMatrices(b);

        int numberOfTotalPositions = RESULT_ROW * RESULT_COLUMN;


        List<Integer> quantity = null;
        try {
            quantity = split(numberOfTotalPositions, NUMBER_OF_THREADS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        int si = 0, sj = 0;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            int val = quantity.get(i);
            threads.add(new Thread(new MultiplicationRows(a, b, result, si, sj, val)));

            int asd = sj + val;
            sj = asd % RESULT_COLUMN;
            si += asd / RESULT_COLUMN;

        }

        for (Thread thread : threads) {
            thread.start();
        }
        for(Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }


    }
}
