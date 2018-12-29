package com.example.chancharwei.dailyapp.gps;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.chancharwei.dailyapp.WeatherActivity;

import java.lang.ref.WeakReference;
import java.util.List;

public class GPSLocationManager {
    private static final String TAG = GPSLocationManager.class.getName();
    private WeakReference<Activity> mContext;
    //private static Object objLock = new Object();
    private LocationManager locationManager;
    private static GPSLocationManager gpsLocationManager;
    private GPSLocation mGPSLocation;
    private static String mLocateType;
    private boolean isOPenGps;
    private long mMinTime;
    private float mMinDistance;
    Activity mActivity;

    //TODO private?
    public GPSLocationManager(Activity context) {
        initData(context);
    }

    private void initData(Activity context) {
        this.mContext = new WeakReference<>(context);

        if (mContext.get() != null) {
            locationManager = (LocationManager) (mContext.get().getSystemService(context.LOCATION_SERVICE));
        }
        mLocateType = locationManager.NETWORK_PROVIDER;
        //
        //mLocateType = locationManager.GPS_PROVIDER;
        isOPenGps = false;
        mMinTime = 1000;
        mMinDistance = 0;
    }
    /*
    private static GPSLocationManager getInstances(Activity context){
        if(gpsLocationManager == null){
            synchronized (objLock){
                if(gpsLocationManager == null){
                    gpsLocationManager = new GPSLocationManager(context);
                }
            }
        }
        return gpsLocationManager;
    }
*/


    public boolean startGPS(GPSLocationListener gpsLocationListener) {
        Log.d(TAG,"startGPS E");
        if (mContext.get() == null) {
            return false;
        }

        mGPSLocation = new GPSLocation(gpsLocationListener);

        Log.d(TAG,"check location permission FINE = "+ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_FINE_LOCATION)+" COARSE = "+ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_COARSE_LOCATION));
        if (ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }


        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER,60*1000,1000,mGPSLocation);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,60*1000,1000,mGPSLocation);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        if(bestLocation == null){
            Log.d(TAG,"get bestLocation is null");
        }else{
            mGPSLocation.onLocationChanged(bestLocation);
        }

        Log.d(TAG,"startGPS X");
        return true;
    }

    public void openGPS(){
        if(Build.VERSION.SDK_INT > 15){
            Intent  intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.get().startActivityForResult(intent, 0);
        }
    }

    public boolean stop(){
        if(mContext.get()!=null){
            if (ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return true; //not stop success
            }
            if(locationManager!=null){
                if(mGPSLocation == null){
                    Log.d(TAG,"Byron check is null");
                    return true;
                }
                Log.d(TAG,"Byron check is not null");
                locationManager.removeUpdates(mGPSLocation);
                return false;
            }
        }
        return true;
    }
}
