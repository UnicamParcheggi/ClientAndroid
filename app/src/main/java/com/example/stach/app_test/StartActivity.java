package com.example.stach.app_test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class StartActivity extends AppCompatActivity {
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Parametri.login_file = new File(this.getFilesDir(), "data.lgn");
        Parametri.advance_setting_file = new File(this.getFilesDir(), "advoption.lgn");

        caricaImpostazioniAvanzate();

        if (recruitData())
            sendDataForLogin();
        else {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void sendDataForLogin() {
        JSONObject postData = new JSONObject();

        try {
            postData.put("username", username);
            postData.put("password", password);
        }catch (Exception e){
            e.printStackTrace();
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Creo ed eseguo una connessione con il server web
        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(loginListenerConnessione);
        conn.execute(Parametri.IP + "/login");
    }

    private ConnessioneListener loginListenerConnessione = new ConnessioneListener() {
        @Override
        public void ResultResponse(String responseCode, String result) {
            if (responseCode == null) {
                Toast.makeText(getApplicationContext(), "ERRORE:\nConnessione Assente o server offline.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                return;
            }

            if (responseCode.equals("400")) {
                String message = Connessione.estraiErrore(result);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                return;
            }

            if (responseCode.equals("200")) {
                String message;

                // Estraggo i miei dati restituiti dal server
                try {
                    JSONObject token = new JSONObject(result);
                    JSONObject autistajs = new JSONObject(token.getString("autista"));
                    JSONObject carta;

                    Parametri.Token = token.getString("token");
                    Parametri.id = autistajs.getString("id");
                    Parametri.username = autistajs.getString("username");
                    Parametri.nome = autistajs.getString("nome");
                    Parametri.cognome = autistajs.getString("cognome");
                    Parametri.data_nascita = autistajs.getString("dataDiNascita");
                    Parametri.email = autistajs.getString("email");
                    Parametri.password = autistajs.getString("password");
                    Parametri.saldo = autistajs.getString("saldo");
                    Parametri.telefono = autistajs.getString("telefono");

                    message = "Benvenuto " + Parametri.nome + ".";

                    // Tento l'estrazione dei dati della carta di credito
                    if (autistajs.has("carta_di_credito")) {
                        carta = new JSONObject(autistajs.getString("carta_di_credito"));

                        if (carta.has("numero_carta"))
                            Parametri.numero_carta = carta.getString("numero_carta");
                        if (carta.has("dataDiScadenza"))
                            Parametri.data_di_scadenza = carta.getString("dataDiScadenza");
                        if (carta.has("pin"))
                            Parametri.pin = carta.getString("pin");
                    }

                } catch (Exception e) {
                    message = "Errore di risposta del server.";

                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    return;
                }

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }
    };

    private boolean recruitData() {
        String mailResult = "";
        String passwordResult = "";

        try {
            String appoggio;
            BufferedReader fos1 = new BufferedReader(new FileReader(Parametri.login_file.getAbsolutePath()));

            // Leeggo l'email
            if ((appoggio = fos1.readLine()) != null)
                mailResult = appoggio;

            // Leeggo la password
            if ((appoggio = fos1.readLine()) != null)
                passwordResult = appoggio;

            // Leggo le impostazioni
            if ((appoggio = fos1.readLine()) != null)
                Parametri.TEMPO_EXTRA = Integer.parseInt(appoggio);

            if ((appoggio = fos1.readLine()) != null)
                Parametri.TEMPO_AVVISO = Integer.parseInt(appoggio);

            fos1.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        username = mailResult;
        password = passwordResult;
        return true;
    }

    private void caricaImpostazioniAvanzate() {
        try {
            String app;
            BufferedReader fos1 = new BufferedReader(new FileReader(Parametri.advance_setting_file.getAbsolutePath()));

            if ((app = fos1.readLine()) != null)
                Parametri.IP = app;

            fos1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
