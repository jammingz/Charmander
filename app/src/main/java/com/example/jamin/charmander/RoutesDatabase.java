package com.example.jamin.charmander;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamin on 8/25/15.
 */
public final class RoutesDatabase {
    private static final String TAG = "RoutesDatabase";

    private List<Route> routesDatabase;
    private List<String> routesName;
    private Context context;

    public RoutesDatabase(Context context) {
        routesDatabase = new ArrayList<Route>();
        routesName = new ArrayList<String>();
        this.context = context;
    }

    // Load database from SQLite
    public void load() {
        String userHash = generateUserHashCode(context);
        boolean authenticateFlag = DatabaseHelper.authenticateUser(context, userHash); // We are checking if user hash code is in database
        if (!authenticateFlag) {
            // If there is no entry for the current user, we will initialize database and create save entry into SQL database
            DatabaseHelper.writeIndexRow(context, userHash); // Create an entry for the current user
            DatabaseHelper.createUserTable(context, userHash); // Create a table for the current user
        } else {
            // We will fetch the database from the SQLite database since it exists
            Log.d(TAG,"authenticate Flag: True");
            Cursor routesCursor = DatabaseHelper.readTable(context, DatabaseHelper.USER_DATABASE, userHash);
            routesCursor.moveToFirst();
            for (int i = 0; i < routesCursor.getCount(); i++) {
                // Each iteration is a different route
                String routeID = routesCursor.getString(routesCursor.getColumnIndex(UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID));
                routesName.add(routeID); // Add the name of the route into the name list ...right?. Half assed this and didnt give much thought if it'll work or not
                Cursor curRouteCursor = DatabaseHelper.readTable(context, DatabaseHelper.ROUTE_DATABASE, routeID);
                curRouteCursor.moveToFirst();
                Route curRoute = new Route();
                int lastGroupNum = 0; // Initalizes at 0. Keeps track of the group number of the last point
                RoutePointsSet curSet = new RoutePointsSet();
                for (int j = 0; j < curRouteCursor.getCount(); j++) {
                    // Each iteration is a set of coordinate
                    double curLatitude = curRouteCursor.getDouble(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE));
                    double curLongitude = curRouteCursor.getDouble(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE));
                    float curAccuracy = curRouteCursor.getFloat(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY));
                    int curListener = curRouteCursor.getInt(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER));
                    long curUTCTime = curRouteCursor.getLong(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_TIME));
                    int curGroupNum = curRouteCursor.getInt(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_GROUP));

                    if (lastGroupNum != curGroupNum) {
                        // We start a new set once the group number flag differs

                        // First we add the current set into the current route IF it is not empty
                        if (curSet.getSize() > 0) {
                            curRoute.addSet(curSet);
                        }

                        curSet = new RoutePointsSet();
                    }


                    RoutePoint curPoint = new RoutePoint(curLatitude, curLongitude, curAccuracy, curUTCTime, curListener);
                    curSet.addPoint(curPoint); // Add point to the corresponding set
                    lastGroupNum = curGroupNum; // Set the group number flag to current group number
                    curRouteCursor.moveToNext(); // Moving to next row in the cursor
                }

                // Append last iteration of the set into the route. This set was not appended because loop terminated before reaching the if statement inside the loop to flush the last set
                if (curSet.getSize() > 0) {
                    curRoute.addSet(curSet);
                }

                curRouteCursor.close();

                // Add current Route object into our list of Routes
                routesDatabase.add(curRoute);

                // iterate to next route
                routesCursor.moveToNext();
            }

            routesCursor.close();
        }
    }

    public int getSize() {
        return routesDatabase.size();
    }

    public List<Route> getRoutes() {
        return routesDatabase;
    }

    public Route getRoute(int n) {
        return routesDatabase.get(n);
    }
    public String getRouteName(int n) {
        return routesName.get(n);
    }

    public String generateUserHashCode(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d("RoutesDatabase", "USER ID: _" + android_id);
        return "_" + android_id; // Adds a '_' in front of hash so we can store into SQLite table. table names cannot start with numbers.'24d3a' -> '_24d3a'
    }

    public void saveRoute(Route route) {
        // Do nothing if route is empty
        if (route.getSize() == 0) {
            return;
        }


        // Now we import data to SQL database
        String userHash = generateUserHashCode(context);
        boolean authenticateFlag = DatabaseHelper.authenticateUser(context, userHash); // We are checking if user is in database
        if (!authenticateFlag) {
            // If there is no entry for the current user, we will initialize database and create save entry into SQL database
            DatabaseHelper.writeIndexRow(context, userHash); // Create an entry for the current user
            DatabaseHelper.createUserTable(context, userHash); // Create a table for the user to contain its routes
        }

        // We write to database

        // Writing to user database
        Cursor routesCursor = DatabaseHelper.readTable(context, DatabaseHelper.USER_DATABASE, userHash);
        routesCursor.moveToFirst();
        int numOfRoutes = routesCursor.getCount();

        String routeId = "Route" + String.valueOf(numOfRoutes + 1); // generic route ID

        java.util.Date utilNow = new java.util.Date();
        Date now = new Date(utilNow.getTime());

        DatabaseHelper.writeUserRow(context, userHash, routeId, now.getTime()); // writing to user table
        DatabaseHelper.writeRoute(context, routeId, route); // writing the coordinates into route table

        routesCursor.close();


        // Add to cached database
        routesDatabase.add(route);
        routesName.add(routeId);
    }

    public void closeSQLConnections() {
        DatabaseHelper.closeReadConnection();
        DatabaseHelper.closeWriteConnection();
        DatabaseHelper.closeDBCache();
    }

}
