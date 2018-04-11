package com.example.stach.app_test;

import android.location.Location;
import java.io.File;
import java.util.List;

public class Parametri {
    // Dati server e connessione
    static String IP = "http://2.226.207.189:5666";
    static String UUIDPARKING = "00001101-0000-1000-8000-00805f9b34fb";
    static String Token = null;
    static File login_file;
    static File advance_setting_file;

    // Dati account utente
    static String id = null;
    static String username = null;
    static String email = null;
    static String password = null;
    static String nome = null;
    static String cognome = null;
    static String data_nascita = null;
    static String telefono = null;
    static String saldo = null;
    static Location lastKnowPosition = null;
    static int TEMPO_AVVISO = 5 * 60 * 1000; // 5 minuti
    static int TEMPO_EXTRA = 20 * 60 * 1000; // 20 minuti

    // Dati carta di credito
    static String numero_carta = null;
    static String data_di_scadenza = null;
    static String pin = null;

    // Parcheggi e prenotazioni
    static List<Parcheggio> parcheggi = null;
    static List<Parcheggio> parcheggi_vicini = null;
    static List<Prenotazione> prenotazioniInCorso = null;
    static List<PrenotazionePassata> prenotazioniVecchie = null;
    static List<PrenotazioneDaPagare> prenotazioniDaPagare = null;

    static public void resetAllParametri() {
        Token = null;
        parcheggi = null;
        parcheggi_vicini = null;
        prenotazioniInCorso = null;
        prenotazioniVecchie = null;
        prenotazioniDaPagare = null;
        lastKnowPosition = null;
        id = null;
        username = null;
        email = null;
        password = null;
        nome = null;
        cognome = null;
        data_nascita = null;
        telefono = null;
        saldo = null;
        numero_carta = null;
        data_di_scadenza = null;
        pin = null;
    }
}
