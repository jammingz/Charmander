package com.example.jamin.charmander;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // the location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    // the dreaded wakelock
    PowerManager.WakeLock m_wakeLock;

    boolean gps_enabled = false;
    boolean network_enabled = false;

    private boolean isListening;
    private final Handler mHandler = new Handler();

    private long timeUntilNextInterval; // Keeps the time when the current interval ends

    private static final int GPS_BUFFER = 0;
    private static final int NETWORK_BUFFER = 1;
    private static final int SCAN_INTERVAL = 9000; // 9 seconds
    private static final int REST_INTERVAL = 1000; // 1 second


    private static List<List<Location>> mGPSList; // ArrayList of ArrayList of locations. Each arraylist inside mGPSList represent a scan interval of coordinates
    private static List<List<Location>> mNetworkList;
    private static List<Location> mGPSBufferList;
    private static List<Location> mNetworkBufferList;
    private static final Object listLock = new Object();


    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }


    LocationListener GPSListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //timer.cancel();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(NetworkListener);

            LatLng curLatLng = new LatLng(lat, lng);


            /*
            // Test pin
            mMap.addMarker(new MarkerOptions().position(curLatLng).title("GPS Pin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            mGPSList.add(location);
            consoleString += "\nAdding GPS: (" + lat + "," + lng + ")";
            console.setText(consoleString);

            //coordsList.add(curLatLng);
            */
            Log.d(TAG, "Got a new location from the GPSListener [" + location.getLatitude()
                    + ", " + location.getLongitude() + "]");

            synchronized (listLock) {
                mGPSBufferList.add(location);
            }

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


            LatLng curLatLng = new LatLng(lat, lng);

            Log.d(TAG, "Got a new location from the NetworkListener [" + location.getLatitude()
                    + ", " + location.getLongitude() + "]");
            /*

            // Test pin
            mMap.addMarker(new MarkerOptions().position(curLatLng).title("Network Pin"));
            mNetworkList.add(location);

            consoleString += "\nAdding Network: (" + lat + "," + lng + ")";
            console.setText(consoleString);

            */
            synchronized (listLock) {
                mNetworkBufferList.add(location);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public void onCreate() {
        // get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "String Wake Lock");

        mGPSList = new ArrayList<List<Location>>(); // resets list
        mNetworkList = new ArrayList<List<Location>>(); // resets list
        mGPSBufferList = new ArrayList<Location>();
        mNetworkBufferList = new ArrayList<Location>();
        isListening = false; // default is false until we start service
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting the LocationService");

        // acquire the wakelock
        Log.d(TAG, "Acquiring the wake lock");
        m_wakeLock.acquire();

        // Running listeners
        isListening = true;

        // Run listen loop
        run();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "Stopping the LocationServices");

        // if the wakelock is held
        if (m_wakeLock.isHeld()) {

            // release it
            Log.d(TAG, "Releasing the wake lock");
            m_wakeLock.release();
        }

        isListening = false;

        // remove the listener
        if (gps_enabled) {
            locationManager.removeUpdates(GPSListener);
        }

        if (network_enabled) {
            locationManager.removeUpdates(NetworkListener);
        }

    }

    // returns the accumlated gps/network lists and empties both lists
    public List<List<List<Location>>> flush() {
        List<List<List<Location>>> results = new ArrayList<List<List<Location>>>(2); // ArrayList of size (2) containing both GPSList and NetworkList
        // Lock the lists so we dont add to the list and flush at the same time
        synchronized (listLock) {
            results.add(mGPSList);
            results.add(mNetworkList);
            mGPSList = new ArrayList<List<Location>>(); // empties list
            mNetworkList = new ArrayList<List<Location>>(); // empties list
        }

        Log.d(TAG, "Flush() Called");

        return results;
    }

    // Returns results in milliseconds
    public long getTimeUntilFlush() {
        return timeUntilNextInterval - SystemClock.elapsedRealtime();
    }

    public void flushBuffer(int buffer) {
        switch (buffer) {
            case GPS_BUFFER:
                synchronized (listLock) {
                    mGPSList.add(mGPSBufferList); // appends the GPS buffer arraylist into the GPS arraylist. GPS arraylist will not be empty unless we flush the GPS arraylist
                    mGPSBufferList = new ArrayList<Location>(); // now we empty out GPS buffer list
                }
                break;
            case NETWORK_BUFFER:
                synchronized (listLock) {
                    mNetworkList.add(mNetworkBufferList); // appends the Network buffer arraylist into the Network arraylist. Network arraylist will not be empty unless we flush the Network arraylist
                    mNetworkBufferList = new ArrayList<Location>(); // now we empty out Network buffer list
                }
                break;
        }
    }

    public void run() {
        new Thread(new Runnable() {
            // 9 seconds to scan
            // 1 seconds until next scan. Interval totals 10 seconds
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isListening) {
                    try {
                        timeUntilNextInterval = SystemClock.elapsedRealtime() + SCAN_INTERVAL; // Time until the end of interval

                        // Scanning Now
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (isListening) {
                                    if (gps_enabled) {
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0,
                                                GPSListener);
                                    }

                                    if (network_enabled) {
                                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0,
                                                NetworkListener);
                                    }
                                }
                            }
                        });


                        Thread.sleep(SCAN_INTERVAL); // Gives device scanInterval amount of time to scan for coordinates

                        // Stopping scan
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (isListening) {
                                    if (gps_enabled) {
                                        locationManager.removeUpdates(GPSListener);
                                    }

                                    if (network_enabled) {
                                        locationManager.removeUpdates(NetworkListener);
                                    }
                                }
                            }
                        });

                        timeUntilNextInterval = SystemClock.elapsedRealtime() + (SCAN_INTERVAL + REST_INTERVAL); // New Interval. We sync/reset the timeUntilNextInterval to 5 seconds
                        // Flush the bufferLists into the Gps/Network list
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (isListening) {
                                    if (gps_enabled) {
                                        flushBuffer(GPS_BUFFER);
                                    }

                                    if (network_enabled) {
                                        flushBuffer(NETWORK_BUFFER);
                                    }
                                }
                            }
                        });

                        Thread.sleep(REST_INTERVAL); // Waits restInterval amount of time before next iteration
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }



}
