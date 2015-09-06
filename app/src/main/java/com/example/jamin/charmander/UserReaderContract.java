package com.example.jamin.charmander;

import android.provider.BaseColumns;

/**
 * Created by jamin on 8/23/15.
 */
public class UserReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public UserReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class UserEntry implements BaseColumns {
        // public static final String TABLE_NAME; Dynamic table name
        public static final String COLUMN_NAME_ROUTE_ID = "RouteId";
        public static final String COLUMN_NAME_DATE_CREATED = "DateCreated";
        public static final String COLUMN_NAME_DATE_MODIFIED = "DateModified";
    }
}