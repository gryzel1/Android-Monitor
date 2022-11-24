package com.grisel.monitor;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class TemperatureJobService extends JobService {
    boolean shouldReschedule = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.scheduleJobTemp();
        Log.d("Temperature", "onStartJob id=" + params.getJobId());
        TemperatureJobService.tempThread thread = new TemperatureJobService.tempThread(this, params);
        thread.start();
        return true;
    }

    public final void scheduleJobTemp() {
        ComponentName serviceName = new
                ComponentName(this, TemperatureJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, serviceName)
                .setMinimumLatency(120000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("Temperature", "onStopJob id=" + params.getJobId());
        return shouldReschedule;
    }

    private int getTemp() throws IOException, JSONException {
        Location loc = getLastKnownLocation();

        URL url = null;
        url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=43.5937200564988&lon=1.4270044246121703&appid=e369d5e5b1411b3ee51d2384c398a391");
        HttpURLConnection con = null;
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();

        if (con.getResponseCode() != 200) {
            return 0;
        }

        String inline = "";
        Scanner scanner = null;
        try {
            scanner = new Scanner(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        scanner.close();
        JSONObject data = new JSONObject(inline);
        JSONObject main = data.getJSONObject("main");
        return main.getInt("temp")-273;
    }

    class tempThread extends Thread {
        private TemperatureJobService tempJobService;
        private JobParameters params;

        tempThread(TemperatureJobService tempJobService, JobParameters params) {
            this.tempJobService = tempJobService;
            this.params = params;
        }

        @Override
        public void run() {
            String time = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());

            SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(TemperatureJobService.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR, "Temperature");
            try {
                values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE, getTemp());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            values.put(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME, time);

            db.beginTransaction();
            long newRowId = db.insert(SensorEntryContract.SensorEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();

            tempJobService.jobFinished(params, false);
        }
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
