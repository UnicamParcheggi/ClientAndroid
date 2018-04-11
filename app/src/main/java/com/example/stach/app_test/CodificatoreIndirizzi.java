package com.example.stach.app_test;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Codifica delle coordinate in indirizzi
public class CodificatoreIndirizzi {
    private Geocoder geocoder;

    public CodificatoreIndirizzi(Context context) throws Exception {
        if (context != null) {
            geocoder = new Geocoder(context, Locale.getDefault());
        }
        else
            throw new Exception("Context inesistente.");
    }

    // Restituisce l'indirizzo ricavato da location
    public String getIndirizzoFromLocation(Location location) {
        String result;
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            result = "Errore geocoder.";
            return result;
        } catch (IllegalArgumentException illegalArgumentException) {
            result = "Lat o Long errati.";
            return result;
        }

        if (addresses == null || addresses.size() == 0) {
            result = "Indirizzo non trovato.";
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                addressFragments.add(address.getAddressLine(i));

            result = TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }

        return result;
    }
}

