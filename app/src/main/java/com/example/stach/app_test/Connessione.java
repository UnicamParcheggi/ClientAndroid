package com.example.stach.app_test;

import android.os.AsyncTask;
import org.json.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Connessione extends AsyncTask<String, String, String> {
    private JSONObject postData;
    private String requestType;
    private String responseInfo;


    List<ConnessioneListener> listeners;

    // This is a constructor that allows you to pass in the JSON body
    public Connessione(JSONObject postData, String requestType) {
        this.requestType = requestType;
        this.listeners = new ArrayList<>();
        if (postData != null) {
            this.postData = postData;
        }
    }

    // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
    @Override
    protected String doInBackground(String... params) {
        try {
            // This is getting the url from the string we passed in
            URL url = new URL(params[0]);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", "application/json; charset= utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod(requestType);
            urlConnection.setConnectTimeout(10000);
            // Send the post body
            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }

            int statusCode = urlConnection.getResponseCode();
            InputStreamReader inputStreamReader;

            // Se lo statuscode è 200 leggo la risposta, altrimenti leggo l'errore
            if (statusCode == 200)
                inputStreamReader = new InputStreamReader (urlConnection.getInputStream());
            else    // Leggo dall' ErrorStream
                inputStreamReader = new InputStreamReader (urlConnection.getErrorStream());

            // Leggo la risposta dal server
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String response;



            while ((response = reader.readLine()) != null)
                sb.append(response).append("\n");

            response = sb.toString();
            responseInfo = response;

            //publishProgress(response, String.valueOf(statusCode));

            return String.valueOf(statusCode);
        } catch (Exception e) {
            responseInfo = e.getMessage();
        }

        return null;
    }

    public void addListener(ConnessioneListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void onPostExecute(String result){ // Result è lo status code di ritorno
        for (ConnessioneListener listener: listeners)
            listener.ResultResponse(result, responseInfo);
    }

    /**
     *  Utilizzare solo in caso di dover informare l'utente durante la trasmissione o elaborazione dei dati,
     *  questo significa che l'utente va avvisato di un eventuale risposta solo alla fine della trasmissione
     *  la suddetta risposta deve essere inviata tramite onPostExecute e non qui.
     */
    @Override
    protected void onProgressUpdate(String... progress) {

    }

    // Funzione per l'estrazione automatica del dato JSon error
    public static String estraiErrore(String data){
        String result;

        try {
            JSONObject response = new JSONObject(data);
            JSONObject error = new JSONObject(response.getString("error"));
            result = error.getString("info");
        } catch (Exception e) {
            result = "Impossibile leggere la risposta del server.";
        }

        return result;
    }

    // Funzione per l'estrazione automatica del dato JSon successful
    public static String estraiSuccessful(String data){
        String result;

        try {
            JSONObject response = new JSONObject(data);
            JSONObject error = new JSONObject(response.getString("successful"));
            result = error.getString("info");
        } catch (Exception e) {
            result = "Impossibile leggere la risposta del server.";
        }

        return result;
    }
}