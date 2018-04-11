package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Handler e Timer scadenza prenotazioni
    private Handler handler = new Handler();
    private final int TIMER = 7 * 1000; // 7 secondi

    private ProgressDialog caricamento = null;
    private boolean launchPrenotaizoni = false;
    private boolean launchPrenPagate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_escape).setVisible(false);

        setTitle("Trova parcheggio");
        FindYourParkingFragment fragment = new FindYourParkingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
        fragmentTransaction.commit();

        // Recupero i dati delle mie prenotaizoni (per il timer delle scadenze)
        GetDatiPrenotazioni(false);
        GetDatiPrenotazioniDaPagare();
    }

    // Funzione per l'aggiornamento automatico dei posti liberi
    private Runnable runnable = new Runnable() {
        public void run() {
            ControllaScadenze();
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // Se il menù è aperto lo chiudo
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            boolean handled = false;

            for (Fragment f : fragmentList)
                if (f instanceof FragmentWithOnBack) {
                    handled = ((FragmentWithOnBack) f).onBackPressed();

                    if (handled)
                        break;
                }

            if (!handled)
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            OptionsMenu fragment = new OptionsMenu();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fram, fragment, "settings");
            fragmentTransaction.addToBackStack("");
            fragmentTransaction.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        FragmentManager sfm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = sfm.beginTransaction();
        int count = sfm.getBackStackEntryCount();
        for (int i = 0; i < count; ++i)
            sfm.popBackStack();

        if (id == R.id.nav_profile) {
            setTitle("Il tuo profilo");
            ProfileFragment fragment = new ProfileFragment();
            fragmentTransaction.replace(R.id.fram, fragment, "Fragment Profile");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_card) {
            setTitle("Aggiorna dati carta");
            Carta_di_credito fragment = new Carta_di_credito();
            fragmentTransaction.replace(R.id.fram, fragment, "Fragment Carta");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_findPark) {
            setTitle("Trova parcheggio");
            FindYourParkingFragment fragment = new FindYourParkingFragment();
            fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
            fragmentTransaction.commit();
        } else if (id == R.id.nav_your_book) {
            GetDatiPrenotazioni(true);
        } else if (id == R.id.nav_old_book) {
            GetDatiOldPrenotazioni();
        } else if (id == R.id.nav_logout) {
            Parametri.login_file.delete();
            Parametri.resetAllParametri();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_escape) {
            if (Parametri.parcheggi == null) {
                launchPrenPagate = true;
                Connessione connPar = new Connessione(new JSONObject(), "POST");
                connPar.addListener(ListenerGetParcheggi);
                connPar.execute(Parametri.IP + "/getAllParcheggi");
            } else {
                setTitle("Uscita parcheggio");
                FragmentPrenotazioniDaPagare fragment = new FragmentPrenotazioniDaPagare();
                fragmentTransaction.replace(R.id.fram, fragment, "Fragment Book");
                fragmentTransaction.commit();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, TIMER);
    }

    // Ogni tot secondi viene chiamata per controllare le scadenze delle prenotazioni in corso
    private void ControllaScadenze() {
        if (Parametri.prenotazioniInCorso != null && Parametri.parcheggi != null) {
            for (int i = 0; i < Parametri.prenotazioniInCorso.size(); i++)
                if (Parametri.prenotazioniInCorso.get(i).getTempoScadenza() <= 0) {
                    new AlertDialog.Builder(this)
                            .setTitle("Penotazione scaduta")
                            .setMessage("La tua prenotazone nel parcheggio " + Parametri.parcheggi.get(i).getIndirizzo() + " è scaduta.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).create().show();
                    Parametri.prenotazioniInCorso.remove(i);
                } else if (Parametri.prenotazioniInCorso.get(i).getTempoScadenza() <= Parametri.TEMPO_AVVISO && Parametri.TEMPO_AVVISO > 0)
                    if (!Parametri.prenotazioniInCorso.get(i).isAlreadyNotified()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Avviso scadenza")
                                .setMessage("La tua prenotazone nel parcheggio " + Parametri.parcheggi.get(i).getIndirizzo()
                                        + " scadrà tra " + ((Parametri.prenotazioniInCorso.get(i).getTempoScadenza() / 1000) / 60) + " minuti.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).create().show();
                        Parametri.prenotazioniInCorso.get(i).Notified();
                    }
        } else
            GetDatiPrenotazioni(false);

        handler.postDelayed(runnable, TIMER);
    }

    public void GetDatiPrenotazioni(boolean forLaunchPrenotaizoni) {
        JSONObject postData = new JSONObject();

        try {
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        launchPrenotaizoni = forLaunchPrenotaizoni;

        if (launchPrenotaizoni) {
            caricamento = ProgressDialog.show(MainActivity.this, "Recupero dati",
                    "Connessione con il server in corso...", true);

        }

        Connessione connPre = new Connessione(postData, "POST");
        connPre.addListener(ListenerGetPrenotazioni);
        connPre.execute(Parametri.IP + "/getPrenotazioniInAttoUtente");
    }

    private ConnessioneListener ListenerGetPrenotazioni = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                if (launchPrenotaizoni) {
                    caricamento.dismiss();
                    launchPrenotaizoni = false;
                }
                Toast.makeText(getApplicationContext(), "Errore di ricezione delle prenotazioni in corso.\nIl server non risponde.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                if (launchPrenotaizoni) {
                    caricamento.dismiss();
                    launchPrenotaizoni = false;
                }
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                List<Prenotazione> prenotazioni = new ArrayList<>();

                try {
                    JSONArray prenotazioniInAtto = (new JSONObject(result).getJSONArray("prenotazioniInAtto"));

                    for (int i = 0; i < prenotazioniInAtto.length(); i++)
                        prenotazioni.add(new Prenotazione(prenotazioniInAtto.getJSONObject(i).toString()));

                } catch (Exception e) {
                    e.printStackTrace();
                    if (launchPrenotaizoni) {
                        caricamento.dismiss();
                        launchPrenotaizoni = false;
                    }
                    Toast.makeText(getApplicationContext(), "Errore di risposta del server.\nImpossibile visualizzare le prenotazioni.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (Parametri.prenotazioniInCorso != null)
                    for (int i = 0; i < Parametri.prenotazioniInCorso.size(); i++)
                        for (int j = 0; j < prenotazioni.size(); j++)
                            if (Parametri.prenotazioniInCorso.get(i).getId() == prenotazioni.get(j).getId())
                                if (Parametri.prenotazioniInCorso.get(i).isAlreadyNotified()) {
                                    prenotazioni.get(j).Notified();
                                    break;
                                }

                Parametri.prenotazioniInCorso = prenotazioni;

                if (Parametri.parcheggi == null) {
                    Connessione connPar = new Connessione(new JSONObject(), "POST");
                    connPar.addListener(ListenerGetParcheggi);
                    connPar.execute(Parametri.IP + "/getAllParcheggi");
                } else if (launchPrenotaizoni) {
                    caricamento.dismiss();
                    setTitle("Le tue prenotazioni");
                    FragmentYour_Book fragment = new FragmentYour_Book();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fram, fragment, "Fragment Book");
                    fragmentTransaction.commit();
                    launchPrenotaizoni = false;
                }
            }
        }

    };

    private ConnessioneListener ListenerGetParcheggi = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                if (launchPrenotaizoni) {
                    caricamento.dismiss();
                    launchPrenotaizoni = false;
                }
                Toast.makeText(getApplicationContext(), "Errore di ricezione dei parcheggi.\nIl server non risponde.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                if (launchPrenotaizoni) {
                    caricamento.dismiss();
                    launchPrenotaizoni = false;
                }
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
                    if (launchPrenotaizoni) {
                        caricamento.dismiss();
                        launchPrenotaizoni = false;
                    }
                    Toast.makeText(getApplicationContext(), "Errore di risposta del server.\nImpossibile visualizzare le prenotazioni.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.parcheggi = par;

                if (launchPrenotaizoni) {
                    caricamento.dismiss();

                    setTitle("Le tue prenotazioni");
                    FragmentYour_Book fragment = new FragmentYour_Book();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fram, fragment, "Fragment Book");
                    fragmentTransaction.commit();
                    launchPrenotaizoni = false;
                } else if (launchPrenPagate) {
                    setTitle("Uscita parcheggio");
                    FragmentPrenotazioniDaPagare fragment = new FragmentPrenotazioniDaPagare();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fram, fragment, "Fragment Book");
                    fragmentTransaction.commit();
                    launchPrenPagate = false;
                }
            }
        }
    };

    private void GetDatiOldPrenotazioni() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        caricamento = ProgressDialog.show(MainActivity.this, "Recupero dati",
                "Connessione con il server in corso...", true);

        Connessione connPre = new Connessione(postData, "POST");
        connPre.addListener(listenerOldPrenotaizoni);
        connPre.execute(Parametri.IP + "/getPrenotazioniPagateUtente");
    }

    private ConnessioneListener listenerOldPrenotaizoni = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                caricamento.dismiss();
                Toast.makeText(getApplicationContext(), "Errore di ricezione delle prenotazioni in corso.\nIl server non risponde.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                caricamento.dismiss();
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                List<PrenotazionePassata> prenotazioni = new ArrayList<>();

                try {
                    JSONArray prenotazioniOld = (new JSONObject(result).getJSONArray("prenotazionePagata"));

                    for (int i = 0; i < prenotazioniOld.length(); i++)
                        prenotazioni.add(new PrenotazionePassata(prenotazioniOld.getJSONObject(i).toString()));

                } catch (Exception e) {
                    e.printStackTrace();
                    caricamento.dismiss();
                    Toast.makeText(getApplicationContext(), "Errore di risposta del server.\nImpossibile visualizzare le prenotazioni.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.prenotazioniVecchie = prenotazioni;

                if (Parametri.parcheggi == null) {
                    Connessione connPar = new Connessione(new JSONObject(), "POST");
                    connPar.addListener(ListenerGetParcheggiForOld);
                    connPar.execute(Parametri.IP + "/getAllParcheggi");
                } else {
                    caricamento.dismiss();
                    setTitle("Prenotazioni pagate");
                    Old_Book fragment = new Old_Book();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fram, fragment, "Fragment vecchie prenotazioni");
                    fragmentTransaction.commit();
                }
            }
        }
    };

    private ConnessioneListener ListenerGetParcheggiForOld = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                caricamento.dismiss();
                Toast.makeText(getApplicationContext(), "Errore di ricezione dei parcheggi.\nIl server non risponde.", Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("400")) {
                caricamento.dismiss();
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
                    caricamento.dismiss();
                    Toast.makeText(getApplicationContext(), "Errore di risposta del server.\nImpossibile visualizzare le prenotazioni.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.parcheggi = par;

                caricamento.dismiss();
                setTitle("Prenotazioni pagate");
                Old_Book fragment = new Old_Book();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fram, fragment, "Fragment vecchie prenotazioni");
                fragmentTransaction.commit();
            }
        }
    };

    public void showEscape(boolean visibile) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_escape).setVisible(visibile);
    }

    public void GetDatiPrenotazioniDaPagare() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Connessione connPre = new Connessione(postData, "POST");
        connPre.addListener(ListenerGetPrenotazioniPagate);
        connPre.execute(Parametri.IP + "/getPrenotazioniDaPagare");
    }

    private ConnessioneListener ListenerGetPrenotazioniPagate = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null)
                return;

            if (responseCode.equals("400")) {
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            if (responseCode.equals("200")) {
                List<PrenotazioneDaPagare> prenotazioni = new ArrayList<>();

                try {
                    JSONArray prenotazioniDaPagare = (new JSONObject(result).getJSONArray("prenotazioniDaPagare"));

                    for (int i = 0; i < prenotazioniDaPagare.length(); i++)
                        prenotazioni.add(new PrenotazioneDaPagare(prenotazioniDaPagare.getJSONObject(i).toString()));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Errore di risposta del server.\nImpossibile reperire le prenotaizoni da pagare.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (prenotazioni.size() > 0) {
                    Parametri.prenotazioniDaPagare = prenotazioni;
                    showEscape(true);
                }
            }
        }
    };
}
