package com.grisel.monitor;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashMap;

public class TemperatureActivity extends AppCompatActivity {

    HashMap <String, Float> temperature = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature);

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getData();
        displayTemperature();
    }

    private void getData(){
        SensorReaderDbHelper dbHelper = new SensorReaderDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor res = db.query(SensorEntryContract.SensorEntry.TABLE_NAME, null, null, null, null, null, null);

        temperature.clear();

        try {
            while(res.moveToNext()){
                @SuppressLint("Range") String sensor = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR));
                @SuppressLint("Range") String value = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE));
                @SuppressLint("Range") String datetime = res.getString(res.getColumnIndex(SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME));
                if(value != null && sensor.equals("Temperature")){
                    temperature.put(datetime, Float.parseFloat(value));
                }
            }
        }finally {
            if (res != null && !res.isClosed()){
                res.close();
            }
        }
    }

    private void displayTemperature(){
        for (String key : temperature.keySet()){
            CardView cardView = new CardView(this);
            cardView.setCardElevation(10);
            cardView.setRadius(15);
            cardView.setContentPadding(15, 15, 15, 15);
            cardView.setCardBackgroundColor(getResources().getColor(R.color.purple_700));
            cardView.setCardElevation(10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 30);
            cardView.setLayoutParams(params);

            TextView textView1 = new TextView(this);
            textView1.setText(temperature.get(key) + " at " + key);
            textView1.setTextColor(getResources().getColor(R.color.white));
            textView1.setTextSize(20);
            cardView.addView(textView1);

            LinearLayout linearLayout = findViewById(R.id.linearLayout);
            linearLayout.addView(cardView);
        }
    }
}
