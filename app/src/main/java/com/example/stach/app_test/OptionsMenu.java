package com.example.stach.app_test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class OptionsMenu extends FragmentWithOnBack implements AdapterView.OnItemSelectedListener {
    private String titolo;

    public OptionsMenu() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options_menu, container, false);

        Spinner spinnerExtra = view.findViewById(R.id.spinnerTempoExtra);
        Spinner spinnerAvvisa = view.findViewById(R.id.spinnerAvvisa);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(view.getContext(), R.array.optionsMenuItem1, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(view.getContext(), R.array.optionsMenuItem2, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExtra.setAdapter(adapter1);
        spinnerAvvisa.setAdapter(adapter2);

        spinnerExtra.setOnItemSelectedListener(this);
        spinnerAvvisa.setOnItemSelectedListener(this);

        int[] opzioniExtra = getResources().getIntArray(R.array.optionsMenuItem1value);
        for (int i = 0; i < opzioniExtra.length; i++)
            if (opzioniExtra[i] == (Parametri.TEMPO_EXTRA / 60 / 1000)) {
                spinnerExtra.setSelection(i);
                break;
            }

        int[] opzioniAvvisa = getResources().getIntArray(R.array.optionsMenuItem2value);
        for (int i = 0; i < opzioniAvvisa.length; i++)
            if (opzioniAvvisa[i] == (Parametri.TEMPO_AVVISO / 60 / 1000)) {
                spinnerAvvisa.setSelection(i);
                break;
            }

        Button buttonSalva = view.findViewById(R.id.buttonSalvaAdvanceImpostazioni);
        buttonSalva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SalvaImpostazioni();
            }
        });

        titolo = getActivity().getTitle().toString();
        getActivity().setTitle("Settings");

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.spinnerTempoExtra) {
            int[] opzioni = getResources().getIntArray(R.array.optionsMenuItem1value);
            Parametri.TEMPO_EXTRA = opzioni[pos] * 60 * 1000;
        } else if (parent.getId() == R.id.spinnerAvvisa) {
            int[] opzioni = getResources().getIntArray(R.array.optionsMenuItem2value);
            Parametri.TEMPO_AVVISO = opzioni[pos] * 60 * 1000;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void SalvaImpostazioni() {
        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Parametri.login_file.getAbsolutePath()));
            fos.write(Parametri.username + "\n");
            fos.write(Parametri.password + "\n");
            fos.write(Parametri.TEMPO_EXTRA + "\n");
            fos.write(Parametri.TEMPO_AVVISO + "\n");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Errore nel salvataggio delle impostazioni.", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getContext(), "Impostazioni salvate.", Toast.LENGTH_SHORT).show();
        getActivity().setTitle("Trova parcheggio");
        getActivity().onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        getActivity().setTitle(titolo);
        return false;
    }
}
