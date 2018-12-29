package com.example.chancharwei.dailyapp.gps;

import android.location.Location;
import android.os.Bundle;

public interface GPSLocationListener {

    void updateLocation(Location location);

    void updateStatus(String provider, int status, Bundle extras);

    void updateGPSProviderStatus(int gpsStatus);
}
