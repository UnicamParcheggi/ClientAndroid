package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity implements TextWatcher, ConnessioneListener {
    private ProgressDialog caricamento = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    /**
     * This method will send user credentials for registration.
     */
    public void sendDataForSignUp(View view){
        //Prendo i dati dalla form:
        EditText nome = findViewById(R.id.nome);
        EditText cognome = findViewById(R.id.cognome);
        EditText dataDinascita = findViewById(R.id.data_nascita);
        EditText telefono = findViewById(R.id.telefono);
        EditText username = findViewById(R.id.username);
        EditText mail = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        EditText passwordr = findViewById(R.id.repPass);

        //Converto i dati in stringa per inviarli al server
        String nomes = nome.getText().toString();
        String cognomes = cognome.getText().toString();
        String dataDinascitas = dataDinascita.getText().toString();
        String telefonos = telefono.getText().toString();
        String usernames = username.getText().toString();
        String mails = mail.getText().toString();
        String passwords = password.getText().toString();
        String passwordrs = passwordr.getText().toString();

        if (passwords.compareTo(passwordrs) != 0 || passwords.length() < 1)
        {
            Toast.makeText(this, "ERRORE:\nLe password non corrispondenti.", Toast.LENGTH_LONG).show();
            return;
        }
        else
            if (nomes.length() < 1 || cognomes.length() < 1 || dataDinascitas.length() < 1 || telefonos.length() < 1 || usernames.length() < 1 || mails.length() < 1)
            {
                Toast.makeText(this, "ERRORE:\nDevi compilare tutti i campi.", Toast.LENGTH_LONG).show();
                return;
            }

        try {
            passwords = SHA1(passwords);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "ERRORE:\nimpossibile hashare la password da inviare.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject postData = new JSONObject();
        JSONObject autista = new JSONObject();

        try {
            autista.put("username", usernames);
            autista.put("password", passwords);
            autista.put("email", mails);
            autista.put("nome", nomes);
            autista.put("cognome", cognomes);
            autista.put("dataDiNascita", dataDinascitas);
            autista.put("telefono", telefonos);
            postData.put("autista", autista);
        }catch (Exception e){
            Toast.makeText(this, "ERRORE:\nimpossibile leggere i campi appena compilati.", Toast.LENGTH_LONG).show();
            return;
        }


        // Avverto l'utente del tentativo di invio dei dati di login al server
        caricamento = ProgressDialog.show(SignUpActivity.this, "",
                "Connessione con il server in corso...", true);
        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(this);
        conn.execute(Parametri.IP + "/signup");
    }


    /**
     * This method allow user to return to login activity
     */
    public void returnToLogin(View view){
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    //Criptazione SHA1
    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash;
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    //Funzione per criptazione SHA1
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                }
                else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    @Override
    public void ResultResponse(String responseCode, String result) {
        if (responseCode == null) {
            caricamento.dismiss();
            Toast.makeText(getApplicationContext(), "ERRORE:\nConnessione Assente o server offline.", Toast.LENGTH_LONG).show();
            return;
        }

        if (responseCode.equals("400")) {
            String message = Connessione.estraiErrore(result);
            caricamento.dismiss();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            return;
        }

        if (responseCode.equals("200")) {
            String message = Connessione.estraiSuccessful(result);
            caricamento.dismiss();
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }
}
