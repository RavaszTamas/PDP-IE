package ro.ubb;

import ro.ubb.models.MultiplicationByKth;
import ro.ubb.models.TaskSimple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolutionKth {

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
        try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter("testResultEveryKthSimple.txt", true)))) {

            for (int i = 0; i < tasks.size(); i++) {
                start = System.nanoTime() / 1000000;
                performTest(tasks.get(i));
                end = System.nanoTime() / 1000000;
                String resultString = "End work multiplication every kth element: " + (end - start) / 1000 + " seconds, " + tasks.get(i).getThreadCount() + " threads/tasks and matrix of size " + tasks.get(i).getRowSize() + "x" + tasks.get(i).getColumnSize();
                System.out.println(resultString);
                output.println(resultString);
                output.flush();
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

        int si = 0, sj = 0;
        for (int i = 0; i < taskSimple.getThreadCount() && si < taskSimple.getRowSize(); i++) {
            threads.add(new Thread(new MultiplicationByKth(a, b, result, si, sj, taskSimple.getThreadCount())));

            sj++;
            if(sj>= taskSimple.getCommonSize()){
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
}
