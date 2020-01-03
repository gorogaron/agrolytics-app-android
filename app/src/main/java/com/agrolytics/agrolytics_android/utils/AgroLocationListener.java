package com.agrolytics.agrolytics_android.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.agrolytics.agrolytics_android.ui.main.MainScreen;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public  class AgroLocationListener implements LocationListener {

    private final String TAG = "LocationListener";

    private Context context;
    private MainScreen screen;

    public AgroLocationListener(Context context, MainScreen screen) {
        this.context = context;
        this.screen = screen;
    }

    @Override
    public void onLocationChanged(Location loc) {
//        Toast.makeText(
//                context,
//                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
//                        + loc.getLongitude(), Toast.LENGTH_SHORT).show();

        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;

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

