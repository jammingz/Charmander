package com.example.jamin.charmander;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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
    private List<LatLng> coordsList;

    // Constants
    private static final int LOCATION_REQUEST_FREQUENCY = 5000;
    private static final int INITIAL_ZOOM_LEVEL = 15;

    Timer timer;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private final Handler mHandler = new Handler();
    private boolean isListening;


    // Defining the GPS Location Listener

    LocationListener GPSListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //timer.cancel();
            double x =location.getLatitude();
            double y = location.getLongitude();
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(NetworkListener);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, "gps enabled "+x + "\n" + y, duration).show();
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
            double x = location.getLatitude();
            double y = location.getLongitude();
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(GPSListener);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, "network enabled"+x + "\n" + y, duration).show();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            double x = 0.0;
            double y = 0.0;

            //locationManager.removeUpdates(GPSListener);
            //locationManager.removeUpdates(NetworkListener);

            Location net_loc=null, gps_loc=null;
            if(gps_enabled)
                gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(network_enabled)
                net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if(gps_loc != null && net_loc != null){
                if(gps_loc.getTime()>net_loc.getTime())  {
                    x = gps_loc.getLatitude();
                    y = gps_loc.getLongitude();
                    consoleString += "\ngps lastknown (first) "+x + "\n" + y;
                    x = net_loc.getLatitude();
                    y = net_loc.getLongitude();
                    consoleString += "network lastknown (second)"+x + "\n" + y;
                } else  {
                    x = net_loc.getLatitude();
                    y = net_loc.getLongitude();
                    consoleString += "network lastknown (first)"+x + "\n" + y;
                    x = gps_loc.getLatitude();
                    y = gps_loc.getLongitude();
                    consoleString += "\ngps lastknown (second) "+x + "\n" + y;
                }
            } else if(gps_loc != null) {
                // only gps location is available
                x = gps_loc.getLatitude();
                y = gps_loc.getLongitude();
                consoleString += "\ngps lastknown "+x + "\n" + y;
            } else  if (net_loc != null){
                // only network location is available
                x = net_loc.getLatitude();
                y = net_loc.getLongitude();
                consoleString += "\nnetwork lastknown "+x + "\n" + y;
            } else {
                // both network and gps locations are unavailable
                consoleString += "\nno last know avilable";
            }

            updateConsole();
        }
    }


    private void getLastLocation() {
        double x = 0.0;
        double y = 0.0;

        //locationManager.removeUpdates(GPSListener);
        //locationManager.removeUpdates(NetworkListener);

        Location net_loc=null, gps_loc=null;
        if(gps_enabled)
            gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(network_enabled)
            net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //if there are both values use the latest one
        if(gps_loc != null && net_loc != null){
            if(gps_loc.getTime()>net_loc.getTime())  {
                x = gps_loc.getLatitude();
                y = gps_loc.getLongitude();
                consoleString += "\ngps lastknown (first): ("+ x + "," + y +")";
                x = net_loc.getLatitude();
                y = net_loc.getLongitude();
                consoleString += "\nnetwork lastknown (second): ("+ x + "," + y +")";
            } else  {
                x = net_loc.getLatitude();
                y = net_loc.getLongitude();
                consoleString += "\nnetwork lastknown (first): ("+ x + "," + y +")";
                x = gps_loc.getLatitude();
                y = gps_loc.getLongitude();
                consoleString += "\ngps lastknown (second): ("+ x + "," + y +")";
            }
        } else if(gps_loc != null) {
            // only gps location is available
            x = gps_loc.getLatitude();
            y = gps_loc.getLongitude();
            consoleString += "\ngps lastknown: ("+ x + "," + y +")";
        } else  if (net_loc != null){
            // only network location is available
            x = net_loc.getLatitude();
            y = net_loc.getLongitude();
            consoleString += "\nnetwork lastknown: ("+ x + "," + y +")";
        } else {
            // both network and gps locations are unavailable
            consoleString += "\nno last know avilable";
        }

        console.setText(consoleString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GPS_SWITCH = false; // default switch is off
        isMapReady = false; // default map is not ready unless notified
        isZoomLocal = false; // False until we turn on locationListener
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isListening = false;
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // Get Last Known Location from networks before location is received. Used for when app first starts up.
        currentFragment = 0; // 0 is null. No fragment currently in fragmentcontainer
        coordsList = new ArrayList<LatLng>();

        coordsList.add(new LatLng(37.78105581, -122.27570709));
        coordsList.add(new LatLng(37.78105581, -122.27670709));
        coordsList.add(new LatLng(37.78205581, -122.27670709));
        coordsList.add(new LatLng(37.78305581, -122.27670709));
        coordsList.add(new LatLng(37.78305581, -122.27770709));
        coordsList.add(new LatLng(37.78405581, -122.27870709));

        console = (TextView) findViewById(R.id.console_small);
        consoleString = "onCreate() Called";
        console.setText(consoleString);

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
                if (GPS_SWITCH) { // Turn switch on if it is current off and vice versa
                    GPS_SWITCH = false;
                    isListening = false;
                    //locationManager.removeUpdates(locationListener);
                    locationManager.removeUpdates(GPSListener);
                    locationManager.removeUpdates(NetworkListener);
                    consoleString += "\n Button Clicked. Listener Off";
                    console.setText(consoleString);
                } else {
                    GPS_SWITCH = true;
                    isListening = true; // Redundant boolean as above
                    // We start listening to location
                    //

                    if (gps_enabled)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0,
                                GPSListener);
                    if (network_enabled)
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0,
                                NetworkListener);


                    //timer=new Timer();
                    //timer.schedule(new GetLastLocation(), 10000);

                    consoleString += "\n Button Clicked. Listener On";
                    console.setText(consoleString);
                    new Thread(new Runnable() {

                        int delay = 10000; //milliseconds
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            while (isListening) {
                                try {
                                    Thread.sleep(delay);
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
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            }
                        }
                    }).start();

                    /*
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            //do something
                            getLastLocation();
                        }
                    }, 10000);
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


    public void setZoomLocal(Location location) {

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), INITIAL_ZOOM_LEVEL));

        // Sets flag to true once we zoom to current location
        isZoomLocal = true;
    }

    public void makeUseOfNewLocation(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        consoleString += "\n Location Updated: (" + String.valueOf(lat) + "," + String.valueOf(lng) + ")";
        console.setText(consoleString);

        LatLng curLatLng = new LatLng(lat, lng);

        // Test pin
        mMap.addMarker(new MarkerOptions().position(curLatLng).title("Test Pin"));
        mMap.addMarker(new MarkerOptions().position(curLatLng).title("Network Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        coordsList.add(curLatLng);
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
        String consoleConcat = "\nOnRestart() Called";
        if (GPS_SWITCH) { // Turn switch on, resume listener
            locationManager.requestLocationUpdates(locationProvider, LOCATION_REQUEST_FREQUENCY, 0, locationListener);
            consoleConcat += " Listener Turning On";
        } else {
            consoleConcat += " Listener Remains Off";
        }

        consoleString += consoleConcat;
        console.setText(consoleString);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stops listening to location updates
        //locationManager.removeUpdates(locationListener);
        consoleString += "\n onStop() Called";
        console.setText(consoleString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stops listening to location updates
        //locationManager.removeUpdates(locationListener);

        locationManager.removeUpdates(GPSListener);
        locationManager.removeUpdates(NetworkListener);
        GPS_SWITCH = false; // sets switch to off
        consoleString += "\n onDestroy() Called";
        console.setText(consoleString);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        isMapReady = true;
        mMap = googleMap;
        consoleString += "\n Map is Ready";
        console.setText(consoleString);

        // Test pin
        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Test Pin"));

        //setZoomLocal(lastKnownLocation);

        for(int i = 0; i < coordsList.size() - 1; i++) {
            LatLng from = coordsList.get(i);
            LatLng to = coordsList.get(i+1);


            Polyline line = mMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(from.latitude, from.longitude),
                            new LatLng(to.latitude, to.longitude)
                            // from,
                            // to
                            //new LatLng(37.78105581, -122.27570709),
                            //new LatLng(37.78105581, -122.27670709)
                    ).width(20).color(getApplicationContext().getResources().getColor(R.color.deepskyblue)).geodesic(true)
            );
        }

    }
}
