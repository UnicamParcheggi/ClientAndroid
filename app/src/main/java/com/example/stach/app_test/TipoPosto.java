package com.example.stach.app_test;

// Enumerazione con valori interi per riconoscere il tipo di parcheggio
public class TipoPosto {
    static int AUTO = 0;
    static int CAMPER = 1;
    static int MOTO = 2;
    static int AUTOBUS = 3;
    static int DISABILE = 4;
    static int N_POSTI = 5;

    static public String getNomeTipoPosto(int id) {
        String result;

        if (id == AUTO)
            result = "Auto";
        else if (id == CAMPER)
            result = "Camper";
        else if (id == MOTO)
            result = "Moto";
        else if (id == AUTOBUS)
            result = "Autobus";
        else if (id == DISABILE)
            result = "Disabile";
        else
            result = "Tipo sconosciuto.";

        return result;
    }
}