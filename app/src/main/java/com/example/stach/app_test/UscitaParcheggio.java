package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

public class UscitaParcheggio extends FragmentWithOnBack implements BluetoothConnessioneListener {
    private final int REQUEST_ENABLE_BT = 377;

    private ProgressDialog pDialog = null;
    private PrenotazioneDaPagare prenotazione = null;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConnection btCon;
    private String macBT;

    public UscitaParcheggio() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uscita_parcheggio, container, false);

        if (Parametri.prenotazioniDaPagare != null) {
            Bundle bundle = getArguments();

            String parcheggio = bundle.getString("NomeParcheggio");
            macBT = bundle.getString("macBT");
            int id = Integer.parseInt(bundle.getString("idPrenotazione"));

            TextView nomeParcheggio = view.findViewById(R.id.nomeParcheggioExit);
            TextView dataPrenotazioneParcheggio = view.findViewById(R.id.oraIngressoParcheggioExit);

            nomeParcheggio.setText(parcheggio);

            for (int i = 0; i < Parametri.prenotazioniDaPagare.size(); i++)
                if (Parametri.prenotazioniDaPagare.get(i).getId() == id) {
                    prenotazione = Parametri.prenotazioniDaPagare.get(i);
                    break;
                }

            if (prenotazione != null) {
                String text = "\nTempo trascorso:\n" + String.valueOf(prenotazione.getMinutiPermanenza() / 60) + " ore";

                if (prenotazione.getMinutiPermanenza() % 60 != 0)
                    text = text + " e " + String.valueOf(prenotazione.getMinutiPermanenza() % 60) + " minuti";

                dataPrenotazioneParcheggio.setText("Ingresso: " + DateFormat.format("dd/MM HH:mm", prenotazione.getDataIngresso()).toString() + text);
            }

            Button btnSendCode = view.findViewById(R.id.btnQrCodeExit);
            btnSendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InviaCodiceViaBT();
                }
            });
        }
        else
            Toast.makeText(getContext(), "Riscontrati errori, prenotazione da pagare non trovata.", Toast.LENGTH_LONG).show();

        return view;
    }

    @Override
    public boolean onBackPressed() {
        getActivity().setTitle("Trova parcheggio");
        FindYourParkingFragment fragment = new FindYourParkingFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
        fragmentTransaction.commit();
        return true;
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

        btCon.Send(BluetoothConnection.USCITA);
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

        boolean escape = true;

        if (app[0].compareTo(BluetoothConnection.SUCCESS) == 0) {
            Parametri.prenotazioniDaPagare.remove(prenotazione);
            int min = Integer.parseInt(app[2]);
            String txt = (min / 60) + " ore";

            if (min % 60 != 0 || (min / 60) == 0)
                txt = txt + " e " + (min % 60) + " minuti";

            response = response + "\nHai passato " + txt + " nel parcheggio.";


            if (Parametri.prenotazioniDaPagare.size() == 0)
                escape = false;
        }

        showToastAsynch(response, Toast.LENGTH_LONG);
        goBackAsynch(escape);
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
                ((MainActivity) getActivity()).showEscape(fescape);
                getActivity().onBackPressed();
            }
        });
    }
}
