package com.example.accelerometersensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "DEBUG_TAG";
    private static final int SENSOR_DELAY = 100000;
    private static final String CSV_NAME = "accelerometer_data";
    private String csvFileName;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private TextView xValue, yValue, zValue, timeRecord, accuracy;

    private int seconds = 0;
    private Boolean timeRunning = false;
    private Boolean isRecordAlreadyStarted = false;

    private ArrayList<String[]> dataList;
    private CSVWriter csvWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        accuracy = (TextView) findViewById(R.id.accuracy);
        timeRecord = (TextView) findViewById(R.id.timeRecord);
        findViewById(R.id.btnStart).setOnClickListener(operasi);
        findViewById(R.id.btnStop).setOnClickListener(operasi);
        findViewById(R.id.btnTest).setOnClickListener(operasi);

        runTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: X: " + event.values[0] + "Y: " + event.values[1] + "Z: " + event.values[2]);

        xValue.setText("xValue: " + event.values[0]);
        yValue.setText("yValue: " + event.values[1]);
        zValue.setText("zValue: " + event.values[2]);

        dataList.add(new String[]{String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])});
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
            }
        }
    };

    private void startRecord() {
        if (isRecordAlreadyStarted) {
            stopRecord();
        }
        timeRunning = true;
        isRecordAlreadyStarted = true;

        Date dateTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentTime = dateFormat.format(dateTime);
        Log.d(TAG, "btnStart: Current Time = " + currentTime);

        csvFileName = CSV_NAME + "_" + currentTime + ".csv";
        String csvCompleteFileName = (getExternalFilesDir(null).getAbsolutePath() + "/" + csvFileName);
        Log.d(TAG, "btnStart: CSV File Name = " + csvCompleteFileName);

        try {
            csvWriter = new CSVWriter(new FileWriter(csvCompleteFileName));
            dataList = new ArrayList<>();
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

        if (csvWriter != null) {
            csvWriter.writeAll(dataList);
            try {
                csvWriter.close();
            } catch (IOException ie) {
                Log.d(TAG, "btnStop: CSVWriter Close Error = " + ie.getMessage());
            }
            dataList.clear();
            Toast.makeText(MainActivity.this, "File " + csvFileName + " berhasil disimpan", Toast.LENGTH_LONG).show();
        }

        xValue.setText("xValue: 0.0");
        yValue.setText("yValue: 0.0");
        zValue.setText("zValue: 0.0");
    }

    private List<DataPoint> readCsvData(String filename) {
        try {
            List<DataPoint> listData = new ArrayList<>();
            String csvCompleteFileName = (getExternalFilesDir(null).getAbsolutePath() + "/" + filename);
            BufferedReader br = new BufferedReader(new FileReader(csvCompleteFileName));
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (lineNumber > 1) {
                    String[] values = line.split(",");
                    Log.d(TAG, "Line # " + lineNumber);
                    Log.d(TAG, values[0] + " " + values[1] + " " + values[DataPoint.CATEGORY_INDEX]);

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

                    data.setAvgresacc_x(Double.parseDouble(values[DataPoint.AVGRESACC_INDEX_X]));
                    data.setAvgresacc_y(Double.parseDouble(values[DataPoint.AVGRESACC_INDEX_Y]));
                    data.setAvgresacc_z(Double.parseDouble(values[DataPoint.AVGRESACC_INDEX_Z]));

                    switch ((int) Double.parseDouble(values[DataPoint.CATEGORY_INDEX])) {
                        case 1:
                            data.setCategory(Category.valueOf("SEPEDA_MOTORAN"));
                            break;
                        case 2:
                            data.setCategory(Category.valueOf("LARI_LARI"));
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
            List<DataPoint> listTrainData = readCsvData(KNNClassifier.TRAIN_FILE_NAME);
            List<DataPoint> listTestData = readCsvData(KNNClassifier.TEST_FILE_NAME);

            KNNClassifier knnClassifier = new KNNClassifier(5);
            knnClassifier.setListTrainData(listTrainData);
            knnClassifier.setListTestData(listTestData);
            knnClassifier.setListTestValidator(listTestData);

            Log.d(TAG, "Accuracy: " + knnClassifier.classify());
            accuracy.setText(String.valueOf(knnClassifier.classify()));
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}