package com.example.stach.app_test;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;

public class PasswordRecoveryActivity extends AppCompatActivity implements ConnessioneListener {
    ProgressDialog caricamento = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);
    }

    public void sendDataForPasswordRecovery(View view){
        //Prendo i dati dalla form:
        EditText email = findViewById(R.id.editTextEmail);

        String emails = email.getText().toString();

        if (emails.length() < 1)
        {
            Toast.makeText(this, "ERRORE:\nInserire un email prima di procedere.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject postData = new JSONObject();

        try {
            postData.put("email", emails);
        }catch (Exception e){
            // Gestire l'errore
        }


        // Avverto l'utente del tentativo di invio dei dati per il reset della password al server
        caricamento = ProgressDialog.show(PasswordRecoveryActivity.this, "",
                "Connessione con il server in corso...", true);

        Connessione conn = new Connessione(postData, "POST");
        conn.addListener(this);
        conn.execute(Parametri.IP + "/resetPassword");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PasswordRecoveryActivity.this, LoginActivity.class));
        finish();
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
