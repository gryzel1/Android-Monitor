package com.grisel.monitor;

import android.provider.BaseColumns;

public final class SensorEntryContract {

    private SensorEntryContract() {}

    public static class SensorEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensors";
        public static final String COLUMN_NAME_SENSOR = "sensor";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATETIME = "datetime";
    }
}