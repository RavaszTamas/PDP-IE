package ro.ubb;

import ro.ubb.models.MultiplicationRows;
import ro.ubb.models.TaskSimple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolutionRow {


    public static void main(String[] args) {
        List<Integer> threadNumbers = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8196, 10000));
        List<Integer> matrixSizes = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50, 150, 350, 500, 750, 1250));


        List<TaskSimple> tasks = new ArrayList<>();

        for (Integer threadNum : threadNumbers) {
            for (Integer matrixSize : matrixSizes) {
                tasks.add(new TaskSimple(threadNum, matrixSize, matrixSize * 2, matrixSize * 3));
            }
        }
        float start, end;
        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter("testResultRowSimple.txt", true)))) {

            for (int i = 0; i < tasks.size(); i++) {
                start = System.nanoTime() / 1000000;
                performTest(tasks.get(i));
                end = System.nanoTime() / 1000000;
                String resultString = "End work multiplication row: " + (end - start) / 1000 + " seconds, " + tasks.get(i).getThreadCount() + " threads/tasks and matrix of size " + tasks.get(i).getRowSize() + "x" + tasks.get(i).getColumnSize();
                System.out.println(resultString);
                output.println(resultString);
            }

        } catch (IOException exception) {
            System.out.println("error writing result");
        }
    }


    private static void performTest(TaskSimple taskSimple) {
        int[][] a = new int[taskSimple.getRowSize()][taskSimple.getCommonSize()];
        int[][] b = new int[taskSimple.getCommonSize()][taskSimple.getColumnSize()];
        int[][] result = new int[taskSimple.getRowSize()][taskSimple.getColumnSize()];

        ArrayList<Thread> threads = new ArrayList<>();

        Utils.initalizeMatrices(a);
        Utils.initalizeMatrices(b);

        int numberOfTotalPositions = taskSimple.getRowSize() * taskSimple.getCommonSize();


        List<Integer> quantity = null;
        try {
            quantity = Utils.split(numberOfTotalPositions, taskSimple.getThreadCount());
        } catch (Exception e) {
            quantity = IntStream.range(1,numberOfTotalPositions+1).map(operand -> 1).boxed().collect(Collectors.toList());
        }

        int si = 0, sj = 0;
        for (int i = 0; i < taskSimple.getThreadCount() && i < numberOfTotalPositions; i++) {
            int val = quantity.get(i);
            threads.add(new Thread(new MultiplicationRows(a, b, result, si, sj, val)));

            int asd = sj + val;
            sj = asd % taskSimple.getColumnSize();
            si += asd / taskSimple.getColumnSize();

        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
