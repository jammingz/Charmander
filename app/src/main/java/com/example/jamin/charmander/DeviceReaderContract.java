package com.example.jamin.charmander;

import android.provider.BaseColumns;

/**
 * Created by jamin on 8/23/15.
 */
public class DeviceReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DeviceReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class DeviceEntry implements BaseColumns {
        // public static final String TABLE_NAME; Dynamic table name
        public static final String COLUMN_NAME_TRACK_ID = "TrackId";
        public static final String COLUMN_NAME_DATE_CREATED = "Date Created";
        public static final String COLUMN_NAME_DATE_MODIFIED = "Date Modified";
    }
}