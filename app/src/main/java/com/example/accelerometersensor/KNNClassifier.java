package com.example.accelerometersensor;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KNNClassifier {

    public static final String TRAIN_FILE_NAME = "ready_train_data_50.csv";
    public static final String TEST_FILE_NAME = "ready_test_data_50.csv";

    private final int K;
    private float accuracy;
    private EuclideanDistance euclideanDistance;
    private List<DataPoint> listTrainData, listTestData, listTestValidator;

    public KNNClassifier(int K) {
        this.K = K;
        euclideanDistance = new EuclideanDistance();
        listTrainData = new ArrayList<>();
        listTestData = new ArrayList<>();
        listTestValidator = new ArrayList<>();
    }

    public int getK() {
        return K;
    }

    public void setListTrainData(List<DataPoint> listTrainData) {
        this.listTrainData = listTrainData;
    }

    public void setListTestData(List<DataPoint> listTestData) {
        this.listTestData = listTestData;
    }

    public void setListTestValidator(List<DataPoint> listTestValidator) {
        this.listTestValidator = listTestValidator;
    }

    public List<DataPoint> getListTrainData() {
        return listTrainData;
    }

    public List<DataPoint> getListTestData() {
        return listTestData;
    }

    public List<DataPoint> getListTestValidator() {
        return listTestValidator;
    }

    private List<Double> calculateDistances(DataPoint newPoint){
        List<Double> listDistance = new ArrayList<>();
        for (DataPoint trainData : listTrainData){
            double distance = euclideanDistance.calculateDistance(newPoint, trainData);
            listDistance.add(distance);
        }
        return listDistance;
    }

    private Category getMaxCategory(HashMap<Category, Integer> hashMap){
        Iterator<Map.Entry<Category, Integer>> iterator = hashMap.entrySet().iterator();
        int maxCategory = Integer.MIN_VALUE;
        Category category = null;
        while (iterator.hasNext()) {
            Map.Entry<Category, Integer> item = iterator.next();
            if (item.getValue() > maxCategory){
                category = item.getKey();
            }
        }
        return category;
    }

    private Category classifyDataPoint(DataPoint point){
        HashMap<Category, Integer> hashMap = new HashMap<>();
        List<Double> listDistance = calculateDistances(point);
        for (int i = 0; i < K; i++){
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < listDistance.size(); j++){
                if (listDistance.get(j) < min){
                    min = listDistance.get(j);
                    minIndex = j;
                }
            }
            Category category = listTrainData.get(minIndex).getCategory();
            if (hashMap.containsKey(category)){
                hashMap.put(category, hashMap.get(category) + 1);
            } else {
                hashMap.put(category, 1);
            }
            listDistance.set(minIndex, Double.MAX_VALUE);
        }
        return getMaxCategory(hashMap);
    }

    public float classify(){
        accuracy = 0;
        for (int i = 0;i < listTestData.size(); i++){
            DataPoint dataPoint = listTestData.get(i);
            Category category = classifyDataPoint(dataPoint);
            if (isCorrect(category, listTestValidator.get(i).getCategory()))
                accuracy++;
            dataPoint.setCategory(category);
        }
        accuracy /= listTestData.size();

        return accuracy;
    }

    private boolean isCorrect(Category predictedCategory, Category trueCategory){
        return predictedCategory.equals(trueCategory);
    }

    public void reset() {
        listTrainData.clear();
        listTestData.clear();
        listTestValidator.clear();
    }
}
