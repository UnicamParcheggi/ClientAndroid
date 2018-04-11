package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FindYourParkingFragment extends Fragment implements GpsChangeListener {
    // Locaizone corrente
    private Location curLocation = null;
    // GPSTracker personalizzato
    private GPSTracker gpsTracker = null;
    // View corrente
    private View view;

    private ProgressDialog caricamento = null;

    // Serve a distinguere i contorlli di varie Activity
    private final int ACTION_LOCATION_SETTING = 100;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 4;
    private final int ACTION_MAP = 50;

    public FindYourParkingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_find_your_parking, container, false);

        ConstraintLayout automaticSearch = view.findViewById(R.id.CercaParcheggiVicini);
        ConstraintLayout inputSearch = view.findViewById(R.id.RicercaParcheggiManuale);

        // Ricerca manuale tra tutti i parcheggi presenti nel database (recuperati dal server ovviamente)
        inputSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LanciaMappe();
            }
        });

        // Ricerca automatica della posizione corrente
        automaticSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    checkLocationPermission();
                else if (gpsTracker.isGPSon()) {
                    SendCoordinateForNearPark();
                } else
                    checkGPS();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker.removeListener(this);
        gpsTracker.StopGPS();
    }

    // Richiedo i permessi per accedere alla posizione
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Attiva i permessi per il GPS")
                        .setMessage("Per cercare un parcheggio è necessario autorizzare quest' applicaizone all' accesso della posizione corrente.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                        // Se l'utente preme indietro baro in stile stacchio
                        /**
                         * startActivity(new Intent(getContext(), MainActivity.class));
                         * getActivity().finish();
                         */
                    }
                }).create().show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permessi garantiti
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Controllo che il GPS sia acceso
                        if (gpsTracker.isGPSon())
                            gpsTracker.StartToGetLocation();
                    }
                } else {
                    // Permessi negati, disabilitare qui le funzioni per il gps
                    Toast.makeText(getContext(), "Permessi negati.\nNon puoi utilizzare correttamente quest applicazione", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Controlla se il GPS è accesso
    private void checkGPS() {
        if (!gpsTracker.isGPSon()) {
            new AlertDialog.Builder(getContext())
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
                    /**
                     * startActivity(new Intent(getContext(), MainActivity.class));
                     * getActivity().finish();
                     */
                }
            }).create().show();
        } else
            gpsTracker.StartToGetLocation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_LOCATION_SETTING:
                if (!gpsTracker.isGPSon()) {
                    Toast.makeText(getContext(), "GPS spento.\nNon puoi utilizzare correttamente quest applicazione", Toast.LENGTH_LONG).show();
                } else
                    gpsTracker.StartToGetLocation();
                break;
            case ACTION_MAP:
                if (resultCode == RESULT_OK) {
                    String seleziona = data.getStringExtra("selezioneParcheggio");

                    if (seleziona.compareTo("false") == 0) { // Mie coordinate
                        SendCoordinateForNearPark();
                    } else if (seleziona.compareTo("true") == 0) { // Parcheggio selezionato
                        int id = Integer.parseInt(data.getStringExtra("id"));
                        CallFragmentPrenotaParcheggio(id);
                    } else
                        Toast.makeText(getContext(), "Risultato di mappe sconosciuto.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void AggiornaIndirizzo() {
        TextView t_indirizzo = view.findViewById(R.id.indirizzo);
        // Ricavo la via dalle coordinate con il Codificatore
        try {
            CodificatoreIndirizzi cod = new CodificatoreIndirizzi(getContext());
            t_indirizzo.setText(cod.getIndirizzoFromLocation(curLocation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LanciaMappe() {
        // Avverto l'utente del tentativo di ricezione dei dati per i parcheggi
        caricamento = ProgressDialog.show(getContext(), "Recupero dati parcheggi", "Connessione con il server in corso...", true);

        JSONObject postData = new JSONObject();
        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(ListenerLanciaMappe);
        conn.execute(Parametri.IP + "/getAllParcheggi");
    }

    @Override
    public void GpsLocationChange(Location location) {
        curLocation = location;
        if (curLocation != null) {
            Parametri.lastKnowPosition = location;
            AggiornaIndirizzo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsTracker.removeListener(this);
        gpsTracker.StopGPS();
    }


     @Override public void onResume() {
         super.onResume();

         if (gpsTracker == null) {
             gpsTracker = new GPSTracker(getContext());
             gpsTracker.addListener(this);
         }
         else
             gpsTracker.addListener(this);

         if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
             checkLocationPermission();
         else if (gpsTracker.isGPSon())
             gpsTracker.StartToGetLocation();
         else
             checkGPS();
     }

    // Listener con funzione per la ricezione dei risultati della Connessione con il server (per avere la lista dei parcheggi nella mappa)
    private ConnessioneListener ListenerLanciaMappe = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                caricamento.dismiss();
                Toast.makeText(getContext(), "ERRORE:\nConnessione Assente o server offline.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                caricamento.dismiss();
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                // Estraggo i dati dei parcheggi restituiti dal server
                ArrayList<String> par = new ArrayList<>();
                try {
                    JSONObject allparcheggi = new JSONObject(result);
                    JSONArray parcheggi = allparcheggi.getJSONArray("parcheggi");

                    for (int i = 0; i < parcheggi.length(); i++)
                        par.add(parcheggi.getJSONObject(i).toString());

                } catch (Exception e) {
                    caricamento.dismiss();
                    Toast.makeText(getContext(), "Errore di risposta del server.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Lascio estrarre la lista dei parcheggi alla MapActivity (si potrebbe pure fare qua senza passarglierli, per ora lascio così)
                caricamento.dismiss();

                // Invio i dati tramite intent
                Intent intent = new Intent(getContext(), MapActivity.class);
                intent.putStringArrayListExtra("parcheggi", par);
                startActivityForResult(intent, ACTION_MAP);
            }
        }
    };

    private void SendCoordinateForNearPark() {
        if (curLocation == null)
            return;

        // Avverto l'utente del tentativo di ricezione dei dati per i parcheggi
        caricamento = ProgressDialog.show(getContext(), "Recupero dati parcheggi",
                "Ricerca Parcheggi vicini in corso...", true);

        JSONObject postData = new JSONObject();
        try {
            postData.put("lat", curLocation.getLatitude());
            postData.put("long", curLocation.getLongitude());
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            e.printStackTrace();
            caricamento.dismiss();
            Toast.makeText(getContext(), "Errore nell' elaborazione dei dati da inviare.", Toast.LENGTH_LONG).show();
            return;
        }

        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(ListenerParcheggiVicini);
        conn.execute(Parametri.IP + "/getParcheggiFromCoordinate");
    }

    private ConnessioneListener ListenerParcheggiVicini = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                caricamento.dismiss();
                Toast.makeText(getContext(), "ERRORE:\nConnessione Assente o server offline.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                caricamento.dismiss();
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                // Estraggo i dati dei parcheggi restituiti dal server
                ArrayList<Parcheggio> par = new ArrayList<>();
                try {
                    JSONObject allparcheggi = new JSONObject(result);
                    JSONArray parcheggi = allparcheggi.getJSONArray("parcheggi");

                    for (int i = 0; i < parcheggi.length(); i++) {
                        par.add(new Parcheggio(parcheggi.getJSONObject(i).toString()));
                        // Estraggo le info di google map
                        par.get(i).setInfo("Distanza: " + parcheggi.getJSONObject(i).get("distanzaFisica") + "\nTempo: "
                                + parcheggi.getJSONObject(i).get("distanzaTemporale"));
                    }
                } catch (Exception e) {
                    caricamento.dismiss();
                    Toast.makeText(getContext(), "Errore di risposta del server.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.parcheggi_vicini = par;

                if (Parametri.parcheggi != null) {
                    caricamento.dismiss();
                    CallFragmentVisualizzaParcheggi();
                } else {
                    Connessione connPar = new Connessione(new JSONObject(), "POST");
                    connPar.addListener(ListenerGetAllParcheggi);
                    connPar.execute(Parametri.IP + "/getAllParcheggi");
                }
            }
        }

    };

    private ConnessioneListener ListenerGetAllParcheggi = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                caricamento.dismiss();
                Toast.makeText(getContext(), "Errore di ricezione dei parcheggi.\nIl server non risponde.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                caricamento.dismiss();
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                ArrayList<Parcheggio> par = new ArrayList<>();

                try {
                    JSONObject allparcheggi = new JSONObject(result);
                    JSONArray parcheggi = allparcheggi.getJSONArray("parcheggi");

                    for (int i = 0; i < parcheggi.length(); i++)
                        par.add(new Parcheggio(parcheggi.get(i).toString()));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Errore di risposta del server.\nImpossibile recuperare la lista dei parcheggi.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.parcheggi = par;
                caricamento.dismiss();
                CallFragmentVisualizzaParcheggi();
            }
        }
    };

    private void CallFragmentVisualizzaParcheggi() {
        getActivity().setTitle("Elenco parcheggi");
        Visualizza_parcheggi fragment = new Visualizza_parcheggi();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "Visualizza parcheggi");
        fragmentTransaction.commit();
    }

    private void CallFragmentPrenotaParcheggio(int id){
        getActivity().setTitle("Prenota parcheggio");
        PrenotaParcheggio fragment = PrenotaParcheggio.newInstance(id, false);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "Prenota Parcheggio");
        fragmentTransaction.addToBackStack("Trova parcheggio");
        fragmentTransaction.commit();
    }
}
