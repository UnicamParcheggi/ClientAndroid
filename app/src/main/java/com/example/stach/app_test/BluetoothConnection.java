package com.example.stach.app_test;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnection extends Thread {
    // Codici per la comunicazione con il server dei parcheggi
    public static String INGRESSO = "<enter-parking>";
    public static String USCITA = "<exit-parking>";
    public static String TERMINA = "<communication-end>";
    public static String ERROR = "<error>";
    public static String SUCCESS = "<successful>";

    private List<BluetoothConnessioneListener> listeners;

    private final UUID UUID_PARKINGSERVER = UUID.fromString(Parametri.UUIDPARKING);
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private BluetoothDevice dev;
    private boolean connected;

    public BluetoothConnection(BluetoothDevice myDevice) {
        dev = myDevice;
        connected = false;
        listeners = new ArrayList<>();
    }

    public void openConnection() {
        this.start();
    }

    // I peremessi saranno controllati dalle classi che utilizzano questa
    public void run() {
        try {
            socket = dev.createRfcommSocketToServiceRecord(UUID_PARKINGSERVER);

            out = socket.getOutputStream();
            in = socket.getInputStream();
            dev = socket.getRemoteDevice();
            socket.connect();
            connected = true;
        } catch (IOException connectException) {
            // Se la connessione fallisce creo una connessione di fallback
            try {
                socket = (BluetoothSocket) dev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(dev, 1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                out = socket.getOutputStream();
                in = socket.getInputStream();
                dev = socket.getRemoteDevice();
                socket.connect();
                connected = true;
            } catch (IOException ce) {
                Close();
                ce.printStackTrace();
            }
        }

        for (BluetoothConnessioneListener l : listeners)
            l.ConnessioneStabilita(connected);
    }

    public void Send(String data) {
        if (!socket.isConnected()) {
            Log.d("send", "socket non connesso");
            return;
        }

        data = data + "\n";

        try {
            out.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Send(int command) {
        if (!socket.isConnected()) {
            Log.d("send", "socket non connesso");
            return;
        }

        try {
            out.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String Receive () throws IOException {
        if (!socket.isConnected()) {
            Log.d("send", "socket non connesso");
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        return reader.readLine();
    }

    public boolean isConnected() {
        return connected;
    }

    public void Close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(BluetoothConnessioneListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(BluetoothConnessioneListener listener) {
        this.listeners.remove(listener);
    }
}
