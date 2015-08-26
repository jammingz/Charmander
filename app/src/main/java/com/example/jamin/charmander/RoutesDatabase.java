package com.example.jamin.charmander;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamin on 8/25/15.
 */
public class RoutesDatabase {
    private List<Route> routesDatabase;

    public RoutesDatabase() {
        routesDatabase = new ArrayList<Route>();
    }

    // Load database from SQLite
    public void load(Context context) {
        String userHash = generateUserHashCode(context);
        Cursor routesTableHashCursor = DatabaseHelper.readIndexTable(context, userHash); // We are getting the hash to the routes table given our current user id
        routesTableHashCursor.moveToFirst();
        if (routesTableHashCursor.getCount() == 0) {
            // If there is no entry for the current user, we will initialize database and create save entry into SQL database
            DatabaseHelper.writeIndexRow(context, userHash); // Create an entry for the current user
        } else {
            // We will fetch the database from the SQLite database since it exists

            String hashCode = routesTableHashCursor.getString(routesTableHashCursor.getColumnIndex(IndexReaderContract.IndexEntry.COLUMN_NAME_TABLE_ID));
            Cursor routesCursor = DatabaseHelper.readTable(context, DatabaseHelper.USER_DATABASE, hashCode);
            routesCursor.moveToFirst();
            for (int i = 0; i < routesCursor.getCount(); i++) {
                // Each iteration is a different route
                String routeID = routesCursor.getString(routesCursor.getColumnIndex(UserReaderContract.UserEntry.COLUMN_NAME_ROUTE_ID));
                Cursor curRouteCursor = DatabaseHelper.readTable(context, DatabaseHelper.ROUTE_DATABASE, routeID);
                curRouteCursor.moveToFirst();
                Route curRoute = new Route();

                for (int j = 0; j < curRouteCursor.getCount(); j++) {
                    // Each iteration is a set of coordinate
                    double curLatitude = curRouteCursor.getDouble(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LATITUDE));
                    double curLongitude = curRouteCursor.getDouble(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LONGITUDE));
                    float curAccuracy = curRouteCursor.getFloat(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_ACCURACY));
                    int curListener = curRouteCursor.getInt(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_LISTENER));
                    long curUTCTime = curRouteCursor.getLong(curRouteCursor.getColumnIndex(RouteReaderContract.RouteEntry.COLUMN_NAME_TIME));

                    RoutePoint curPoint = new RoutePoint(curLatitude, curLongitude, curAccuracy, curUTCTime, curListener);
                    curRoute.addPoint(curPoint);
                    curRouteCursor.moveToNext(); // Moving to next row in the cursor
                }
                curRouteCursor.close();;
            }

            routesCursor.close();
        }

        routesTableHashCursor.close();
    }

    public int getSize() {
        return routesDatabase.size();
    }

    public List<Route> getRoutes() {
        return routesDatabase;
    }

    public String generateUserHashCode(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public void saveRoute(List<List<Location>> locationsList) {

    }

}
