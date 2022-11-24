package com.grisel.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    HashMap <String, Float> temperature = new HashMap<>();
    HashMap <String, Float> humidity = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleJobTemperature();
        scheduleJobHumidity();

        SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        getSensors(dbHelper, db);

        new updateThread().start();

        startForegroundService();

        Button button= (Button)findViewById(R.id.deleteDB);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                db.delete(SensorEntryContract.SensorEntry.TABLE_NAME, null, null);
//                clearWifi();
            }
        });

        updateGraph(temperature, humidity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //when clicking on the graph, it will open the datalist activity
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DatalistActivity.class);
                startActivity(intent);
            }
        });
    }

    public final void scheduleJobWiFi() {
        ComponentName serviceName = new
                ComponentName(this, WifiJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, serviceName)
                .setMinimumLatency(1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this, "WiFi activity monitored", Toast.LENGTH_SHORT).show();
        }
    }

    public final void scheduleJobTemperature() {
        ComponentName serviceName = new
                ComponentName(this, TemperatureJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, serviceName)
                .setMinimumLatency(1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this, "Temperature activity monitored", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleJobHumidity() {
        ComponentName serviceName = new
                ComponentName(this, HumidityJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(2, serviceName)
                .setMinimumLatency(1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this, "Humidity activity monitored", Toast.LENGTH_SHORT).show();
        }
    }

//    private void updateWifi(String value){
//        TextView label = findViewById(R.id.wifiNumber);
//        label.setText(label.getText()+"\n"+value);
//    }
//
//    private void updateText(String value){
//        TextView label = findViewById(R.id.wifiNumber);
//        label.setText(value);
//    }

//    private void clearWifi(){
//        TextView label = findViewById(R.id.wifiNumber);
//        label.setText("");
//    }

    private void getSensors(){
//        clearWifi();

        SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor res = db.query(SensorEntryContract.SensorEntry.TABLE_NAME, null, null, null, null, null, null);

        temperature.clear();
        humidity.clear();

        try {
            while(res.moveToNext()){
                @SuppressLint("Range") String sensor = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR));
                @SuppressLint("Range") String value = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE));
                @SuppressLint("Range") String datetime = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME));
                if (sensor.equals("Temperature")){
                    if(value != null){
                        temperature.put(datetime, Float.parseFloat(value));
                    }
                }else if (sensor.equals("Humidity")){
                    if(value != null){
                        humidity.put(datetime, Float.parseFloat(value));
                    }
                }
            }
        }finally {
            if (res != null && !res.isClosed()){
                res.close();
            }
//            updateText("Temperature: " + temperature.toString() + "\nHumidity: " + humidity.toString());
            updateGraph(temperature, humidity);
        }

    }

    private void getSensors(SensorReaderDbHelper dbHelper, SQLiteDatabase db){
//        clearWifi();

        Cursor res = db.query(SensorEntryContract.SensorEntry.TABLE_NAME, null, null, null, null, null, null);

        temperature.clear();
        humidity.clear();

        try {
            while(res.moveToNext()){
                @SuppressLint("Range") String sensor = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR));
                @SuppressLint("Range") String value = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE));
                @SuppressLint("Range") String datetime = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME));
                if (sensor.equals("Temperature")){
                    if(value != null){
                        temperature.put(datetime, Float.parseFloat(value));
                    }
                }else if (sensor.equals("Humidity")){
                    if(value != null){
                        humidity.put(datetime, Float.parseFloat(value));
                    }
                }
            }
        } finally {
            if (res != null && !res.isClosed()){
                res.close();
            }
//            updateText("Temperature: " + temperature.toString() + "\nHumidity: " + humidity.toString());
            updateGraph(temperature, humidity);
        }

    }

    private void updateGraph(HashMap temperature, HashMap humidity){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        HashMap<Float, ArrayList<Float>> link = new HashMap<>();
        for (Object key : temperature.keySet()){
            Float value = (Float) temperature.get(key);
            if (link.containsKey(value)){
                ArrayList<Float> list = link.get(value);
                if (humidity.containsKey(key)){
                    list.add((Float) humidity.get(key));
                    link.put(value, list);
                }
            }else{
                ArrayList<Float> list = new ArrayList<>();
                if (humidity.containsKey(key)){
                    list.add((Float) humidity.get(key));
                    link.put(value, list);
                }
            }
        }
        DataPoint[] dataPoints = new DataPoint[link.size()];
        int i = 0;
        for (Object key : link.keySet()){
            ArrayList<Float> list = link.get(key);
            float sum = 0;
            for (Float value : list){
                sum += value;
            }
            dataPoints[i] = new DataPoint((Float) key, sum/list.size());
            i++;
        }

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(dataPoints);
        graph.removeAllSeries();
        graph.addSeries(series);

        graph.setTitle("Temperature vs Humidity");
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollable(true);

    }

    private HashMap<Float, Float[]> linkSensors(HashMap<String, Float> temperature, HashMap<String, Float> humidity){
        HashMap<Float, Float[]> linkedSensors = new HashMap<>();
        for (String key : temperature.keySet()){
            Float[] values = new Float[2];
            values[0] = temperature.get(key);
            values[1] = humidity.get(key);
            linkedSensors.put(Float.parseFloat(key), values);
        }
        return linkedSensors;
    }

    class updateThread extends Thread {

        updateThread() {
            super("updateThread");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        getSensors();
                    }
                });
            }
        }
    }

    public void startForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopForegroundService(View view) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

}