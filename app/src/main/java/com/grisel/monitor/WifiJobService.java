package com.grisel.monitor;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class WifiJobService extends JobService {

    boolean shouldReschedule = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("Wifi", "onStartJob id=" + params.getJobId());
        wifiThread thread = new wifiThread(this, params);
        thread.start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("Wifi", "onStopJob id=" + params.getJobId());
        return shouldReschedule;
    }

    private int getWifiAPs(){
        WifiManager wifiMan = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiMan.getScanResults();
        return scanResults.size();
    }

    class wifiThread extends Thread {
        private WifiJobService wifiJobService;
        private JobParameters params;

        wifiThread(WifiJobService wifiJobService, JobParameters params) {
            this.wifiJobService = wifiJobService;
            this.params = params;
        }

        @Override
        public void run() {
            String time = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());

            SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(WifiJobService.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR, "WiFi");
            values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE, getWifiAPs());
            values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME, time);

            long newRowId = db.insert(SensorEntryContract.SensorEntry.TABLE_NAME, null, values);

            wifiJobService.jobFinished(params, false);
        }
    }
}