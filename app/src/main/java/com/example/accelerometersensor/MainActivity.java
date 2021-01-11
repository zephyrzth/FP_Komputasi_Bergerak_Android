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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONObject;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private static final String TAG = "DEBUG_TAG";
    private static final int SENSOR_DELAY = 100000;
    private static final String CSV_NAME = "accelerometer_data";
    private static final int K = 5;
    private static final int MIN_DISTANCE_GPS = 20;     // meters
    private static final int MIN_TIME_GPS = 10000;      // miliseconds
    public static final int REQUEST_ACTIVITY_RECOGNITION = 23;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private TextView xValue, yValue, zValue, timeRecord, accuracy, resultCategory, latValue, longValue;
    private EditText namaText, urlText;
    private Button btnStart, btnStop, btnTest;

    private String csvFileName;
    private int seconds = 0;
    private Boolean timeRunning = false;
    private Boolean isRecordAlreadyStarted = false;

    private ArrayList<String[]> dataList, testList;
    private List<DataPoint> listTrainData;
    private CSVWriter csvWriter;

    private ApiInterface mApiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACTIVITY_RECOGNITION);
        }

        initWidgets();
        runTimer();
    }

    private void initWidgets() {
        Log.d(TAG, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_GPS, MIN_DISTANCE_GPS, this);

        mApiInterface = ApiClient.getClient().create(ApiInterface.class);

        dataList = new ArrayList<>();
        testList = new ArrayList<>();

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        latValue = (TextView) findViewById(R.id.latValue);
        longValue = (TextView) findViewById(R.id.longValue);
        namaText = (EditText) findViewById(R.id.namaText);
        urlText = (EditText) findViewById(R.id.urlText);
        accuracy = (TextView) findViewById(R.id.accuracy);
        timeRecord = (TextView) findViewById(R.id.timeRecord);
        resultCategory = (TextView) findViewById(R.id.resultCategory);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(operasi);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setEnabled(false);
        btnStop.setOnClickListener(operasi);
        btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(operasi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission berhasil dilakukan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission gagal dilakukan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latValue.setText(String.valueOf(location.getLatitude()));
        longValue.setText(String.valueOf(location.getLongitude()));
        Toast.makeText(MainActivity.this, "GPS Captured", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: X: " + event.values[0] + "Y: " + event.values[1] + "Z: " + event.values[2]);

        xValue.setText("xValue: " + event.values[0]);
        yValue.setText("yValue: " + event.values[1]);
        zValue.setText("zValue: " + event.values[2]);

        dataList.add(new String[]{String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])});
        testList.add(new String[]{String.valueOf(event.values[0]), String.valueOf(event.values[1]), String.valueOf(event.values[2])});
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
        ApiClient.BASE_URL = urlText.getText().toString();
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

                DataActivity newData = new DataActivity();
                newData.setNama_user(namaText.getText().toString());
                newData.setLabel_aktivitas(result.ordinal());
                newData.setLocations(new Double[] { Double.parseDouble(latValue.getText().toString()),
                        Double.parseDouble(longValue.getText().toString()) });

                Log.d(TAG, "Locations: " + Arrays.toString(newData.getLocations()));

                Call<PostPutDelData> postDataCall = mApiInterface.storeData(ApiClient.BASE_URL, newData);
                postDataCall.enqueue(new Callback<PostPutDelData>() {
                    @Override
                    public void onResponse(Call<PostPutDelData> call, Response<PostPutDelData> response) {
                        if (response.isSuccessful()) {
//                            PostPutDelData storeResult = response.body();
                            Toast.makeText(MainActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
//                                String responseBodyString = response.errorBody().string();
//                                Log.d(TAG, responseBodyString);
//                                JSONObject jsonObject = new JSONObject(responseBodyString);

                                Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.d(TAG, "Error Body JSON: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PostPutDelData> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Terjadi kesalahan", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error Retrofit Store: " + t.getMessage());
                    }
                });
            }
        });
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (seconds % 5 == 0 && seconds != 0) {
                    runPredict();
                    testList.clear();
                }

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
                    hideKeyboard(view);
                    startRecord();
                    break;
                case R.id.btnStop:
                    hideKeyboard(view);
                    stopRecord();
                    break;
                case R.id.btnTest:
                    hideKeyboard(view);
                    testData();
                    break;
            }
        }
    };

    private void hideKeyboard(View view) {
        InputMethodManager a = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        a.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean validateNullText(EditText t) {
        if (t.getText().toString().trim().matches("")) {
            t.setError("Field harus diisi!");
            return false;
        }
        return true;
    }

    private void startRecord() {
        if (isRecordAlreadyStarted) {
            stopRecord();
        }
        if (!validateNullText(urlText) || !validateNullText(namaText)) return;
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
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

        btnStop.setEnabled(false);
        btnStart.setEnabled(true);

        sensorManager.unregisterListener(MainActivity.this);
        Log.d(TAG, "btnStop: Unregister accelerometer listener");

        if (csvWriter != null) {
            csvWriter.writeAll(dataList);
            try {
                csvWriter.close();
            } catch (IOException ie) {
                Log.d(TAG, "btnStop: CSVWriter Close Error = " + ie.getMessage());
            }
            Toast.makeText(MainActivity.this, "File " + csvFileName + " berhasil disimpan", Toast.LENGTH_LONG).show();
        }

        listTrainData.clear();
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