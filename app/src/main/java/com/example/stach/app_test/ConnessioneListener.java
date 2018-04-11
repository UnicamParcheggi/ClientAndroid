package com.example.stach.app_test;

// Interfaccia per gestire eventi tra il Connessione e i fragment
public interface ConnessioneListener {
    void ResultResponse(String responseCode, String result);
}
