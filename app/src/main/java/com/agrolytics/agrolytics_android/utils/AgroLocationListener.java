package com.agrolytics.agrolytics_android.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.agrolytics.agrolytics_android.ui.main.MainScreen;

public class AgroLocationListener implements LocationListener {

    private final String TAG = "LocationListener";

    private Context context;
    private MainScreen screen;

    public AgroLocationListener(Context context, MainScreen screen) {
        this.context = context;
        this.screen = screen;
    }

    @Override
    public void onLocationChanged(Location loc) {
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);

        Util.Companion.setLat(loc.getLatitude());
        Util.Companion.setLong(loc.getLongitude());
        screen.locationUpdated();
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}

