package ro.ubb;

import java.util.ArrayList;
import java.util.Random;

public class Utils {
    private static final int LOWER_LIMIT = 100;
    private static final int UPPER_LIMIT = 900;

    public static void initalizeMatrices(int[][] matrix) {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = random.nextInt(random.nextInt((UPPER_LIMIT - LOWER_LIMIT) + 1) + LOWER_LIMIT);
    }
    public static ArrayList<Integer> split(int x, int n) throws Exception {

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

}
