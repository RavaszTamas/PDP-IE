package ro.ubb.models;

import ro.ubb.Main;

public class MultiplicationByKth implements Runnable {

    private int[][] a;
    private final int[][] b;
    private final int[][] result;
    private int startRow;
    private int startColumn;
    private int k;

    public MultiplicationByKth(int[][] a, int[][] b, int[][] result,int startRow,int startColumn, int k) {

        this.a = a;
        this.b = b;
        this.result = result;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.k = k;
    }

    @Override
    public void run() {

        int i = startRow, j = startColumn;

        while (i < result.length) {
            while (j < result[i].length) {
                try {
                    Main.computeAtPosition(a, b, result, i, j);
//                    Main.accesCount[i][j]++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                j += k;
            }

            int asd = j;
            j = asd % result[i].length;
            i += asd / result[i].length;
        }

    }
}
