package com.example.stach.app_test;

import android.location.Location;

// Interfaccia per gestire eventi tra il GPSTracker e i fragment
public interface GpsChangeListener {
    void GpsLocationChange(Location location);
}
