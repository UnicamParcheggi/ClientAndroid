package com.example.stach.app_test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;

public class Cambia_credenziali extends FragmentWithOnBack implements ConnessioneListener {
    private EditText t_nome;
    private EditText t_cognome;
    private EditText t_data;
    private EditText t_telefono;
    private EditText t_email;
    private EditText t_vecchia_password;
    private EditText t_nuova_password;
    private EditText t_username;

    private String nome;
    private String cognome;
    private String data;
    private String telefono;
    private String email;
    private String password;
    private String username;

    // è inutile prendere qui context e activity perché avviene prima della creazione della classe stessa e quindi sono sempre NULL

    public Cambia_credenziali() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cambia_credenziali, container, false);

        t_nome = view.findViewById(R.id.nuova_Nome);
        t_cognome = view.findViewById(R.id.nuova_Cognome);
        t_data = view.findViewById(R.id.nuova_Data_Nascita);
        t_telefono = view.findViewById(R.id.nuova_Telefono);
        t_email = view.findViewById(R.id.nuova_Email);
        t_vecchia_password = view.findViewById(R.id.vecchia_password);
        t_nuova_password = view.findViewById(R.id.nuova_password);
        t_username = view.findViewById(R.id.nuova_Username);

        t_nome.setText(Parametri.nome);
        t_cognome.setText(Parametri.cognome);
        t_data.setText(Parametri.data_nascita);
        t_telefono.setText(Parametri.telefono);
        t_email.setText(Parametri.email);
        t_username.setText(Parametri.username);

        Button sendCredenziali = view.findViewById((R.id.nuova_buttonCommit));
        final ConnessioneListener me = this;
        sendCredenziali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cur_password;
                String new_password;

                password = Parametri.password;

                // Prelevo i dati per il login per inviarli al server.
                nome = t_nome.getText().toString();
                cognome = t_cognome.getText().toString();
                data = t_data.getText().toString();
                telefono = t_telefono.getText().toString();
                email = t_email.getText().toString();
                cur_password = t_vecchia_password.getText().toString();
                new_password = t_nuova_password.getText().toString();
                username = t_username.getText().toString();

                if (new_password.length() > 0 && cur_password.compareTo(password) == 0)
                    password = new_password;

                JSONObject postData = new JSONObject();
                JSONObject autista = new JSONObject();

                try {
                    autista.put("username", username);
                    autista.put("password", password);
                    autista.put("email", email);
                    autista.put("nome", nome);
                    autista.put("cognome", cognome);
                    autista.put("dataDiNascita", data);
                    autista.put("telefono", telefono);
                    postData.put("autista", autista);
                    postData.put("token", Parametri.Token);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "ERRORE:\nimpossibile leggere i campi appena compilati.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Creo ed eseguo una connessione con il server web
                Connessione conn = new Connessione(postData, "PATCH");
                conn.addListener(me);
                conn.execute(Parametri.IP + "/cambiaCredenziali");
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void AggiornaParametri(){
        Parametri.nome = nome;
        Parametri.cognome = cognome;
        Parametri.data_nascita = data;
        Parametri.password = password;
        Parametri.username = username;
        Parametri.telefono = telefono;
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

            AggiornaParametri();
            getActivity().onBackPressed();
        }
    }
}
