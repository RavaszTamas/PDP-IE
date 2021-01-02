package ro.ubb.models;

import ro.ubb.Main;

public class MultiplicationRows implements Runnable {

    private int[][] a;
    private final int[][] b;
    private final int[][] result;
    private final int startRow;
    private final int startColumn;
    private final int numberOfItems;

    public MultiplicationRows(int[][] a, int[][] b, int[][] result, int startRow, int startColumn, int numberOfItems) {

        this.a = a;
        this.b = b;
        this.result = result;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.numberOfItems = numberOfItems;
    }

    @Override
    public void run() {
        int count = 0;
        int i = startRow, j = startColumn;
        while (count < numberOfItems && i < result.length) {
            while (count < numberOfItems && j < result[i].length) {
//                System.out.println(i + " " + j);
                try {
                    Main.computeAtPosition(a, b, result, i, j);
//                    Main.accesCount[i][j]++;

                } catch (Exception e) {
                    System.out.println("Bad handling");
                }
                j++;
                count++;
            }
            j = 0;
            i++;
        }


//        for(int i = startRow; i < result.length && count < numberOfItems; i++){
//            for(int j = startColumn; j < result[i].length && count < numberOfItems; j++, count++)
//            {
//            }
//        }
//        System.out.println("===="+count);

    }
}
