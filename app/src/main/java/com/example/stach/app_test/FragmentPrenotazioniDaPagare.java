package com.example.stach.app_test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class FragmentPrenotazioniDaPagare extends FragmentWithOnBack {
    private List<Parcheggio> parcheggi = new ArrayList<>();

    public FragmentPrenotazioniDaPagare() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prenotazioni_da_pagare, container, false);
        View linearLayout = view.findViewById(R.id.linearInternalPrenotazioniDaPagare);

        if (Parametri.prenotazioniDaPagare != null)
            if (Parametri.prenotazioniDaPagare.size() > 0) {
                TextView[] viewPrenotazioni = new TextView[Parametri.prenotazioniDaPagare.size()];

                // Recupero i parcheggi collegati alle mie prenotazioni
                for (int i = 0; i < Parametri.prenotazioniDaPagare.size(); i++) {
                    for (int j = 0; j < Parametri.parcheggi.size(); j++) {
                        if (Parametri.prenotazioniDaPagare.get(i).getIdParcheggio() == Parametri.parcheggi.get(j).getId()) {
                            parcheggi.add(Parametri.parcheggi.get(j));
                            break;
                        }
                    }
                }

                for (int i = 0; i < Parametri.prenotazioniDaPagare.size(); i++) {
                    viewPrenotazioni[i] = new TextView(view.getContext());
                    viewPrenotazioni[i].setText(parcheggi.get(i).getIndirizzo() + "\nIngresso: " + DateFormat.format("dd/MM HH:mm", Parametri.prenotazioniDaPagare.get(i).getDataIngresso()).toString());
                    viewPrenotazioni[i].setId(i);
                    viewPrenotazioni[i].setBackgroundResource(R.drawable.roundedtextboxactive);
                    viewPrenotazioni[i].setPaddingRelative(8, 8, 8, 8);
                    viewPrenotazioni[i].setTextSize(19);
                    viewPrenotazioni[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    viewPrenotazioni[i].setTextColor(Color.BLACK);

                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    param.setMargins(0, 0, 0, 32);

                    viewPrenotazioni[i].setLayoutParams(param);

                    ((LinearLayout) linearLayout).addView(viewPrenotazioni[i]);

                    //Setto la funzione da chiamare per mostrare i dettagli della prenotazione
                    viewPrenotazioni[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int index = view.getId();

                            if (index >= 0 && index < Parametri.prenotazioniDaPagare.size()) {
                                //passo le informazioni relative alla mia prenotazione
                                Bundle bundle = new Bundle();
                                bundle.putString("idPrenotazione", String.valueOf(Parametri.prenotazioniDaPagare.get(index).getId()));
                                bundle.putString("NomeParcheggio", String.valueOf(parcheggi.get(index).getIndirizzo()));
                                bundle.putString("macBT", String.valueOf(parcheggi.get(index).getMacBT()));
                                //eseguo la transazione
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                //passo i valori
                                UscitaParcheggio uscita_parcheggio = new UscitaParcheggio();
                                uscita_parcheggio.setArguments(bundle);
                                //eseguo la transazione
                                fragmentTransaction.replace(R.id.fram, uscita_parcheggio);
                                fragmentTransaction.commit();
                            }
                        }
                    });
                }
            } else {
                TextView viewPrenotazioni = new TextView(view.getContext());
                viewPrenotazioni.setText("Non hai nessuna prenotazione da pagare.");
                viewPrenotazioni.setId(0);
                viewPrenotazioni.setBackgroundResource(R.drawable.roundedtextboxactive);
                viewPrenotazioni.setPaddingRelative(8, 8, 8, 8);
                viewPrenotazioni.setTextSize(19);
                viewPrenotazioni.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                viewPrenotazioni.setTextColor(Color.BLACK);

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                param.setMargins(0, 0, 0, 32);

                viewPrenotazioni.setLayoutParams(param);

                ((LinearLayout) linearLayout).addView(viewPrenotazioni);
            }
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
}
