package com.example.jamin.charmander;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Created by jamin on 8/22/15.
 */
public final class DatabaseHelper {
    // Database constants
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String DATABASE_NAME = "CharmanderDatabase.db";



    private static final int INDEX_DATABASE = 0;
    protected static final int USER_DATABASE = 1;
    protected static final int ROUTE_DATABASE = 2;

    private static final String SQL_CREATE_INDEX_ENTRIES =
            "CREATE TABLE " + IndexReaderContract.IndexEntry.TABLE_NAME + " (" +
                    IndexReaderContract.IndexEntry._ID + " INTEGER PRIMARY KEY," +
                    IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_INDEX_ENTRIES =
            "DROP TABLE IF EXISTS " + IndexReaderContract.IndexEntry.TABLE_NAME;

    // Private constructor
    private DatabaseHelper() {

    }


    // Write Methods

    public static SQLiteDatabase getWritableDatabase(Context context, int databaseType) {
        return (new IndexReaderDbHelper(context)).getWritableDatabase();
    }

    public static SQLiteDatabase getWritableDatabase(Context context, int databaseType, String tableId) {
        if (databaseType < 0 || databaseType > 2) { // out of index
            return null;
        }

        if (databaseType == INDEX_DATABASE) {
            return (new IndexReaderDbHelper(context)).getWritableDatabase();
        } else if (databaseType == USER_DATABASE) {
            return (new UserReaderDbHelper(context, tableId)).getWritableDatabase();
        }

        // If not index or user table, we should return route table
        return (new RouteReaderDbHelper(context, tableId)).getWritableDatabase();

    }

    public static long writeIndexRow(Context context, String tableId) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase(context, INDEX_DATABASE);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID, tableId);
        // values.put(IndexReaderContract.IndexEntry.COLUMN_NAME_DATE, date);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                IndexReaderContract.IndexEntry.TABLE_NAME,
                null,
                values);

        db.close();
        return  newRowId;
    }

    public static long writeUserRow(Context context, String tableId, String routeId, long dateCreated) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase(context, USER_DATABASE, tableId);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID, routeId);
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED, dateCreated);
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED, dateCreated); // created date and modified date are the same

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                tableId,
                null,
                values);

        db.close();
        return newRowId;
    }

    public static long writeRouteRow(Context context, String routeId, double lat, double lng, float accuracy, int listener, long time, int groupNum) {
        // Gets the data repository in write mode
        SQLiteDatabase db = getWritableDatabase(context, ROUTE_DATABASE, routeId);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE, lng);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY, accuracy);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER, listener);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_TIME, time);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP, groupNum);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                routeId,
                null,
                values);

        db.close();
        return newRowId;
    }

    public static void writeRouteSet(Context context, String routeId, RoutePointsSet set) {
        int groupNum = getMaxGroup(context, routeId) + 1; // get the biggest group number in the table and increment by 1

        // Iterate across the set
        for (int i = 0; i < set.getSize(); i++) {
            // for each point
            RoutePoint curLocation = set.get(i);
            double lat = curLocation.getLatitude();
            double lng = curLocation.getLongitude();
            float acc = curLocation.getAccuracy();
            int listener = curLocation.getListener();
            long time = curLocation.getUTCTime();
            writeRouteRow(context, routeId, lat, lng, acc, listener, time, groupNum);
        }

    }


    // Update Methods

    public static int updateIndexRow(Context context, String oldTableId, String newTableId) {
        SQLiteDatabase db = getReadableDatabase(context, INDEX_DATABASE);

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID, newTableId);

        // Which row to update, based on the ID
        String selection = IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID + " LIKE ?";
        String[] selectionArgs = { oldTableId };

        int count = db.update(
                IndexReaderContract.IndexEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
        return count;
    }

    public static int updateUserRow(Context context, String tableId, String oldRouteId, String newRouteId) {
        SQLiteDatabase db = getReadableDatabase(context, USER_DATABASE, tableId);

        // New values
        ContentValues values = new ContentValues();
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID, newRouteId);


        // Create date string for current time (now)
        // SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date utilNow = new java.util.Date();
        Date now = new Date(utilNow.getTime());
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED, now.getTime()); // Updating modified date to current date

        // Which row to update, based on the ID
        String selection = UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID + " LIKE ?";
        String[] selectionArgs = { oldRouteId };

        int count = db.update(
                tableId,
                values,
                selection,
                selectionArgs);

        db.close();
        return count;
    }

    public static int updateRouteRow(Context context, String routeId, String columnName, Object oldValue, Object newValue) {
        SQLiteDatabase db = getReadableDatabase(context, ROUTE_DATABASE, routeId);

        // New values
        ContentValues values = new ContentValues();

        switch (columnName) {
            case RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE:
                values.put(columnName, (double) newValue);
                break;
            case RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE:
                values.put(columnName, (double) newValue);
                break;
            case RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY:
                values.put(columnName, (float) newValue);
                break;
            case RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER:
                values.put(columnName, (int) newValue);
                break;
            case RouteReaderContract.RouteEntry.COLUMN_NAME_TIME:
                values.put(columnName, (long) newValue);
                break;
        }

        // Which row to update, based on the ID
        String selection = columnName + " LIKE ?";
        String[] selectionArgs = { String.valueOf(oldValue) };

        int count = db.update(
                routeId,
                values,
                selection,
                selectionArgs);

        db.close();
        return count;
    }


    // Read Methods

    public static SQLiteDatabase getReadableDatabase(Context context, int databaseType) {
        return (new IndexReaderDbHelper(context)).getReadableDatabase();
    }

    public static SQLiteDatabase getReadableDatabase(Context context, int databaseType, String tableId) {
        if (databaseType < 0 || databaseType > 2) { // out of index
            return null;
        }

        if (databaseType == INDEX_DATABASE) {
            return (new IndexReaderDbHelper(context)).getReadableDatabase();
        } else if (databaseType == USER_DATABASE) {
            return (new UserReaderDbHelper(context, tableId)).getReadableDatabase();
        }


        // If not index or user table, we should return route table
        return (new RouteReaderDbHelper(context, tableId)).getReadableDatabase();
    }


    public static Cursor readIndexTable(Context context, String userHash) {
        SQLiteDatabase db = getReadableDatabase(context, INDEX_DATABASE);

        String[] projection = {
                IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID,
        };

        String sortOrder = null; // IndexReaderContract.IndexEntry.COLUMN_NAME_DATE + " ASC";
        String selection = IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID + " LIKE ?"; // We are looking for the table for the current user
        String[] selectionArgs = { userHash };


        Cursor c = db.query(
                IndexReaderContract.IndexEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        db.close();
        return c;
    }

    public static int getMaxGroup(Context context, String tableId) { // Get max group # for the current table
        // Accessing the route database
        SQLiteDatabase db = getReadableDatabase(context, ROUTE_DATABASE, tableId);
        String[] projection = new String[]{
                "MAX(" + RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP + ")"
        };

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        Cursor c = db.query(
                tableId,                                  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        db.close();

        if (c.getCount() == 0) {
            // No query. returns -1
            return -1;
        }

        // Else we return the max count
        return c.getInt(c.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP));
    }

    public static Cursor readTable(Context context, int databaseType, String tableId) {
        if (databaseType < 0 || databaseType > 2) { // out of index
            return null;
        }


        if (databaseType == INDEX_DATABASE) {
            return null; // should never reach here
        }

        SQLiteDatabase db;
        String[] projection;
        String sortOrder;
        String selection = null;
        String[] selectionArgs = null;

        if (databaseType == USER_DATABASE) {
            // Accessing the user database
            db = getReadableDatabase(context, USER_DATABASE, tableId);
            projection = new String[] {
                    UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID,
                    UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED,
                    UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED
            };

            sortOrder = UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED + " ASC";
        } else {
            // Accessing the route database
            db = getReadableDatabase(context, ROUTE_DATABASE, tableId);
            projection = new String[] {
                    RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE,
                    RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE,
                    RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY,
                    RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER,
                    RouteReaderContract.RouteEntry.COLUMN_NAME_TIME,
                    RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP
            };

            sortOrder = RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP + " ASC"; // Sort by group.
        }

        Cursor c = db.query(
                tableId,                                  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        db.close();
        return c;

    }

    public static class IndexReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;

        public IndexReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_INDEX_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_INDEX_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public static class UserReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        private final String TABLENAME;
        private final String SQL_CREATE_USER_ENTRIES;
        private final String SQL_DELETE_USER_ENTRIES;

        public UserReaderDbHelper(Context context, String tableName) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            TABLENAME = tableName;

            SQL_CREATE_USER_ENTRIES =
                    "CREATE TABLE " + tableName + " (" +
                            UserReaderContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                            UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
                            UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED + TEXT_TYPE + COMMA_SEP +
                            UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED + TEXT_TYPE +
                            " )";

            SQL_DELETE_USER_ENTRIES =
                    "DROP TABLE IF EXISTS " + TABLENAME;
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_USER_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_USER_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

    }

    public static class RouteReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        private final String TABLENAME;
        private final String SQL_CREATE_ROUTE_ENTRIES;
        private final String SQL_DELETE_ROUTE_ENTRIES;

        public RouteReaderDbHelper(Context context, String tableName) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            TABLENAME = tableName;

            SQL_CREATE_ROUTE_ENTRIES =
                    "CREATE TABLE " + tableName + " (" +
                            RouteReaderContract.RouteEntry._ID + " INTEGER PRIMARY KEY," +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY + REAL_TYPE + COMMA_SEP +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER + INTEGER_TYPE + COMMA_SEP +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP + INTEGER_TYPE + COMMA_SEP +
                            RouteReaderContract.RouteEntry.COLUMN_NAME_TIME + INTEGER_TYPE +
                            " )";

            SQL_DELETE_ROUTE_ENTRIES =
                    "DROP TABLE IF EXISTS " + TABLENAME;
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ROUTE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ROUTE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }


    }


}
