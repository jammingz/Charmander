package com.example.jamin.charmander;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Created by jamin on 8/22/15.
 */
public final class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    // Database constants
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    protected static final int INDEX_DATABASE = 0;
    protected static final int USER_DATABASE = 1;
    protected static final int ROUTE_DATABASE = 2;

    private static final int READ_DATABASE = 0;
    private static final int WRITE_DATABASE = 1;


    private static SQLiteDatabase readDB = null;
    private static SQLiteDatabase writeDB = null;
    private static SQLiteDatabase readDBCache = null;
    private static SQLiteDatabase writeDBCache = null;

    // Private constructor
    private DatabaseHelper() {

    }


    // Write Methods
    public static SQLiteDatabase getWritableDatabase(Context context) {
        if (writeDBCache != null) {
            writeDB = writeDBCache;
        } else {
            writeDBCache = (new RouteReaderDbHelper(context)).getWritableDatabase();
            writeDB = writeDBCache;
        }

        return writeDB;
    }

    public static long writeIndexRow(Context context, String tableId) {
        if (!isConnected(WRITE_DATABASE)) {
            getWritableDatabase(context); // Connect to writeable database if we are not connected
        }

        SQLiteDatabase db = writeDB;
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

        return newRowId;
    }

    public static long writeUserRow(Context context, String userId, String routeId, long dateCreated) {
        if (!isConnected(WRITE_DATABASE)) {
            getWritableDatabase(context); // Connect to writeable database if we are not connected
        }

        SQLiteDatabase db = writeDB;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID, routeId);
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED, dateCreated);
        values.put(UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED, dateCreated); // created date and modified date are the same

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                userId,
                null,
                values);

        return newRowId;
    }

    public static void writeRouteRow(Context context, String routeId, double lat, double lng, float accuracy, int listener, long time, int groupNum) {
        if (!isConnected(WRITE_DATABASE)) {
            getWritableDatabase(context); // Connect to writeable database if we are not connected
        }

        SQLiteDatabase db = writeDB;

        // createRouteTable(context, routeId); // create table if it does not exist

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE, lat);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE, lng);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY, accuracy);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER, listener);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_TIME, time);
        values.put(RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP, groupNum);

        // Insert the new row,
        db.insert(routeId, null, values);
    }


    public static void writeRouteSet(Context context, String routeId, RoutePointsSet set) {// Close previous writeDatabase. see writeUserRow for explanation
        // We do not check connection to database because helper functions will check for us before the actual write process proceeds.

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

    public static void writeRoute(Context context, String routeId, Route route) {
        // Does not check for connection to database because helper function checks before writing

        // Create the route table
        createRouteTable(context, routeId);

        // Insert the data into table
        for (int i = 0; i < route.getSize(); i++) {
            // add each set into database
            RoutePointsSet curSet = route.get(i);
            writeRouteSet(context, routeId, curSet);
        }
    }


    // Read Methods
    public static SQLiteDatabase getReadableDatabase(Context context) {
        if (readDBCache != null) {
            readDB = readDBCache;
        } else {
            readDBCache = (new RouteReaderDbHelper(context)).getReadableDatabase();
            readDB = readDBCache;
        }
        return readDB;

    }

    // Confirm the user is in the database
    public static boolean authenticateUser(Context context, String userHash) {
        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
            Log.d(TAG,"authenticateUser.isconnected:false");
        }
        SQLiteDatabase db = readDB;

        String[] projection = {
                IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID,
        };

        String sortOrder = null; // IndexReaderContract.IndexEntry.COLUMN_NAME_DATE + " ASC";
        String selection = IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID + " = ?"; // We are looking for the table for the current user
        String[] selectionArgs = { userHash };

        Log.d(TAG, "Authenticate userString: " + selection + " /" +  userHash);


        Cursor cursor = db.query(
                IndexReaderContract.IndexEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();

        Log.d(TAG, "Authenticate Table Count: " + String.valueOf(count));
        if (count > 0) {
            // If we found a match of username in the database, we return true
            return true;
        }

        // Else we return false
        return false;
    }

    public static int getMaxGroup(Context context, String routeId) { // Get max group/set # for the current table
        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
        }
        SQLiteDatabase db = readDB;
        String[] projection = new String[]{
                "MAX(" + RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP + ")"
        };

        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;

        Cursor c = db.query(
                routeId,                                  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        if (c.getCount() == 0) {
            // No query. returns -1
            Log.d(TAG, "maxGroup SQL Query returns 0 count for cursor");
            return -1;
        }

        c.moveToFirst();
        // Else we return the max count
        int maxGroup = c.getInt(c.getColumnIndex("MAX("+RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP+")"));

        Log.d(TAG,"getMaxGroup(): " + String.valueOf(maxGroup));
        return maxGroup;
    }

    public static Cursor readTable(Context context, int databaseType, String tableId) {
        if (databaseType < 1 || databaseType > 2) { // out of index
            return null;
        }

        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
        }

        SQLiteDatabase db = readDB;
        String[] projection;
        String sortOrder;
        String selection = null;
        String[] selectionArgs = null;

        if (databaseType == USER_DATABASE) {
            projection = new String[] {
                    UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID,
                    UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED,
                    UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED
            };

            sortOrder = UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED + " ASC";
        } else {
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

        return c;
    }


    // Update Methods
    public static int updateIndexRow(Context context, String oldTableId, String newTableId) {
        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
        }

        SQLiteDatabase db = readDB;

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

        return count;
    }

    public static int updateUserRow(Context context, String userId, String oldRouteId, String newRouteId) {
        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
        }
        SQLiteDatabase db = readDB;

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
                userId,
                values,
                selection,
                selectionArgs);

        return count;
    }

    public static int updateRouteRow(Context context, String routeId, String columnName, Object oldValue, Object newValue) {
        if (!isConnected(READ_DATABASE)) {
            getReadableDatabase(context);
        }
        SQLiteDatabase db = readDB;

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
        return count;
    }



    public static class RouteReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + IndexReaderContract.IndexEntry.TABLE_NAME + " (" +
                        IndexReaderContract.IndexEntry._ID + " INTEGER PRIMARY KEY," +
                        IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID + TEXT_TYPE +
                        " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + IndexReaderContract.IndexEntry.TABLE_NAME;

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "CharmanderDatabase.db";

        public RouteReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "RouteReaderDBHelper.onCreate() Called");
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static boolean isConnected(int databaseType) {
        switch (databaseType) {
            case READ_DATABASE:
                return readDB != null;
            case WRITE_DATABASE:
                return writeDB != null;
        }
        return false;
    }

    public static void createUserTable(Context context, String tableName) {
        if (!isConnected(WRITE_DATABASE)) {
            // We connect to database
            writeDB = getWritableDatabase(context);
        }

        SQLiteDatabase db = writeDB;
        String SQL_CREATE_USER_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        UserReaderContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                        UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
                        UserReaderContract.UserEntry.COLUMN_NAME_DATE_CREATED + TEXT_TYPE + COMMA_SEP +
                        UserReaderContract.UserEntry.COLUMN_NAME_DATE_MODIFIED + TEXT_TYPE +
                        " )";

        Log.d(TAG, "Creating User Table: " + tableName);
        db.execSQL(SQL_CREATE_USER_ENTRIES);
    }

    public static void createRouteTable(Context context, String tableName) {
        if (!isConnected(WRITE_DATABASE)) {
            // We connect to database
            writeDB = getWritableDatabase(context);
        }

        SQLiteDatabase db = writeDB;
        String SQL_CREATE_ROUTE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        RouteReaderContract.RouteEntry._ID + " INTEGER PRIMARY KEY," +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY + REAL_TYPE + COMMA_SEP +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER + INTEGER_TYPE + COMMA_SEP +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP + INTEGER_TYPE + COMMA_SEP +
                        RouteReaderContract.RouteEntry.COLUMN_NAME_TIME + INTEGER_TYPE +
                        " )";

        Log.d(TAG, "Creating Route Table: " + tableName);
        db.execSQL(SQL_CREATE_ROUTE_ENTRIES);
    }

    public static void dropTable(Context context, String tableName) {
        if (!isConnected(WRITE_DATABASE)) {
            // We connect to database
            writeDB = getWritableDatabase(context);
        }

        SQLiteDatabase db = writeDB;
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + tableName;

        Log.d(TAG, "Dropping Table: " + tableName);
        db.execSQL(SQL_DELETE_ENTRIES);;
    }

    /*
    public static class UserReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
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


            Log.d(TAG,"new UserReaderDBHelper() (" + SQL_CREATE_USER_ENTRIES + ")");
        }
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "UserReader.onCreate() Called");

            db.execSQL(SQL_CREATE_USER_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_USER_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"db.execute(" + SQL_CREATE_USER_ENTRIES + ")");
            onUpgrade(db, oldVersion, newVersion);
        }

    }

    public static class RouteReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
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
            Log.d(TAG,"db.execute(" + SQL_CREATE_ROUTE_ENTRIES + ")");
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

    */

    public static void closeWriteConnection() {
        if (isConnected(WRITE_DATABASE)) {
            writeDB.close();
        }
    }

    public static void closeReadConnection() {
        if (isConnected(READ_DATABASE)) {
            readDB.close();
        }
    }

    public static void closeDBCache() {
        if (writeDBCache != null) {
            writeDBCache.close();
            writeDBCache = null;
            writeDB = null;
        }

        if (readDBCache != null) {
            readDBCache.close();
            readDBCache = null;
            readDB = null;
        }
    }

}
