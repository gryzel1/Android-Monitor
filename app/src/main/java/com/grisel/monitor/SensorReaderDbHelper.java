package com.grisel.monitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SensorReaderDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SensorEntryContract.SensorEntry.TABLE_NAME + " (" +
                    SensorEntryContract.SensorEntry._ID + " TEXT PRIMARY KEY," +
                    SensorEntryContract.SensorEntry.COLUMN_NAME_SENSOR + " TEXT," +
                    SensorEntryContract.SensorEntry.COLUMN_NAME_VALUE + " TEXT," +
                    SensorEntryContract.SensorEntry.COLUMN_NAME_DATETIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SensorEntryContract.SensorEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sensorData.db";

    public SensorReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
