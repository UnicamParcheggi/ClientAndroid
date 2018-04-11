package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PrenotaParcheggio extends FragmentWithOnBack {
    private int index;
    private int tipo_parcheggio;
    private View view;
    private ProgressDialog caricamento;
    private boolean needBack;

    // Intervallo ed handler per aggiornare i posti liberi
    private final int TIMER = 5 * 1000; // 5 secondi
    private Handler handler = new Handler();

    public static PrenotaParcheggio newInstance(int indice, boolean needBack) {
        PrenotaParcheggio fragment = new PrenotaParcheggio();
        Bundle args = new Bundle();
        args.putInt("ID", indice);
        args.putBoolean("needBack", needBack);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_prenota_parcheggio, container, false);

        int id = getArguments().getInt("ID", -1);
        needBack = getArguments().getBoolean("needBack");

        for (index = 0; index < Parametri.parcheggi.size(); index++)
            if (Parametri.parcheggi.get(index).getId() == id)
                break;

        TextView informazioni = view.findViewById(R.id.textViewViaParcheggio);
        informazioni.setText(Parametri.parcheggi.get(index).getIndirizzo() + "\n\nTariffe orarie:\nLavorativi: "
                + Parametri.parcheggi.get(index).getPrezzoLavorativi() + "€\nFestivi: " + Parametri.parcheggi.get(index).getPrezzoFestivi() + "€");
        RadioButton rd = view.findViewById(R.id.RadioAuto);
        String str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.AUTO]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioCamper);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.CAMPER]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioMoto);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.MOTO]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioAutobus);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.AUTOBUS]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioDisabile);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.DISABILE]);
        rd.setText(str);
        Button btn = view.findViewById(R.id.BtnPrenota);

        // Prenoto
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Prenotazione(view);
            }
        });

        ChiediPostiLiberi();
        return view;
    }

    // Funzione per l'aggiornamento automatico dei posti liberi
    private Runnable runnable = new Runnable() {
        public void run() {
            ChiediPostiLiberi();
        }
    };

    public void Prenotazione(View view) {
        // Avverto l'utente del tentativo di invio dei dati per la prenotazione
        caricamento = ProgressDialog.show(getContext(), "Invio prenotazione",
                "Invio prenotazione in corso...", true);

        tipo_parcheggio = -1;

        RadioButton rdAuto = view.findViewById(R.id.RadioAuto);
        RadioButton rdMoto = view.findViewById(R.id.RadioMoto);
        RadioButton rdCamper = view.findViewById(R.id.RadioCamper);
        RadioButton rdAutobus = view.findViewById(R.id.RadioAutobus);
        RadioButton rdDisabile = view.findViewById(R.id.RadioDisabile);

        for (int i = 0; i < 5; i++) {
            if (rdAuto.isChecked())
                tipo_parcheggio = TipoPosto.AUTO;
            if (rdMoto.isChecked())
                tipo_parcheggio = TipoPosto.MOTO;
            if (rdCamper.isChecked())
                tipo_parcheggio = TipoPosto.CAMPER;
            if (rdAutobus.isChecked())
                tipo_parcheggio = TipoPosto.AUTOBUS;
            if (rdDisabile.isChecked())
                tipo_parcheggio = TipoPosto.DISABILE;
        }

        if (tipo_parcheggio == -1) {
            caricamento.dismiss();
            Toast.makeText(this.getContext(), "Selezionare un tipo di parcheggio!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject postData = new JSONObject();

        try {
            postData.put("idParcheggio", Parametri.parcheggi.get(index).getId());
            postData.put("macBT", Parametri.parcheggi.get(index).getMacBT());
            postData.put("tipoParcheggio", tipo_parcheggio);
            postData.put("token", Parametri.Token);
            postData.put("tempoExtra", Parametri.TEMPO_EXTRA);

            // Se conosco la mia posizione attuale la invio al server insieme a quella della mia destinazione
            if (Parametri.lastKnowPosition != null) {
                JSONObject pos = new JSONObject();
                pos.put("lat", Parametri.lastKnowPosition.getLatitude());
                pos.put("long", Parametri.lastKnowPosition.getLongitude());
                postData.put("partenza", pos);
                pos = new JSONObject();
                pos.put("lat", Parametri.parcheggi.get(index).getCoordinate().latitude);
                pos.put("long", Parametri.parcheggi.get(index).getCoordinate().longitude);
                postData.put("destinazione", pos);
            }

        } catch (Exception e) {
            Toast.makeText(this.getContext(), "Errore nell' elaborazione dei dati da inviare!", Toast.LENGTH_LONG).show();
            return;
        }

        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(ListenerConfermaPrenotazione);
        conn.execute(Parametri.IP + "/effettuaPrenotazione");
    }

    private ConnessioneListener ListenerConfermaPrenotazione = new ConnessioneListener() {
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
                if (Parametri.prenotazioniInCorso == null)
                    Parametri.prenotazioniInCorso = new ArrayList<>();

                Prenotazione pren;

                try {
                    JSONObject risp = new JSONObject(result);
                    String code = risp.getString("QR_Code");
                    Date data_scadenza = stringToDate(risp.getString("scadenza"), "yyyy-MM-dd HH:mm:ss");
                    int id = risp.getInt("idPrenotazione");
                    pren = new Prenotazione(id, data_scadenza, Parametri.parcheggi.get(index).getId(), tipo_parcheggio, code);
                } catch (Exception e) {
                    caricamento.dismiss();
                    Toast.makeText(getContext(), "Errore di risposta del server.", Toast.LENGTH_LONG).show();
                    return;
                }

                Parametri.prenotazioniInCorso.add(pren);
                caricamento.dismiss();
                Toast.makeText(getContext(), "Prenotazione effettutata con successo!", Toast.LENGTH_LONG).show();

                //passo le informazioni relative alla mia prenotazione
                Bundle bundle = new Bundle();
                bundle.putString("idPrenotazione", String.valueOf(pren.getId()));
                bundle.putString("NomeParcheggio", String.valueOf(Parametri.parcheggi.get(index).getIndirizzo()));
                bundle.putString("macBT", String.valueOf(Parametri.parcheggi.get(index).getMacBT()));
                bundle.putBoolean("needBack", false);
                getActivity().setTitle("Le tue prenotazioni");
                //eseguo la transazione
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //passo i valori
                Detail_Book detail_book = new Detail_Book();
                detail_book.setArguments(bundle);
                //eseguo la transazione
                fragmentTransaction.replace(R.id.fram, detail_book);
                fragmentTransaction.commit();
            }
        }

    };

    private void ChiediPostiLiberi() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("id", Parametri.parcheggi.get(index).getId());
            postData.put("token", Parametri.Token);
        } catch (Exception e) {
            Toast.makeText(this.getContext(), "Errore nell' elaborazione dei dati da inviare!", Toast.LENGTH_LONG).show();
            return;
        }

        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(ListenerPostiLiberi);
        conn.execute(Parametri.IP + "/getPostiLiberiParcheggio");
        handler.postDelayed(runnable, TIMER);
    }

    private ConnessioneListener ListenerPostiLiberi = new ConnessioneListener() {
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
                int[] postiLib = new int[TipoPosto.N_POSTI];

                try {
                    JSONObject posti = (new JSONObject(result).getJSONObject("postiLiberi"));

                    postiLib[TipoPosto.AUTO] = posti.getInt("nPostiMacchina");
                    postiLib[TipoPosto.AUTOBUS] = posti.getInt("nPostiAutobus");
                    postiLib[TipoPosto.CAMPER] = posti.getInt("nPostiCamper");
                    postiLib[TipoPosto.MOTO] = posti.getInt("nPostiMoto");
                    postiLib[TipoPosto.DISABILE] = posti.getInt("nPostiDisabile");
                } catch (Exception e) {
                    caricamento.dismiss();
                    Toast.makeText(getContext(), "Errore di risposta del server.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Parametri.parcheggi.get(index).setPostiLiberi(postiLib);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Errore di risposta del server.", Toast.LENGTH_LONG).show();
                    return;
                }


                if (caricamento != null)
                    if (caricamento.isShowing())
                        caricamento.dismiss();

                AggiornaPostiLiberi();
            }
        }

    };

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

    private void AggiornaPostiLiberi() {
        RadioButton rd = view.findViewById(R.id.RadioAuto);
        String str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.AUTO]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioCamper);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.CAMPER]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioMoto);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.MOTO]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioAutobus);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.AUTOBUS]);
        rd.setText(str);
        rd = view.findViewById(R.id.RadioDisabile);
        str = String.valueOf(Parametri.parcheggi.get(index).getPostiLiberi()[TipoPosto.DISABILE]);
        rd.setText(str);
    }

    private Date stringToDate(String data, String format) {
        Date stringDate = null;

        if (data == null)
            return null;

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
        simpledateformat.setTimeZone(tz);

        try {
            stringDate = simpledateformat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stringDate;
    }

    @Override
    public boolean onBackPressed() {
        if (!needBack) {
            getActivity().setTitle("Trova parcheggio");
            FindYourParkingFragment fragment = new FindYourParkingFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
            fragmentTransaction.commit();
            return true;
        }
        else {
            getActivity().setTitle("Elenco parcheggi");
            return false;
        }
    }
}
