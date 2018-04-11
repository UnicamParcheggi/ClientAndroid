package com.example.stach.app_test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DetailOldBook extends Fragment {

    public DetailOldBook() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_old_book, container, false);

        if (Parametri.prenotazioniVecchie != null) {
            PrenotazionePassata prenotazione = null;
            Bundle bundle = getArguments();

            String parcheggio = bundle.getString("NomeParcheggio");
            int id = Integer.parseInt(bundle.getString("idPrenotazione"));

            TextView nomeParcheggio = view.findViewById(R.id.textViewParcheggioOldBook);
            TextView dataPrenotazioneParcheggio = view.findViewById(R.id.textViewDataOldBook);
            TextView orecosto = view.findViewById(R.id.textViewOreCostoOldBook);

            nomeParcheggio.setText(parcheggio);

            for (int i = 0; i < Parametri.prenotazioniVecchie.size(); i++)
                if (Parametri.prenotazioniVecchie.get(i).getId() == id) {
                    prenotazione = Parametri.prenotazioniVecchie.get(i);
                    break;
                }

            if (prenotazione != null) {
                String text = "Posto prenotato: " + TipoPosto.getNomeTipoPosto(prenotazione.getIdTipo())
                        + "\nOre permanenza: " + String.valueOf(prenotazione.getOrePermanenza() / 60);

                if (prenotazione.getOrePermanenza() % 60 != 0  || (prenotazione.getOrePermanenza() / 60) == 0)
                    text = text + ", " + String.valueOf(prenotazione.getOrePermanenza() % 60) + " minuti";

                dataPrenotazioneParcheggio.setText(DateFormat.format("dd MMMM yyyy", prenotazione.getData()).toString());
                orecosto.setText(text);
            }
        }
        else
            Toast.makeText(getContext(), "Riscontrati errori, prenotazione non trovata.", Toast.LENGTH_LONG).show();

        return view;
    }

}
