/*
* Nazim A.Belabbaci
* check out the rest of my work here:https://github.com/NazimBL
* */
package com.example.mymapapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location location,prev_location;
    private static ArrayList<Location> locationTrack = new ArrayList<Location>();
    // the listener that checks the location on changes.
    private CurrentLocationListener currentLocationListener;
    // manages the location services.
    private LocationManager locationManager;
    private TextView location_text,speedText,speedAvgText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();
        //if this check fails, the app will not function.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location_text.setText("Permission to gps not granted. Grant permissions and try again or restart the application.");
            alertbox();
        }
        else {
            location = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE);
            prev_location=location;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, currentLocationListener);
        //run a task every 10s
        //using this method to run the app even in background
        Thread t = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(10000);  //1000ms = 1 sec
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            //this will run every 10 seconds, gets current location, update map, calculate the distance/speed and averaging the values stored
                                try {
                                    updateLocation();
                                    updateMap();
                                    if(prev_location==null) prev_location=location;
                                    //get speed ,distance and average speed
                                    speedometer();

                                    prev_location=location;
                                }catch (Exception e){
                                    Log.d("Nazim", "exception:\n"+e.toString());
                                    Toast.makeText(MapsActivity.this,"Location unknown. Try again or restart the application",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.start();
    }

//initialisation method
    void init(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Constructs the listener object.
        currentLocationListener = new CurrentLocationListener();
        location_text = findViewById(R.id.displayText_id);
        speedText=findViewById(R.id.speedText_id);
        speedAvgText=findViewById(R.id.speedAvg_id);
        //Constructs the location manager object.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    // asks for gps permission.
    protected void alertbox() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private void speedometer(){
        double speed,distance,avgspeed;
        float convert_num;

        speed=(prev_location.distanceTo(location))/10;
        //convert speed to show only t digits after decimal
        convert_num = (float) Math.round(speed * 100) / 100;
        speedText.setText("Current Speed: "+convert_num+"m/s");

        //store the current location to calcualte speed since the app started
        locationTrack.add(location);
        //average speed/distance
        distance=averageDistance();
        convert_num = (float) Math.round(distance * 100) / 100;
        location_text.setText("Distance traveled:"+convert_num+" m");

        avgspeed=(distance/(locationTrack.size()-1))/10;
        convert_num = (float) Math.round(avgspeed * 100) / 100;
        speedAvgText.setText("Average Speed: "+convert_num+"m/s");
    }

    public static double LatLngToDistance(Location point1,Location point2) {
        double degToRad = Math.PI / 180;
        long R = 6371000;
        double angle = Math.acos(Math.sin(point1.getLongitude()) * Math.sin(point2.getLatitude())
                + Math.cos(point1.getLatitude()) * Math.cos(point2.getLatitude()) * Math.cos(point1.getLongitude() - point2.getLongitude()));
        return R * degToRad * angle;
    }

    public static double averageDistance(){
        double distancesSum = 0;

        for(int i = 0;i<locationTrack.size()-1;i++) {
            distancesSum += locationTrack.get(i).distanceTo(locationTrack.get(i+1));
        }
        return distancesSum;
    }

    void updateLocation() {
        //check permissions. If grantedm get last known location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location_text.setText("Permission to gps not granted. Grant permissions and try again or restart the application.");
            alertbox();
            return;
        } else {
            location = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, currentLocationListener);
           if (location == null) {
            location = currentLocationListener.getLocation();
            location_text.setText("Latitude: " + location.getLatitude() + ",\nLongitude: " + location.getLongitude());
            Log.d("Nazim","Latitude: " + location.getLatitude() + ",\nLongitude: " + location.getLongitude());
        }else {
            location_text.setText("Location unknown. Try again or restart the application.");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney;
        // Add a marker in Sydney and move the camera
        if(location!=null) sydney = new LatLng(location.getLatitude(), location.getLongitude());
        else sydney = new LatLng(43,-71);
        //replace with geothing api
        mMap.addMarker(new MarkerOptions().position(sydney).title("Im Here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    void updateMap(){
        LatLng sydney;
        // Add a marker in Sydney and move the camera
        if(location!=null) sydney = new LatLng(location.getLatitude(), location.getLongitude());
        else sydney = new LatLng(43,-71);
        //replace with geothing api
        mMap.addMarker(new MarkerOptions().position(sydney).title(getAddress(location.getLatitude(), location.getLongitude())));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public String getAddress(double lat,double lng){
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location
            //result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0); // If any additional address line
        //present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        return address+" "+city+" "+state+" "+" "+country+" "+ postalCode + " " +knownName+" ";
    }
}

