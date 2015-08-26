package com.example.jamin.charmander;

import android.provider.BaseColumns;

/**
 * Created by jamin on 8/21/15.
 * Influenced by http://developer.android.com/training/basics/data-storage/databases.html
 */
public final class IndexReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public IndexReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class IndexEntry implements BaseColumns {
        public static final String TABLE_NAME = "Indexes";
        public static final String COLUMN_NAME_TABLE_ID = "TableId";
        public static final String COLUMN_NAME_DATE = "Date";
    }
}