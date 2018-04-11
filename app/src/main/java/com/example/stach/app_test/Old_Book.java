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

public class Old_Book extends FragmentWithOnBack {
    // Parcheggi associati alle mie prenotazioni da pagare
    private List<Parcheggio> parcheggi = new ArrayList<>();

    public Old_Book() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_old__book, container, false);
        View linearLayout = view.findViewById(R.id.linearInternalOldBook);

        if (Parametri.prenotazioniVecchie != null) {
            if (Parametri.prenotazioniVecchie.size() > 0) {
                TextView[] viewPrenotazioni = new TextView[Parametri.prenotazioniVecchie.size()];

                // Recupero i parcheggi collegati alle mie prenotazioni
                for (int i = 0; i < Parametri.prenotazioniVecchie.size(); i++) {
                    for (int j = 0; j < Parametri.parcheggi.size(); j++) {
                        if (Parametri.prenotazioniVecchie.get(i).getIdParcheggio() == Parametri.parcheggi.get(j).getId()) {
                            parcheggi.add(Parametri.parcheggi.get(j));
                            break;
                        }
                    }
                }

                for (int i = 0; i < Parametri.prenotazioniVecchie.size(); i++) {
                    viewPrenotazioni[i] = new TextView(view.getContext());
                    viewPrenotazioni[i].setText(parcheggi.get(i).getIndirizzo() + "\nData: " + DateFormat.format("dd MMMM yyyy HH:mm", Parametri.prenotazioniVecchie.get(i).getData()).toString());
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

                            if (index >= 0 && index < Parametri.prenotazioniVecchie.size()) {
                                //passo le informazioni relative alla mia prenotazione
                                Bundle bundle = new Bundle();
                                bundle.putString("idPrenotazione", String.valueOf(Parametri.prenotazioniVecchie.get(index).getId()));
                                bundle.putString("NomeParcheggio", String.valueOf(parcheggi.get(index).getIndirizzo()));
                                //eseguo la transazione
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                //passo i valori
                                DetailOldBook detail_old_book = new DetailOldBook();
                                detail_old_book.setArguments(bundle);
                                //eseguo la transazione
                                fragmentTransaction.replace(R.id.fram, detail_old_book);
                                fragmentTransaction.addToBackStack("Prenotazioni pagate");
                                fragmentTransaction.commit();
                            }
                        }
                    });
                }
            } else {
                TextView viewPrenotazioni = new TextView(view.getContext());
                viewPrenotazioni.setText("Non hai nessuna prenotazione passata.");
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
