package com.example.accelerometersensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "DEBUG_TAG";
    private static final int SENSOR_DELAY = 100000;
    private static final String CSV_NAME = "accelerometer_data";
    private static final int K = 5;
    private String csvFileName;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private TextView xValue, yValue, zValue, timeRecord, accuracy, resultCategory;

    private int seconds = 0;
    private Boolean timeRunning = false;
    private Boolean isRecordAlreadyStarted = false;

    private ArrayList<String[]> dataList, testList;
    private List<DataPoint> listTrainData;
    private CSVWriter csvWriter;

    Button btLocation;
    TextView textView1, textView2, textView3;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        dataList = new ArrayList<>();
        testList = new ArrayList<>();

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        accuracy = (TextView) findViewById(R.id.accuracy);
        timeRecord = (TextView) findViewById(R.id.timeRecord);
        resultCategory = (TextView) findViewById(R.id.resultCategory);
        findViewById(R.id.btnStart).setOnClickListener(operasi);
        findViewById(R.id.btnStop).setOnClickListener(operasi);
        findViewById(R.id.btnTest).setOnClickListener(operasi);
        findViewById(R.id.bt_location).setOnClickListener(operasi);

        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        runTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: X: " + event.values[0] + "Y: " + event.values[1] + "Z: " + event.values[2]);

        xValue.setText("xValue: " + event.values[0]);
        yValue.setText("yValue: " + event.values[1]);
        zValue.setText("zValue: " + event.values[2]);

        dataList.add(new String[]{String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])});
        testList.add(new String[]{String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])});
        if (seconds % 5 == 0 && seconds != 0) {
            runPredict();
            testList.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private DataPoint extractFeatures(ArrayList<String[]> listData) {
        List<Double> listDataX = new ArrayList<>();
        List<Double> listDataY = new ArrayList<>();
        List<Double> listDataZ = new ArrayList<>();

        for (int i = 0; i < listData.size(); i++) {
            listDataX.add(Double.parseDouble(listData.get(i)[0]));
            listDataY.add(Double.parseDouble(listData.get(i)[1]));
            listDataZ.add(Double.parseDouble(listData.get(i)[2]));
        }

        double averageX = CalculateFeature.countAverage(listDataX);
        double averageY = CalculateFeature.countAverage(listDataY);
        double averageZ = CalculateFeature.countAverage(listDataZ);

        double stdDevX = CalculateFeature.countStdDev(listDataX);
        double stdDevY = CalculateFeature.countStdDev(listDataY);
        double stdDevZ = CalculateFeature.countStdDev(listDataZ);

        double maxX = CalculateFeature.countMax(listDataX);
        double maxY = CalculateFeature.countMax(listDataY);
        double maxZ = CalculateFeature.countMax(listDataZ);

        double minX = CalculateFeature.countMin(listDataX);
        double minY = CalculateFeature.countMin(listDataY);
        double minZ = CalculateFeature.countMin(listDataZ);

        double avgAbsDiffX = CalculateFeature.countAvgAbsDiff(listDataX);
        double avgAbsDiffY = CalculateFeature.countAvgAbsDiff(listDataY);
        double avgAbsDiffZ = CalculateFeature.countAvgAbsDiff(listDataZ);

        double avgResAcc = CalculateFeature.countAvgResAcc(listDataX, listDataY, listDataZ);

        DataPoint datapoint = new DataPoint();
        datapoint.setAvg_x(averageX);
        datapoint.setAvg_y(averageY);
        datapoint.setAvg_z(averageZ);
        datapoint.setStd_dev_x(stdDevX);
        datapoint.setStd_dev_y(stdDevY);
        datapoint.setStd_dev_z(stdDevZ);
        datapoint.setMax_x(maxX);
        datapoint.setMax_y(maxY);
        datapoint.setMax_z(maxZ);
        datapoint.setMin_x(minX);
        datapoint.setMin_y(minY);
        datapoint.setMin_z(minZ);
        datapoint.setAvgabsdif_x(avgAbsDiffX);
        datapoint.setAvgabsdif_y(avgAbsDiffY);
        datapoint.setAvgabsdif_z(avgAbsDiffZ);
        datapoint.setAvgresacc(avgResAcc);
        return datapoint;
    }

    private void runPredict() {
        final ArrayList<String[]> testListCopy = new ArrayList<>(testList);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                DataPoint resultPoint = extractFeatures(testListCopy);

                Log.d(TAG, "Is listData empty: " + testListCopy.isEmpty());
                Log.d(TAG, "listData Features: " + String.valueOf(resultPoint.getAvg_x()) + ", " + String.valueOf(resultPoint.getAvg_y())
                                                + ", " + String.valueOf(resultPoint.getAvg_z()) + ", " + String.valueOf(resultPoint.getAvgresacc()));

                KNNClassifier knnClassifier = new KNNClassifier(K);
                knnClassifier.setListTrainData(listTrainData);
                Category result = knnClassifier.predict(resultPoint);
                Log.d(TAG, "Prediction Result: " + result.name());
                resultCategory.setText("Prediction Result: " + result.name());
            }
        });
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timeRecord.setText(time);

                if (timeRunning) {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    View.OnClickListener operasi = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnStart:
                    startRecord();
                    break;
                case R.id.btnStop:
                    stopRecord();
                    break;
                case R.id.btnTest:
                    testData();
                    break;

                case R.id.bt_location:
                    cekLokasi();
                    break;
            }
        }
    };

    private void cekLokasi() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {

                    try {

                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );

                        textView1.setText(Html.fromHtml(
                                "<font color = '#6200EE'> <b>Latitude : </b><br></font>"
                                + addresses.get(0).getLatitude()
                        ));

                        textView2.setText(Html.fromHtml(
                                "<font color = '#6200EE'> <b>Longitude : </b><br></font>"
                                        + addresses.get(0).getLongitude()
                        ));

                        textView3.setText(Html.fromHtml(
                                "<font color = '#6200EE'> <b>Posisi Alamat : </b><br></font>"
                                        + addresses.get(0).getAddressLine(0)
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startRecord() {
        if (isRecordAlreadyStarted) {
            stopRecord();
        }
        timeRunning = true;
        isRecordAlreadyStarted = true;

        listTrainData = readCsvData(KNNClassifier.TRAIN_FILE_NAME);
        Date dateTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentTime = dateFormat.format(dateTime);
        Log.d(TAG, "btnStart: Current Time = " + currentTime);

        csvFileName = CSV_NAME + "_" + currentTime + ".csv";
        String csvCompleteFileName = (getExternalFilesDir(null).getAbsolutePath() + "/" + csvFileName);
        Log.d(TAG, "btnStart: CSV File Name = " + csvCompleteFileName);

        try {
            csvWriter = new CSVWriter(new FileWriter(csvCompleteFileName));
            dataList.add(new String[]{"X", "Y", "Z"});
        } catch (Exception e) {
            Log.d(TAG, "btnStart: Error = " + e.getMessage());
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException ie) {
                    Log.d(TAG, "btnStart: CSVWriter Close Error = " + ie.getMessage());
                }
            }
        }

        sensorManager.registerListener(MainActivity.this, mAccelerometer, SENSOR_DELAY);
        Log.d(TAG, "btnStart: Register accelerometer listener");
    }

    private void stopRecord() {
        timeRunning = false;
        seconds = 0;
        isRecordAlreadyStarted = false;

        sensorManager.unregisterListener(MainActivity.this);
        Log.d(TAG, "btnStop: Unregister accelerometer listener");

        listTrainData.clear();

        if (csvWriter != null) {
            csvWriter.writeAll(dataList);
            try {
                csvWriter.close();
            } catch (IOException ie) {
                Log.d(TAG, "btnStop: CSVWriter Close Error = " + ie.getMessage());
            }
            Toast.makeText(MainActivity.this, "File " + csvFileName + " berhasil disimpan", Toast.LENGTH_LONG).show();
        }

        dataList.clear();

        xValue.setText("xValue: 0.0");
        yValue.setText("yValue: 0.0");
        zValue.setText("zValue: 0.0");
    }

    private List<DataPoint> readCsvData(String filename) {
        try {
            List<DataPoint> listData = new ArrayList<>();
            String csvCompleteFileName = (getExternalFilesDir(null).getAbsolutePath() + "/" + filename);
            Log.d(TAG, "Complete location: " + csvCompleteFileName);
            BufferedReader br = new BufferedReader(new FileReader(csvCompleteFileName));
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (lineNumber > 1) {
                    String[] values = line.split(",");
//                    Log.d(TAG, "Line # " + lineNumber);
//                    Log.d(TAG, values[0] + " " + values[1] + " " + values[DataPoint.CATEGORY_INDEX]);

                    DataPoint data = new DataPoint();
                    data.setAvg_x(Double.parseDouble(values[DataPoint.AVG_INDEX_X]));
                    data.setAvg_y(Double.parseDouble(values[DataPoint.AVG_INDEX_Y]));
                    data.setAvg_z(Double.parseDouble(values[DataPoint.AVG_INDEX_Z]));

                    data.setStd_dev_x(Double.parseDouble(values[DataPoint.STDDEV_INDEX_X]));
                    data.setStd_dev_y(Double.parseDouble(values[DataPoint.STDDEV_INDEX_Y]));
                    data.setStd_dev_z(Double.parseDouble(values[DataPoint.STDDEV_INDEX_Z]));

                    data.setMax_x(Double.parseDouble(values[DataPoint.MAX_INDEX_X]));
                    data.setMax_y(Double.parseDouble(values[DataPoint.MAX_INDEX_Y]));
                    data.setMax_z(Double.parseDouble(values[DataPoint.MAX_INDEX_Z]));

                    data.setMin_x(Double.parseDouble(values[DataPoint.MIN_INDEX_X]));
                    data.setMin_y(Double.parseDouble(values[DataPoint.MIN_INDEX_Y]));
                    data.setMin_z(Double.parseDouble(values[DataPoint.MIN_INDEX_Z]));

                    data.setAvgabsdif_x(Double.parseDouble(values[DataPoint.AVGABSDIF_INDEX_X]));
                    data.setAvgabsdif_y(Double.parseDouble(values[DataPoint.AVGABSDIF_INDEX_Y]));
                    data.setAvgabsdif_z(Double.parseDouble(values[DataPoint.AVGABSDIF_INDEX_Z]));

                    data.setAvgresacc(Double.parseDouble(values[DataPoint.AVGRESACC_INDEX]));

                    switch ((int) Double.parseDouble(values[DataPoint.CATEGORY_INDEX])) {
                        case 0:
                            data.setCategory(Category.valueOf("DIAM"));
                            break;
                        case 1:
                            data.setCategory(Category.valueOf("SEPEDA_MOTORAN"));
                            break;
                        case 2:
                            data.setCategory(Category.valueOf("LOMPAT_LOMPAT"));
                            break;
                    }

                    listData.add(data);
                }
            }

            return listData;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

    private void testData() {
        try {
            List<DataPoint> listTrainDataCoba = readCsvData(KNNClassifier.TRAIN_FILE_NAME);
            List<DataPoint> listTestDataCoba = readCsvData(KNNClassifier.TEST_FILE_NAME);

            KNNClassifier knnClassifier = new KNNClassifier(5);
            knnClassifier.setListTrainData(listTrainDataCoba);
            knnClassifier.setListTestData(listTestDataCoba);
            knnClassifier.setListTestValidator(listTestDataCoba);

            Log.d(TAG, "Accuracy: " + knnClassifier.checkAccuracy());
            accuracy.setText(String.valueOf(knnClassifier.checkAccuracy()));
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}