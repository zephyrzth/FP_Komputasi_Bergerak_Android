package com.example.accelerometersensor;

public class DataPoint {

    public static final int AVG_INDEX_X = 0;
    public static final int AVG_INDEX_Y = 1;
    public static final int AVG_INDEX_Z = 2;

    public static final int STDDEV_INDEX_X = 3;
    public static final int STDDEV_INDEX_Y = 4;
    public static final int STDDEV_INDEX_Z = 5;

    public static final int MAX_INDEX_X = 6;
    public static final int MAX_INDEX_Y = 7;
    public static final int MAX_INDEX_Z = 8;

    public static final int MIN_INDEX_X = 9;
    public static final int MIN_INDEX_Y = 10;
    public static final int MIN_INDEX_Z = 11;

    public static final int AVGABSDIF_INDEX_X = 12;
    public static final int AVGABSDIF_INDEX_Y = 13;
    public static final int AVGABSDIF_INDEX_Z = 14;

    public static final int AVGRESACC_INDEX_X = 15;
    public static final int AVGRESACC_INDEX_Y = 16;
    public static final int AVGRESACC_INDEX_Z = 17;

    public static final int CATEGORY_INDEX = 18;

    private double avg_x;
    private double avg_y;
    private double avg_z;

    private double std_dev_x;
    private double std_dev_y;
    private double std_dev_z;

    private double max_x;
    private double max_y;
    private double max_z;

    private double min_x;
    private double min_y;
    private double min_z;

    private double avgabsdif_x;
    private double avgabsdif_y;
    private double avgabsdif_z;

    private double avgresacc_x;
    private double avgresacc_y;
    private double avgresacc_z;

    private Category category;

    public DataPoint() { }

    public void setAvg_x(double avg_x) {
        this.avg_x = avg_x;
    }

    public void setAvg_y(double avg_y) {
        this.avg_y = avg_y;
    }

    public void setAvg_z(double avg_z) {
        this.avg_z = avg_z;
    }

    public void setStd_dev_x(double std_dev_x) {
        this.std_dev_x = std_dev_x;
    }

    public void setStd_dev_y(double std_dev_y) {
        this.std_dev_y = std_dev_y;
    }

    public void setStd_dev_z(double std_dev_z) {
        this.std_dev_z = std_dev_z;
    }

    public void setMax_x(double max_x) {
        this.max_x = max_x;
    }

    public void setMax_y(double max_y) {
        this.max_y = max_y;
    }

    public void setMax_z(double max_z) {
        this.max_z = max_z;
    }

    public void setMin_x(double min_x) {
        this.min_x = min_x;
    }

    public void setMin_y(double min_y) {
        this.min_y = min_y;
    }

    public void setMin_z(double min_z) {
        this.min_z = min_z;
    }

    public void setAvgabsdif_x(double avgabsdif_x) {
        this.avgabsdif_x = avgabsdif_x;
    }

    public void setAvgabsdif_y(double avgabsdif_y) {
        this.avgabsdif_y = avgabsdif_y;
    }

    public void setAvgabsdif_z(double avgabsdif_z) {
        this.avgabsdif_z = avgabsdif_z;
    }

    public void setAvgresacc_x(double avgresacc_x) {
        this.avgresacc_x = avgresacc_x;
    }

    public void setAvgresacc_y(double avgresacc_y) {
        this.avgresacc_y = avgresacc_y;
    }

    public void setAvgresacc_z(double avgresacc_z) {
        this.avgresacc_z = avgresacc_z;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getAvg_x() {
        return avg_x;
    }

    public double getAvg_y() {
        return avg_y;
    }

    public double getAvg_z() {
        return avg_z;
    }

    public double getStd_dev_x() {
        return std_dev_x;
    }

    public double getStd_dev_y() {
        return std_dev_y;
    }

    public double getStd_dev_z() {
        return std_dev_z;
    }

    public double getMax_x() {
        return max_x;
    }

    public double getMax_y() {
        return max_y;
    }

    public double getMax_z() {
        return max_z;
    }

    public double getMin_x() {
        return min_x;
    }

    public double getMin_y() {
        return min_y;
    }

    public double getMin_z() {
        return min_z;
    }

    public double getAvgabsdif_x() {
        return avgabsdif_x;
    }

    public double getAvgabsdif_y() {
        return avgabsdif_y;
    }

    public double getAvgabsdif_z() {
        return avgabsdif_z;
    }

    public double getAvgresacc_x() {
        return avgresacc_x;
    }

    public double getAvgresacc_y() {
        return avgresacc_y;
    }

    public double getAvgresacc_z() {
        return avgresacc_z;
    }

    public Category getCategory() {
        return category;
    }
}
