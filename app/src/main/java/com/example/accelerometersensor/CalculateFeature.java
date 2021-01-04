package com.example.accelerometersensor;

import java.util.ArrayList;
import java.util.List;

public class CalculateFeature {
    public static double countAverage(List<Double> listData) {
        double sum = 0.0;
        for (Double i : listData) {
            sum += i;
        }
        return sum / listData.size();
    }

    public static double countStdDev(List<Double> listData) {
        double stdDev = 0.0, average = countAverage(listData);
        for(double num: listData) {
            stdDev += Math.pow(num - average, 2);
        }
        return Math.sqrt(stdDev / listData.size());
    }

    public static double countMax(List<Double> listData) {
        double max = Double.MIN_VALUE;
        for(double num: listData) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    public static double countMin(List<Double> listData) {
        double min = Double.MAX_VALUE;
        for(double num: listData) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }

    public static double countAvgAbsDiff(List<Double> listData) {
        double average = countAverage(listData);
        for (int i = 0; i < listData.size(); i++) {
            listData.set(i, Math.abs(listData.get(i) - average));
        }
        return countAverage(listData);
    }

    public static double countAvgResAcc(List<Double> listDataX, List<Double> listDataY, List<Double> listDataZ) {
        List<Double> squareX = new ArrayList<>();
        List<Double> squareY = new ArrayList<>();
        List<Double> squareZ = new ArrayList<>();
        List<Double> resAcc = new ArrayList<>();

        for (double x : listDataX) {
            squareX.add(Math.pow(x, 2));
        }
        for (double y : listDataY) {
            squareY.add(Math.pow(y, 2));
        }
        for (double z : listDataZ) {
            squareZ.add(Math.pow(z, 2));
        }

        for (int i = 0; i < listDataX.size(); i++) {
            resAcc.add(Math.sqrt(squareX.get(i) + squareY.get(i) + squareZ.get(i)));
        }

        return countAverage(resAcc);
    }
}
