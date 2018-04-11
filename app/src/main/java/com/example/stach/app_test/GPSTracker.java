package com.example.stach.app_test;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class GPSTracker extends Service {
    private final Context mContext;
    private Location location;
    private LocationManager locationManager;
    private List<GpsChangeListener> listeners;
    private FusedLocationProviderClient mFusedLocationClient;

    // Ogni 3 secondi aggiorna la posizione (si può tranquillamente aumentare il tempo)
    private static final long TEMPO = 1000 * 3;

    public GPSTracker(Context context) {
        this.mContext = context;
        listeners = new ArrayList<>();
        mFusedLocationClient = null;
        location = null;
    }

    // I permessi verranno controllati dalla classe che utilizza il GPSTracker
    @SuppressLint("MissingPermission")
    public void StartToGetLocation() {
        LocationRequest mLocationRequest;

        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            // Se il GPS è attivo
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (location == null) {
                    // Setto il tempo d'intervallo per aggiornare la posizione
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(TEMPO);
                    mLocationRequest.setFastestInterval(TEMPO);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // Precisione gps

                    // Imposto la callback e faccio partire la richiesta di aggiornamenti sulla mia posizione
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    // Prendo l'ultima posizione nota
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        // Chiamo tutti quelli in ascolto su questo evento (tramite il mio GpsChangeListener)
                                        for (GpsChangeListener listener : listeners)
                                            listener.GpsLocationChange(location);
                                    }
                                }
                            });
                }
            }
            else
                location = null;

        } catch (Exception e) {
            location = null;
        }
    }

    // Permetto a chi vuole di mettersi in ascolto sul evento onLocationChanged tramite il GpsChangeListener
    public void addListener(GpsChangeListener x) {
        if (!listeners.contains(x))
            listeners.add(x);
    }

    public void removeListener(GpsChangeListener x) {
                listeners.remove(x);
    }

    // Aggiorno la locazione corrente quando viene rilevato uno spostamento
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            location = locationResult.getLastLocation();

            // (dovrebbe prendere la posizione più precisa se ce ne sono più di una)
            for (Location loc : locationResult.getLocations())
                if (loc.getAccuracy() > location.getAccuracy())
                    location = loc;

            // Chiamo tutti quelli in ascolto su questo evento (tramite il mio GpsChangeListener)
            for (GpsChangeListener listener : listeners)
                listener.GpsLocationChange(location);
        }
    };

    public boolean isGPSon() {
        boolean response = false;

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (locationManager != null)
            response = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return response;
    }

    public void StopGPS() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            location = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
