package com.example.accelerometersensor;

public class EuclideanDistance {

    public static double calculateDistance(DataPoint firstPoint, DataPoint secondPoint) {

        double avgSquare_x = Math.pow(firstPoint.getAvg_x() - secondPoint.getAvg_x(), 2);
        double avgSquare_y = Math.pow(firstPoint.getAvg_y() - secondPoint.getAvg_y(), 2);
        double avgSquare_z = Math.pow(firstPoint.getAvg_z() - secondPoint.getAvg_z(), 2);

        double stdDevSquare_x = Math.pow(firstPoint.getStd_dev_x() - secondPoint.getStd_dev_x(), 2);
        double stdDevSquare_y = Math.pow(firstPoint.getStd_dev_y() - secondPoint.getStd_dev_y(), 2);
        double stdDevSquare_z = Math.pow(firstPoint.getStd_dev_z() - secondPoint.getStd_dev_z(), 2);

        double maxSquare_x = Math.pow(firstPoint.getMax_x() - secondPoint.getMax_x(), 2);
        double maxSquare_y = Math.pow(firstPoint.getMax_y() - secondPoint.getMax_y(), 2);
        double maxSquare_z = Math.pow(firstPoint.getMax_z() - secondPoint.getMax_z(), 2);

        double minSquare_x = Math.pow(firstPoint.getMin_x() - secondPoint.getMin_x(), 2);
        double minSquare_y = Math.pow(firstPoint.getMin_y() - secondPoint.getMin_y(), 2);
        double minSquare_z = Math.pow(firstPoint.getMin_z() - secondPoint.getMin_z(), 2);

        double avgAbsDifSquare_x = Math.pow(firstPoint.getAvgabsdif_x() - secondPoint.getAvgabsdif_x(), 2);
        double avgAbsDifSquare_y = Math.pow(firstPoint.getAvgabsdif_y() - secondPoint.getAvgabsdif_y(), 2);
        double avgAbsDifSquare_z = Math.pow(firstPoint.getAvgabsdif_z() - secondPoint.getAvgabsdif_z(), 2);

        double avgResAccSquare = Math.pow(firstPoint.getAvgresacc() - secondPoint.getAvgresacc(), 2);

        double distance = Math.sqrt(avgSquare_x + avgSquare_y + avgSquare_z +
                                    stdDevSquare_x + stdDevSquare_y + stdDevSquare_z +
                                    maxSquare_x + maxSquare_y + maxSquare_z +
                                    minSquare_x + minSquare_y + minSquare_z +
                                    avgAbsDifSquare_x + avgAbsDifSquare_y + avgAbsDifSquare_z +
                                    avgResAccSquare);
        return distance;
    }
}
