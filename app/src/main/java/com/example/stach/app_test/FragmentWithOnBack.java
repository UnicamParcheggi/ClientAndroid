package com.example.stach.app_test;

import android.support.v4.app.Fragment;

// Classe dei fragment con la gestione del tasto onBack tramite MainActivity
public class FragmentWithOnBack extends Fragment {
    public boolean onBackPressed() {
        return false;
    }
}
