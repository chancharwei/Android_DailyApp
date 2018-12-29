package com.example.chancharwei.dailyapp.gps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class GPSLocation implements LocationListener{
    private static final String TAG = GPSLocation.class.getName();
    private GPSLocationListener mGpsLocationListener;


    public GPSLocation(GPSLocationListener gpsLocationListener){
        this.mGpsLocationListener = gpsLocationListener;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"check onLocationChanged "+(location==null?"true":"false")+" provider = "+location.getProvider());
        if(location != null){
            mGpsLocationListener.updateLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        mGpsLocationListener.updateStatus(provider,status,bundle);
        switch (status){
            case LocationProvider.AVAILABLE:
                Log.d(TAG,"get Available");
                mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_AVAILABLE);
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d(TAG,"get out of service");
                mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_OUT_OF_SERVICE);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d(TAG,"get temp unavailable");
                mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_TEMPORARILY_UNAVAILABLE);
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d(TAG," get gps enable provider = "+provider);
        mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_ENABLED);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG," get gps disable provider = "+provider);
        mGpsLocationListener.updateGPSProviderStatus(GPSProviderStatus.GPS_DISABLED);
    }

    public class GPSProviderStatus{
        public static final int GPS_ENABLED = 0;
        public static final int GPS_DISABLED = 1;
        public static final int GPS_OUT_OF_SERVICE = 2;
        public static final int GPS_TEMPORARILY_UNAVAILABLE = 3;
        public static final int GPS_AVAILABLE = 4;
    }
}


