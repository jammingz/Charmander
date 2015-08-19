package com.example.jamin.charmander;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.jamin.charmander.LocationService;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private boolean GPS_SWITCH;
    private final String locationProvider = LocationManager.GPS_PROVIDER;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;
    private TextView console;
    private String consoleString;
    private int currentFragment;
    private final int MAP_FRAGMENT = 1;
    private final int CONSOLE_FRAGMENT = 2;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private ConsoleFragment mConsoleFragment;
    private boolean isMapReady;
    private boolean isZoomLocal; // flag to zoom the first instance of map to the user's location
    private List<Location> coordsList;
    private List<List<Location>> mGPSList;
    private List<List<Location>> mNetworkList;
    private static final Object listLock = new Object();

    // Constants
    private static final int LOCATION_REQUEST_FREQUENCY = 5000;
    private static final int INITIAL_ZOOM_LEVEL = 17;
    private static final String TAG = "MainActivity";

    boolean gps_enabled = false;
    boolean network_enabled = false;
    private final Handler mHandler = new Handler();
    private boolean isListening;
    private boolean isAwake; // is the device awake? Only update coordinates when the device is awake
    private LocationService mService;
    private boolean isBound;


    // Defining the GPS Location Listener

    LocationListener GPSListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //timer.cancel();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(NetworkListener);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            //Toast.makeText(context, "gps enabled "+x + "\n" + y, duration).show();

            LatLng curLatLng = new LatLng(lat, lng);

            // Test pin
            mMap.addMarker(new MarkerOptions().position(curLatLng).title("GPS Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            //mGPSList.add(location);
            consoleString += "\nAdding GPS: (" + lat + "," + lng + ")";
            console.setText(consoleString);
            Log.d(TAG, "Adding GPS: (" + lat + "," + lng + ")");
            //coordsList.add(curLatLng);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };


    // Defining the Network Location Listener

    LocationListener NetworkListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //timer.cancel();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(GPSListener);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            //Toast.makeText(context, "network enabled"+x + "\n" + y, duration).show();

            LatLng curLatLng = new LatLng(lat, lng);

            // Test pin
            mMap.addMarker(new MarkerOptions().position(curLatLng).title("Network Pin"));
            //mNetworkList.add(location);

            consoleString += "\nAdding Network: (" + lat + "," + lng + ")";
            console.setText(consoleString);
            Log.d(TAG, "Adding Network: (" + lat + "," + lng + ")");
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /*

    private void getLastLocation() {
        double lat = 0.0;
        double lng = 0.0;

        //locationManager.removeUpdates(GPSListener);
        //locationManager.removeUpdates(NetworkListener);

        Location net_loc=null, gps_loc=null;
        if(gps_enabled)
            gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(network_enabled)
            net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //if there are both values use the latest one
        if(gps_loc != null && net_loc != null) {


            if(gps_loc.getTime()>net_loc.getTime())  {
                lat = gps_loc.getLatitude();
                lng = gps_loc.getLongitude();
                consoleString += "\ngps lastknown (first): ("+ lat + "," + lng +")";
                lat = net_loc.getLatitude();
                lng = net_loc.getLongitude();
                consoleString += "\nnetwork lastknown (second): ("+ lat + "," + lng +")";
            } else  {
                lat = net_loc.getLatitude();
                lng = net_loc.getLongitude();
                consoleString += "\nnetwork lastknown (first): ("+ lat + "," + lng +")";
                lat = gps_loc.getLatitude();
                lng = gps_loc.getLongitude();
                consoleString += "\ngps lastknown (second): ("+ lat + "," + lng +")";
            }


            // Most of the time we will choose GPS location but will choose network location if GPS has not updated

            long gps_time = gps_loc.getElapsedRealtimeNanos();
            long net_time = net_loc.getElapsedRealtimeNanos();
            float gps_acc = gps_loc.getAccuracy();
            float net_acc = net_loc.getAccuracy();
            double avg_gps_lat = 0.0;
            double avg_gps_lng = 0.0;
            double avg_net_lat = 0.0;
            double avg_net_lng = 0.0;

            if (mGPSList.size() > 0) {
                double total_gps_lat = 0.0;
                double total_gps_lng = 0.0;
                double total_gps_weight = 0.0;

                for (int i = 0; i < mGPSList.size(); i++) {
                    // iterate across all the points taken from the GPS in the alloted interval time
                    Location curLocation = mGPSList.get(i);
                    double cur_gps_lat = curLocation.getLatitude();
                    double cur_gps_lng = curLocation.getLongitude();
                    float cur_accuracy = curLocation.getAccuracy();
                    double cur_weight =  getWeightFromAccuracy(cur_accuracy);

                    total_gps_lat += cur_gps_lat * cur_weight;
                    total_gps_lng += cur_gps_lng * cur_weight;
                    total_gps_weight += cur_weight;

                }

                avg_gps_lat = total_gps_lat / total_gps_weight;
                avg_gps_lng = total_gps_lng / total_gps_weight;


                LatLng avgGPS = new LatLng(avg_gps_lat, avg_gps_lng);
                mMap.addMarker(new MarkerOptions().position(avgGPS).title("GPS Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                consoleString += "\nAdding GPS(Average): (" + avg_gps_lat + "," + avg_gps_lng + ")";
                console.setText(consoleString);

            }

            if (mNetworkList.size() > 0) {
                double total_net_lat = 0.0;
                double total_net_lng = 0.0;
                double total_net_weight = 0.0;

                for (int i = 0; i < mNetworkList.size(); i++) {
                    // iterate across all the points taken from the GPS in the alloted interval time
                    Location curLocation = mNetworkList.get(i);
                    double cur_net_lat = curLocation.getLatitude();
                    double cur_net_lng = curLocation.getLongitude();
                    float cur_accuracy = curLocation.getAccuracy();
                    double cur_weight =  getWeightFromAccuracy(cur_accuracy);

                    total_net_lat += cur_net_lat * cur_weight;
                    total_net_lng += cur_net_lng * cur_weight;
                    total_net_weight += cur_weight;

                }

                avg_net_lat = total_net_lat / total_net_weight;
                avg_net_lng = total_net_lng / total_net_weight;

                LatLng avgNetwork = new LatLng(avg_net_lat, avg_net_lng);
                mMap.addMarker(new MarkerOptions().position(avgNetwork).title("Network Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                consoleString += "\nAdding Network(Average): (" + avg_net_lat + "," + avg_net_lng + ")";
                console.setText(consoleString);
            }



            float time_diff = (gps_time - net_time) / 1000000; // milliseconds
            //consoleString += "\nGPS Accuracy: " + gps_acc + "\nNet Accuracy: " + net_acc;
            //consoleString += "\nTime Difference: " + time_diff;
            //console.setText(consoleString);
        } else if (gps_loc != null) {
            // only gps location is available

            lat = gps_loc.getLatitude();
            lng = gps_loc.getLongitude();
            consoleString += "\ngps lastknown: ("+ lat + "," + lng +")";


            float gps_acc = gps_loc.getAccuracy();
            consoleString += "\nGPS Accuracy: " + gps_acc;
            console.setText(consoleString);
        } else  if (net_loc != null){
            // only network location is available

            lat = net_loc.getLatitude();
            lng = net_loc.getLongitude();
            consoleString += "\nnetwork lastknown: ("+ lat + "," + lng +")";


            float net_acc = net_loc.getAccuracy();
            consoleString += "\nNet Accuracy: " + net_acc;
            console.setText(consoleString);
        } else {
            // both network and gps locations are unavailable
            consoleString += "\nno last know avilable";
            console.setText(consoleString);
            return;
        }

        // If we are currently not zoomed on a location, we will zoom to the best estimated coordinate found just now
        if (!isZoomLocal) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng))      // Sets the center of the map to current location
                    .zoom(INITIAL_ZOOM_LEVEL)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            isZoomLocal = true;
        }
        console.setText(consoleString);


        mGPSList = new ArrayList<Location>(); // resets list
        mNetworkList = new ArrayList<Location>(); // resets list
        locationManager.removeUpdates(GPSListener);
        locationManager.removeUpdates(NetworkListener);

    }

    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GPS_SWITCH = false; // default switch is off
        isMapReady = false; // default map is not ready unless notified
        isZoomLocal = false; // False until we turn on locationListener
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isListening = false;
        isBound = false;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // Get Last Known Location from GPS before location is received. Used for when app first starts up.
        if (lastKnownLocation == null) {  // attempt to get network last location if gps has no last known location
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        currentFragment = 0; // 0 is null. No fragment currently in fragmentcontainer
        coordsList = new ArrayList<Location>();
        mGPSList = new ArrayList<List<Location>>();
        mNetworkList = new ArrayList<List<Location>>();

        /*
        coordsList.add(new LatLng(37.78105581, -122.27570709));
        coordsList.add(new LatLng(37.78105581, -122.27670709));
        coordsList.add(new LatLng(37.78205581, -122.27670709));
        coordsList.add(new LatLng(37.78305581, -122.27670709));
        coordsList.add(new LatLng(37.78305581, -122.27770709));
        coordsList.add(new LatLng(37.78405581, -122.27870709));

        */

        console = (TextView) findViewById(R.id.console_small);
        consoleString = "onCreate() Called";
        console.setText(consoleString);
        Log.d(TAG, "onCreate() Called");

        Button button = (Button) findViewById(R.id.button);

        /*
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the GPS location provider.

                if (!isMapReady) { // Run onLocationChange(location) only if the map is available
                    return;
                }

                if (!isZoomLocal) {
                    //setZoomLocal(location); // Zoom to current position. used when first map is first initialized
                }

                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        */


        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



        if (!gps_enabled && !network_enabled) {  Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, "nothing is enabled", duration);
            toast.show();

        }




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListening) { // Turn switch on if it is current off and vice versa
                    //locationManager.removeUpdates(locationListener);
                    isListening = false;
                    locationManager.removeUpdates(GPSListener);
                    locationManager.removeUpdates(NetworkListener);
                    consoleString += "\n Button Clicked. Listener Off";
                    console.setText(consoleString);
                    stopService();
                } else {
                    isListening = true; // Redundant boolean as above
                    isAwake = true;
                    // We start listening to location



                    consoleString += "\n Button Clicked. Listener On";
                    console.setText(consoleString);
                    startService();
                    startMapUpdates();

                    /*
                    new Thread(new Runnable() {

                        int scanInterval = 5000; //milliseconds
                        int restInterval = 5000;
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            while (isListening) {
                                try {
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            if (isListening) {
                                                if (gps_enabled)
                                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0,
                                                            GPSListener);

                                                if (network_enabled)
                                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0,
                                                            NetworkListener);
                                            }
                                        }
                                    });
                                    Thread.sleep(scanInterval); // Gives device scanInterval amount of time to scan for coordinates
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            // TODO Auto-generated method stub
                                            if (isListening) {


                                                // Double Check to see if we are still listening. Thread could still be at final iteration AFTER listening is disabled. Double check will guarantee we do not update after we stop listening.
                                                getLastLocation();
                                            }
                                        }
                                    });
                                    Thread.sleep(restInterval); // Waits scanInterval amount of time before next iteration
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            }
                        }
                    }).start();
*/

                }
            }
        });



        // Setting up Action Bar
        //getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("Charmander");


        // Getting height of the screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenHeight = size.y;//parent.getMeasuredHeight();
        int screenWidth = size.x;//parent.getMeasuredWidth();

        // Changing map layout to square layout
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
        frameLayout.getLayoutParams().width = screenWidth;
        frameLayout.getLayoutParams().height = screenWidth;


        /*
        // Adding map to activity
        Fragment mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mMapFragment);
        fragmentTransaction.commit();
        currentFragment = MAP_FRAGMENT;
        */

        //mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mMapFragment = MapFragment.newInstance(); // Initialize instance of map
        mMapFragment.getMapAsync(this);
        mConsoleFragment = new ConsoleFragment();
        getMapFragment();
    }

    public void updateConsole() {
        console.setText(consoleString);
    }

    // Runs the loop to update map
    public void startMapUpdates() {
        Log.d(TAG,"startMapUpdates()");
        new Thread(new Runnable() {

            //int scanInterval = 5000; //milliseconds
            //int restInterval = 5000;
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isListening && isAwake) {
                    try {
                        int timeUntilFlush = (int) mService.getTimeUntilFlush();
                        Log.d(TAG,"Time until Flush: " + String.valueOf(timeUntilFlush));
                        Thread.sleep(timeUntilFlush);

                        // Double Check to see if we are still listening. Thread could still be at final iteration AFTER listening is disabled. Double check will guarantee we do not update after we stop listening.

                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                if (isListening && isAwake) {
                                    List<List<List<Location>>> flushResults = mService.flush();
                                    Log.d(TAG,"Calling Flush()");
                                    List<List<Location>> flushGPSList = flushResults.get(0);
                                    List<List<Location>> flushNetworkList = flushResults.get(1);

                                    Log.d(TAG,"Flushed Size: (" + String.valueOf(flushGPSList.size()) + "," + String.valueOf(flushNetworkList.size()) + ")");
                                    // Plot each new interval into the map. Number of interval is the size of the flushGPSList/flushNetworkList. Each object is an interval of data
                                    for (int i = 0; i < flushGPSList.size(); i++) {
                                        List<Location> curGPSList = flushGPSList.get(i);
                                        plotGPSInterval(curGPSList);
                                    }
                                    for (int j = 0; j < flushNetworkList.size(); j++) {
                                        List<Location> curNetworkList = flushNetworkList.get(j);
                                        plotNetworkInterval(curNetworkList);
                                    }

                                    // Add the flushed results into our list
                                    synchronized (listLock) {
                                /*
                                for (int i = 0; i < flushGPSList.size(); i++) {
                                    Location location = flushGPSList.get(i);
                                    mGPSList.add(location);
                                }


                                for (int i = 0; i < flushNetworkList.size(); i++) {
                                    Location location = flushNetworkList.get(i);
                                    mNetworkList.add(location);
                                }
                                */

                                        mGPSList.addAll(flushGPSList);
                                        mNetworkList.addAll(flushNetworkList);
                                    }

                                } else {
                                    return;
                                }
                            }
                        });


                        /*
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (isListening && isAwake) {
                                    // Double Check to see if we are still listening. Thread could still be at final iteration AFTER listening is disabled. Double check will guarantee we do not update after we stop listening.
                                    getLastLocation();
                                }
                            }
                        });*/
                        //Thread.sleep(restInterval); // Waits scanInterval amount of time before next iteration
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }

    private void plotGPSInterval(List<Location> GPSList) {
        Log.d(TAG, "GPS List size: " + String.valueOf(GPSList.size()));
        if (GPSList.size() > 0) { // If the flushed list has GPS coordinates gathered, then we will process it
            /*
            long gps_time = gps_loc.getElapsedRealtimeNanos();
            long net_time = net_loc.getElapsedRealtimeNanos();
            float gps_acc = gps_loc.getAccuracy();
            float net_acc = net_loc.getAccuracy();
            */

            double avg_gps_lat = 0.0;
            double avg_gps_lng = 0.0;
            double total_gps_lat = 0.0;
            double total_gps_lng = 0.0;
            double total_gps_weight = 0.0;

            for (int i = 0; i < GPSList.size(); i++) {
                // iterate across all the points taken from the GPS in the alloted interval time
                Location curLocation = GPSList.get(i);
                double cur_gps_lat = curLocation.getLatitude();
                double cur_gps_lng = curLocation.getLongitude();

                LatLng curLatLng = new LatLng(cur_gps_lat, cur_gps_lng);
                mMap.addMarker(new MarkerOptions().position(curLatLng).title("GPS Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                Log.d(TAG, "\n Added GPS: (" + cur_gps_lat + "," + cur_gps_lng + ")");

                float cur_accuracy = curLocation.getAccuracy();
                double cur_weight =  getWeightFromAccuracy(cur_accuracy);

                total_gps_lat += cur_gps_lat * cur_weight;
                total_gps_lng += cur_gps_lng * cur_weight;
                total_gps_weight += cur_weight;

            }

            avg_gps_lat = total_gps_lat / total_gps_weight;
            avg_gps_lng = total_gps_lng / total_gps_weight;


            LatLng avgGPS = new LatLng(avg_gps_lat, avg_gps_lng);
            mMap.addMarker(new MarkerOptions().position(avgGPS).title("GPS Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Log.d(TAG, "\nAdding GPS(Average): (" + avg_gps_lat + "," + avg_gps_lng + ")");

        }


    }

    private void plotNetworkInterval(List<Location> NetworkList) {
        Log.d(TAG, "Network List size: " + String.valueOf(NetworkList.size()));
        if (NetworkList.size() > 0) { // If the flushed list has Network coordinates gathered, then we will process it

            /*
            long gps_time = gps_loc.getElapsedRealtimeNanos();
            long net_time = net_loc.getElapsedRealtimeNanos();
            float gps_acc = gps_loc.getAccuracy();
            float net_acc = net_loc.getAccuracy();
            */

            double avg_net_lat = 0.0;
            double avg_net_lng = 0.0;

            double total_net_lat = 0.0;
            double total_net_lng = 0.0;
            double total_net_weight = 0.0;

            for (int i = 0; i < NetworkList.size(); i++) {
                // iterate across all the points taken from the GPS in the alloted interval time
                Location curLocation = NetworkList.get(i);
                double cur_net_lat = curLocation.getLatitude();
                double cur_net_lng = curLocation.getLongitude();

                LatLng curLatLng = new LatLng(cur_net_lat, cur_net_lng);
                mMap.addMarker(new MarkerOptions().position(curLatLng).title("Network Pin"));
                Log.d(TAG, "\nAdding Network: (" + cur_net_lat + "," + cur_net_lng + ")");


                float cur_accuracy = curLocation.getAccuracy();
                double cur_weight =  getWeightFromAccuracy(cur_accuracy);

                total_net_lat += cur_net_lat * cur_weight;
                total_net_lng += cur_net_lng * cur_weight;
                total_net_weight += cur_weight;

            }

            avg_net_lat = total_net_lat / total_net_weight;
            avg_net_lng = total_net_lng / total_net_weight;


            LatLng avgNetwork = new LatLng(avg_net_lat, avg_net_lng);
            mMap.addMarker(new MarkerOptions().position(avgNetwork).title("Network Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            Log.d(TAG, "\nAdding Network(Average): (" + avg_net_lat + "," + avg_net_lng + ")");

        }
    }


    public void setZoomLocation(Location location) {

        /*
        // Construct a CameraPosition focusing on current location and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))      // Sets the center of the map to current location
                .zoom(INITIAL_ZOOM_LEVEL)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        */


        // Start the camera at last known location
        if (lastKnownLocation == null) {
            return; // do nothing if there is no last known location
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), INITIAL_ZOOM_LEVEL));

        // Sets flag to true once we zoom to current location
        isZoomLocal = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_console:
                getConsoleFragment();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_map:
                getMapFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void getMapFragment() {
        if (currentFragment == 0) { // This step happens only if there is no fragment in fragment container
            // Adding map to activity
            //mMapFragment = MapFragment.newInstance();
            //Fragment mapFragment = getFragmentManager().findFragmentById(R.id.map_fragment);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mMapFragment);
            fragmentTransaction.commit();
            currentFragment = MAP_FRAGMENT;
        } else if (currentFragment != MAP_FRAGMENT) { // This step happens if there is currently a fragment but isn't map fragment
            // We replace the fragment with the map fragment

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            fragmentTransaction.replace(R.id.fragment_container, mMapFragment);
            fragmentTransaction.addToBackStack(null);

            // Commit the transaction
            fragmentTransaction.commit();
            currentFragment = MAP_FRAGMENT;

            // Switching the consoleTextView to small
            console = (TextView) findViewById(R.id.console_small);
            console.setText(consoleString); // Updates the text for the small textview
        }
    }

    private void getConsoleFragment() {
        if (currentFragment == 0) { // This step happens only if there is no fragment in fragment container
            // Adding console to activity
            //Fragment consoleFragment = getFragmentManager().findFragmentById(R.id.console_fragment);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, mConsoleFragment);
            fragmentTransaction.commit();
            getFragmentManager().executePendingTransactions(); // forces commit() to happen
            currentFragment = CONSOLE_FRAGMENT;
            console.setText(""); // Clears out the small textview
            console = (TextView) findViewById(R.id.console_large);
            console.setText(consoleString);
        } else if (currentFragment != CONSOLE_FRAGMENT) { // This step happens if there is currently a fragment but isn't console fragment
            // We replace the fragment with the console fragment
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            fragmentTransaction.replace(R.id.fragment_container, mConsoleFragment);
            fragmentTransaction.addToBackStack(null);

            // Commit the transaction
            fragmentTransaction.commit();
            getFragmentManager().executePendingTransactions(); // Forces fragmentTransaction to commit()

            currentFragment = CONSOLE_FRAGMENT;

            // formatting textviews
            console.setText(""); // Clears out the small textview
            console = (TextView) mConsoleFragment.getView().findViewById(R.id.console_large);
            console.setText(consoleString);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        String consoleConcat = "OnRestart() Called";
        if (isListening) { // Turn switch on, resume listener
           // locationManager.requestLocationUpdates(locationProvider, LOCATION_REQUEST_FREQUENCY, 0, locationListener);
            consoleConcat += " Listener Turning On";
        } else {
            consoleConcat += " Listener Remains Off";
        }

        consoleString += consoleConcat;
        console.setText(consoleString);

        Log.d(TAG, consoleConcat);
        isAwake = true;
        startMapUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stops listening to location updates
        //locationManager.removeUpdates(locationListener);
        consoleString += "\n onStop() Called";
        console.setText(consoleString);
        Log.d(TAG, "onStop() Called");
        isAwake = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stops listening to location updates
        //locationManager.removeUpdates(locationListener);

        locationManager.removeUpdates(GPSListener);
        locationManager.removeUpdates(NetworkListener);
        stopService();
        isListening = false; // sets switch to off
        isAwake = false;
        consoleString += "\n onDestroy() Called";
        console.setText(consoleString);
        Log.d(TAG, "onDestroy() Called");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        isMapReady = true;
        mMap = googleMap;
        consoleString += "\n Map is Ready";
        console.setText(consoleString);
        Log.d(TAG, "Map is Ready");

        // Test pin
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(37.7796446, -122.2758692)).title("Test Pin"));

        setZoomLocation(lastKnownLocation);

        for(int i = 0; i < coordsList.size() - 1; i++) {
            Location from = coordsList.get(i);
            Location to = coordsList.get(i+1);


            Polyline line = mMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(from.getLatitude(), from.getLongitude()),
                            new LatLng(to.getLatitude(), to.getLongitude())
                            // from,
                            // to
                            //new LatLng(37.78105581, -122.27570709),
                            //new LatLng(37.78105581, -122.27670709)
                    ).width(20).color(getApplicationContext().getResources().getColor(R.color.deepskyblue)).geodesic(true)
            );
        }

    }


    private double getWeightFromAccuracy(float accuracy) {
        return 1/(Math.pow(accuracy/15,2) + 1);
    }


    // Service functions

    private void startService() {
        Log.d(TAG, "Starting Service");
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void stopService() {
        Log.d(TAG, "Stopping Service");
        if (isBound) {
            Log.d(TAG, "Unbinding Service");
            unbindService(mConnection);
            isBound = false;
        }
        stopService(new Intent(this, LocationService.class));
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            isBound = true;
            Log.d(TAG, "Bounded to Service");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}
