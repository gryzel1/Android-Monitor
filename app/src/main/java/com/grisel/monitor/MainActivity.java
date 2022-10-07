package com.grisel.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleJobWiFi();
        getSensors();
    }

    private void scheduleJobWiFi() {
        ComponentName serviceName = new
                ComponentName(this, WifiJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, serviceName)
                .setPeriodic(1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this, "WiFi activity monitored", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWifi(String value){
        TextView label = findViewById(R.id.wifiNumber);
        label.setText(value);
    }

    private void getSensors(){
        SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor res = db.query(SensorEntryContract.SensorEntry.TABLE_NAME, null, null, null, null, null, null);

        while(res.moveToNext()){
            @SuppressLint("Range") String sensor = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR));
            @SuppressLint("Range") String value = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE));
            @SuppressLint("Range") String datetime = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME));
            updateWifi(sensor + " " + value + " " + datetime);
        }
    }

}