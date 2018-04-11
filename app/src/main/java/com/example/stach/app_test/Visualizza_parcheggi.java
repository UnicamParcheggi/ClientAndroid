package com.example.stach.app_test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Visualizza_parcheggi extends FragmentWithOnBack {
    private int index = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualizza_parcheggi, container, false);
        View linearLayout = view.findViewById(R.id.linearLayoutVisualizza);

        if (Parametri.parcheggi_vicini != null) {
            TextView[] info_parcheggio = new TextView[Parametri.parcheggi_vicini.size()];

            for (int i = 0; i < Parametri.parcheggi_vicini.size(); i++) {
                Parcheggio parcheggio = Parametri.parcheggi_vicini.get(i);

                info_parcheggio[i] = new TextView(view.getContext());
                info_parcheggio[i].setText(parcheggio.getIndirizzo_format() + "\n" + parcheggio.getInfo());
                info_parcheggio[i].setBackgroundResource(R.drawable.roundedtextbox);
                info_parcheggio[i].setPaddingRelative(0, 8, 0, 8);
                // Setto l'id della text view come indice del vettore dei parcheggi vicini
                info_parcheggio[i].setId(i);
                info_parcheggio[i].setTextSize(19);
                info_parcheggio[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                info_parcheggio[i].setTextColor(Color.BLACK);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                param.setMargins(0,0,0,32);
                info_parcheggio[i].setLayoutParams(param);

                ((LinearLayout) linearLayout).addView(info_parcheggio[i]);

                info_parcheggio[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        SetColor(arg0);
                    }
                });
            }

            Button btn = new Button(view.getContext());
            btn.setText("SELEZIONA PARCHEGGIO");
            btn.setBackgroundResource(R.drawable.roundedbutton);
            btn.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            param.setMargins(0,0,0,32);
            btn.setLayoutParams(param);
            ((LinearLayout) linearLayout).addView(btn);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ConfermaPrenotazione();
                }
            });
        }

        return view;
    }

    private void SetColor(View tw) {
        for (int j = 0; j < Parametri.parcheggi_vicini.size(); j++) {
            TextView pr = getActivity().findViewById(j);
            pr.setBackgroundResource(R.drawable.roundedtextbox);
        }
        tw.setBackgroundResource(R.drawable.roundedtextboxactive);
        index = tw.getId();
    }

    private void ConfermaPrenotazione() {
        int id = -1;

        if (index >= 0 && index < Parametri.parcheggi_vicini.size())
            id = Parametri.parcheggi_vicini.get(index).getId();

        if (id == -1) {
            Toast.makeText(this.getContext(), "Selezionare un parcheggio!", Toast.LENGTH_LONG).show();
            return;
        }

        getActivity().setTitle("Prenota parcheggio");
        PrenotaParcheggio fragment = PrenotaParcheggio.newInstance(id, true);
        android.support.v4.app.FragmentManager fmanager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fmanager.beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "PrenotaParcheggio");
        fragmentTransaction.addToBackStack("Visualizza parcheggi");
        fragmentTransaction.commit();
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