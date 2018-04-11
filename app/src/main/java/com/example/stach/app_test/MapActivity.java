package com.example.stach.app_test;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.*;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMyLocationButtonClickListener, OnMapReadyCallback, OnMarkerClickListener {
    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private LocationManager lmanager;
    private Marker mCurrLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient;

    private List<Parcheggio> parcheggi;
    private Marker scelta = null;

    public MapActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFrag;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        lmanager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        this.parcheggi = new ArrayList<>();

        // Prelevo i dati passati tramite intent
        Intent intent = getIntent();
        List<String> parcheggi = intent.getStringArrayListExtra("parcheggi");

        // Estraggo tutti i parcheggi
        try {
            for (String obj : parcheggi)
                this.parcheggi.add(new Parcheggio(obj));
        } catch (Exception e) {
            finish();
        }

        // Salvo i parcheggi in Parametri
        if (!this.parcheggi.isEmpty())
            Parametri.parcheggi = this.parcheggi;

        Button ButtonCerca = findViewById(R.id.buttonPosition);
        ButtonCerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonSearchClick();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fermo l'aggiornamento della posizione
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

     @Override public void onResume() {
         super.onResume();
         if (mFusedLocationClient != null)
             if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                 // Controllo che il GPS sia acceso
                 if (!lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                     checkGPS();
                 else {
                     mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                     mGoogleMap.setMyLocationEnabled(true);
                 }
             } else {
                 checkLocationPermission();
             }
     }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fermo l'aggiornamento della posizione
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mLocationRequest = new LocationRequest();
        // Un minuto di intervallo (ogni minuto prende la posizione corrente in automatico)
        mLocationRequest.setInterval(1000 * 60 * 1);
        mLocationRequest.setFastestInterval(1000 * 60 * 1);
        // Precisione gps (consuma troppa batteria ma probabilmente non è inerente alle specifiche del nostro progetto)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);

        // Metto tutti i parcheggi nella mappa come Marker blu
        for (Parcheggio p : parcheggi) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(p.getCoordinate());
            markerOptions.title(p.getIndirizzo());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mGoogleMap.addMarker(markerOptions);
        }

        // Sposto la telecamera della mappa sul centro italia
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.056696, 12.567339), 7));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Controllo che il GPS sia acceso
            if (lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
                checkGPS();
            else {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            checkLocationPermission();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                // Creo un maker della mia posizione corrente
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("La tua posizione");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                Parametri.lastKnowPosition = location;
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    // Richiedo i permessi per accedere alla posizione
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Attiva i permessi per il GPS")
                        .setMessage("Per cercare un parcheggio è necessario autorizzare quest' applicaizone all' accesso della posizione corrente.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        // Se l'utente preme indietro baro in stile stacchio
                        //startActivity(new Intent(getApplicationContext(), MapActivity.class));
                        //finish();
                    }
                }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permessi garantiti
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Controllo che il GPS sia acceso
                        if (lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                            mGoogleMap.setMyLocationEnabled(true);
                        }
                    }
                } else {
                    // Permessi negati, disabilitare qui le funzioni per il gps
                    Toast.makeText(this, "Permessi negati.\nNon puoi utilizzare correttamente quest applicazione", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Serve a distinguere i contorlli di varie azioni (in questo caso ne abbiamo solo una per l'attivazione del gps)
    private final int ACTION_LOCATION_SETTING = 100;

    // Controlla se il GPS è accesso
    private void checkGPS() {
        if (!lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this)
                    .setTitle("Devi attivare il GPS")
                    .setMessage("Per cercare un parcheggio è necessario attivare la localizzazione automatica.")
                    .setPositiveButton("Attiva GPS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent locationSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(locationSettingIntent, ACTION_LOCATION_SETTING);
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.cancel();
                    // Se l'utente preme indietro baro in stile stacchio
                    //startActivity(new Intent(getApplicationContext(), MapActivity.class));
                    //finish();
                }
            }).create().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_LOCATION_SETTING:
                if (!lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, "GPS spento.\nNon puoi utilizzare correttamente quest applicazione.", Toast.LENGTH_LONG).show();
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    } else
                        checkLocationPermission();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Click sul bottone in alto a destra per visualizzare la tua posizione
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getApplicationContext(), "Tu sei qua.", Toast.LENGTH_SHORT).show();
        return false;
    }

    // Click su un marker qualsiasi presente nella mappa (i marker sono i palloncini colorati che indicano posizioni specifiche sulla mappa)
    @Override
    public boolean onMarkerClick(final Marker marker) {
        scelta = marker;

        Toast.makeText(getApplicationContext(), "Hai scelto " + marker.getTitle(), Toast.LENGTH_LONG).show();
        // Sposto la visuale e faccio lo zoom sulla posizione cliccata
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(scelta.getPosition(), 10));

        return false;
    }

    public void ButtonSearchClick() {
        if (scelta != null) {
            if (mCurrLocationMarker != null) {
                if (!mCurrLocationMarker.equals(scelta)) { // L'utente ha scelto un parcheggio
                    new AlertDialog.Builder(this)
                            .setTitle("Selezione parcheggio")
                            .setMessage(scelta.getTitle() + ".\n\nVuoi cercare posto in questo parcheggio ?")
                            .setPositiveButton("Cerca posto",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Ricerco il parcheggio selezionato dall' utente
                                            for (Parcheggio p : Parametri.parcheggi)
                                                if (p.getIndirizzo().compareTo(scelta.getTitle()) == 0)
                                                    TerminateAndResponseParkId(p.getId());
                                        }
                                    }).setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.cancel();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    }).create().show();
                } else { // L'utente ha scelto la sua posizione attuale
                    new AlertDialog.Builder(this)
                            .setTitle("Selezione parcheggio")
                            .setMessage("Vuoi cercare i parcheggi più vicini alla tua posizione in automatico ?")
                            .setPositiveButton("Cerca",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            TerminateAndResponseMyCoordinate();
                                        }
                                    }).setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.cancel();
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    }).create().show();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Devi prima attivare il GPS.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Devi prima selezionare un parcheggio.", Toast.LENGTH_SHORT).show();
    }

    private void TerminateAndResponseMyCoordinate (){
        Intent intent = new Intent();
        intent.putExtra("selezioneParcheggio", "false");
        setResult(RESULT_OK, intent);
        finish();
    }

    private void TerminateAndResponseParkId (int id){
        Intent intent = new Intent();
        intent.putExtra("selezioneParcheggio", "true");
        intent.putExtra("id", String.valueOf(id));
        setResult(RESULT_OK, intent);
        finish();
    }
}