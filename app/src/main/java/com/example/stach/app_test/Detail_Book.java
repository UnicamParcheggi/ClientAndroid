package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Detail_Book extends FragmentWithOnBack implements ConnessioneListener, BluetoothConnessioneListener {
    private final int REQUEST_ENABLE_BT = 377;

    private ProgressDialog pDialog = null;
    private Prenotazione prenotazione = null;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConnection btCon;
    private String macBT;
    private boolean needBack;

    public Detail_Book() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail__book, container, false);

        if (Parametri.prenotazioniInCorso != null) {
            Bundle bundle = getArguments();

            needBack = bundle.getBoolean("needBack");
            String parcheggio = bundle.getString("NomeParcheggio");
            macBT = bundle.getString("macBT");
            int id = Integer.parseInt(bundle.getString("idPrenotazione"));

            TextView nomeParcheggio = view.findViewById(R.id.nomeParcheggio);
            TextView oraPrenotazioneParcheggio = view.findViewById(R.id.oraPrenotazioneParcheggio);
            TextView informazioni = view.findViewById(R.id.textViewInfo);

            nomeParcheggio.setText(parcheggio);

            for (int i = 0; i < Parametri.prenotazioniInCorso.size(); i++)
                if (Parametri.prenotazioniInCorso.get(i).getId() == id) {
                    prenotazione = Parametri.prenotazioniInCorso.get(i);
                    break;
                }

            if (prenotazione != null) {
                oraPrenotazioneParcheggio.setText(DateFormat.format("dd MMMM yyyy HH:mm:ss", prenotazione.getScadenza()).toString());
                informazioni.setText("Posto prenotato: " + TipoPosto.getNomeTipoPosto(prenotazione.getIdTipo()));
            }

            Button buttonDeletePrenotazione = view.findViewById(R.id.btnEraseBook);
            buttonDeletePrenotazione.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Selezione parcheggio")
                            .setMessage("Sicuro di voler cancellare questa prenotaizone ?")
                            .setPositiveButton("Cancella",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            EliminaPrenotazione();
                                        }
                                    }).setNegativeButton("Annulla",
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
            });

            Button buttonNaviga = view.findViewById(R.id.btnNaviga);
            buttonNaviga.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Naviga();
                }
            });

            Button btnSendCode = view.findViewById(R.id.btnQrCodeEnter);
            btnSendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InviaCodiceViaBT();
                }
            });
        } else
            Toast.makeText(getContext(), "Riscontrati errori, prenotazione non trovata.", Toast.LENGTH_LONG).show();

        return view;
    }

    private void EliminaPrenotazione() {
        if (prenotazione == null)
            return;

        JSONObject postData = new JSONObject();

        try {
            postData.put("idPrenotazione", prenotazione.getId());
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Errore nell' elaborazione dei dati da inviare!", Toast.LENGTH_LONG).show();
            return;
        }

        Connessione conn = new Connessione(postData, "DELETE");
        conn.addListener(this);
        conn.execute(Parametri.IP + "/deletePrenotazione");
    }

    private void Naviga() {
        if (Parametri.parcheggi == null)
            return;

        LatLng destinazione = null;

        for (Parcheggio p : Parametri.parcheggi)
            if (p.getId() == prenotazione.getIdParcheggio()) {
                destinazione = p.getCoordinate();
                break;
            }

        if (destinazione != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinazione.latitude + "," + destinazione.longitude + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
            getActivity().finish();
        }
    }

    @Override
    public void ResultResponse(String responseCode, String result) {
        if (responseCode == null) {
            Toast.makeText(getContext(), "ERRORE:\nConnessione Assente o server offline.", Toast.LENGTH_LONG).show();
            return;
        }

        if (responseCode.equals("400")) {
            String message = Connessione.estraiErrore(result);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            return;
        }

        if (responseCode.equals("200")) {
            String message = Connessione.estraiSuccessful(result);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Parametri.prenotazioniInCorso.remove(prenotazione);
            getActivity().onBackPressed();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (!needBack) {
            FragmentManager sfm = getActivity().getSupportFragmentManager();
            int count = sfm.getBackStackEntryCount();
            for (int i = 0; i < count; ++i)
                sfm.popBackStack();

            getActivity().setTitle("Trova parcheggio");
            FindYourParkingFragment fragment = new FindYourParkingFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
            fragmentTransaction.commit();
            return true;
        } else
            return false;
    }

    private void InviaCodiceViaBT() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), "Il suo dispositivo non supporta il Bluetooth.", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macBT);
            btCon = new BluetoothConnection(device);
            btCon.addListener(this);
            pDialog = ProgressDialog.show(getContext(), "Attendere", "Connessione Bluetooth in corso...", true);
            btCon.openConnection();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (mBluetoothAdapter.isEnabled()) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macBT);
                    btCon = new BluetoothConnection(device);
                    btCon.addListener(this);
                    pDialog = ProgressDialog.show(getContext(), "Attendere", "Connessione Bluetooth in corso...", true);
                    btCon.openConnection();
                } else
                    Toast.makeText(getContext(), "Deve accendere il Bluetooth per inviare il codice.", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Listener per la connessione bluetooth
    @Override
    public void ConnessioneStabilita(boolean sucess) {
        if (!sucess) {
            pDialog.dismiss();
            showToastAsynch("Connessione fallita, riprovi ad inviare il codice.", Toast.LENGTH_SHORT);
            return;
        }

        mBluetoothAdapter.cancelDiscovery();

        btCon.Send(BluetoothConnection.INGRESSO);
        btCon.Send(prenotazione.getCodice());
        String response;

        try {
            response = btCon.Receive();
        } catch (IOException e) {
            e.printStackTrace();
            response = null;
        }

        if (response == null) {
            pDialog.dismiss();
            showToastAsynch("Invio codice riuscito.\nErrore durante la ricezione della risposta.", Toast.LENGTH_SHORT);
            btCon.Close();
            return;
        }

        btCon.Close();

        String[] app = response.split("\\|");
        response = app[1];

        pDialog.dismiss();

        PrenotazioneDaPagare pnuova = null;

        if (app[0].compareTo(BluetoothConnection.SUCCESS) == 0) {
            Parametri.prenotazioniInCorso.remove(prenotazione);

            if (Parametri.prenotazioniDaPagare == null)
                Parametri.prenotazioniDaPagare = new ArrayList<>();


            try {
                pnuova = new PrenotazioneDaPagare(Integer.parseInt(app[2]), new Date(),
                        prenotazione.getIdParcheggio(), prenotazione.getIdTipo(), prenotazione.getCodice());

            } catch (Exception e) {
                showToastAsynch("Sei abilitato ad entrare.\nRiscontrati errori nell' elaborazione dei dati ricevuti, riavviare l'applicaizone per poter uscire dal parcheggio.", Toast.LENGTH_LONG);
                goBackAsynch(false);
                return;
            }

            Parametri.prenotazioniDaPagare.add(pnuova);
        }

        showToastAsynch(response, Toast.LENGTH_LONG);
        goBackAsynch(true);
    }

    private void showToastAsynch(String msg, int durata) {
        final String fmsg = msg;
        final int fdurata = durata;

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getContext(), fmsg, fdurata).show();
            }
        });
    }

    private void goBackAsynch(boolean escape) {
        final boolean fescape = escape;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (fescape)
                    ((MainActivity) getActivity()).showEscape(true);
                getActivity().onBackPressed();
            }
        });
    }
}
