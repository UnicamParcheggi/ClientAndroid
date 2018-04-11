package com.example.stach.app_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ImpostazioniAvanzate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impostazioni_avanzate);

        EditText editT = findViewById(R.id.editTextServerIP);
        editT.setText(Parametri.IP);

        Button btnSend = findViewById(R.id.buttonSalvaAdvanceImpostazioni);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Salva();
            }
        });
    }

    private void Salva() {
        EditText editT = findViewById(R.id.editTextServerIP);
        Parametri.IP = editT.getText().toString();

        try {
            BufferedWriter fos = new BufferedWriter(new FileWriter(Parametri.advance_setting_file.getAbsolutePath()));
            fos.write(Parametri.IP + "\n");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ImpostazioniAvanzate.this, "Errore! Impossibile salvare il file delle impostazioni avanzate.", Toast.LENGTH_LONG);
            return;
        }

        finish();
    }
}
