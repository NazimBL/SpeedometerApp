package com.example.mymapapp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class CurrentLocationListener implements LocationListener {
    Location myLocation;

    public Location getLocation(){
        return myLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        Log.d("Nazim", String.valueOf(location.getLatitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
