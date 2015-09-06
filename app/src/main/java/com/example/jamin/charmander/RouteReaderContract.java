package com.example.jamin.charmander;

import android.provider.BaseColumns;

/**
 * Created by jamin on 8/23/15.
 */
public class RouteReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RouteReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class RouteEntry implements BaseColumns {
        // public static final String TABLE_NAME; Dynamic table name
        public static final String COLUMN_NAME_LATITUDE = "Latitude";
        public static final String COLUMN_NAME_LONGITUDE = "Longitude";
        public static final String COLUMN_NAME_ACCURACY = "Accuracy";
        public static final String COLUMN_NAME_LISTENER = "Listener";
        public static final String COLUMN_NAME_TIME = "Time";
        public static final String COLUMN_NAME_GROUP = "GroupNum"; // integer to determine which set # the point belongs to
    }
}