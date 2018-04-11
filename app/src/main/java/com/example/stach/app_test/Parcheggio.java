package com.example.stach.app_test;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;

public class Parcheggio {
    private int id;
    private String indirizzo;
    // Formato indirizzo con stringhe tutte a capo
    private String indirizzo_format;
    private LatLng coordinate;

    private int[] postiLiberi;

    // Prezzi orari standard del parcheggio
    private double prezzoFestivi;
    private double prezzoLavorativi;

    // Indirizzo mac bluetooth parcheggio (potrebbe essere utile cosidernarne più di uno per parcheggio)
    private String macBT;

    // Info restituite da google map
    private String info;

    // Costruttore parcheggio senza posti liberi da stringa formattata in JSon
    public Parcheggio(String JSonobj)throws Exception {
        this.postiLiberi = new int[TipoPosto.N_POSTI];

        // Estraggo tutti i parcheggi
        JSONObject jobj = new JSONObject(JSonobj);
        this.id = jobj.getInt("id");
        JSONObject indr = jobj.getJSONObject("indirizzo");
        this.indirizzo = indr.getString("via") + ", " + indr.getString("n_civico")
                + ", " + indr.getString("cap") + ", " + indr.getString("citta") + " "
                + indr.getString("provincia");
        JSONObject coord = jobj.getJSONObject("coordinate");
        double x = coord.getDouble("x");
        double y = coord.getDouble("y");
        this.prezzoLavorativi = jobj.getDouble("tariffaOrariaLavorativi");
        this.prezzoFestivi = jobj.getDouble("tariffaOrariaFestivi");
        this.coordinate = new LatLng(x, y);
        this.macBT = jobj.getString("macBT");

        this.postiLiberi[TipoPosto.AUTO] = jobj.getInt("nPostiMacchina");
        this.postiLiberi[TipoPosto.AUTOBUS] = jobj.getInt("nPostiAutobus");
        this.postiLiberi[TipoPosto.CAMPER] = jobj.getInt("nPostiCamper");
        this.postiLiberi[TipoPosto.MOTO] = jobj.getInt("nPostiMoto");
        this.postiLiberi[TipoPosto.DISABILE] = jobj.getInt("nPostiDisabile");

        // Setting dell' indirizzo con il secondo tipo di formato
        this.indirizzo_format = "Città: " + indr.getString("citta") + "\n" +"Provincia: "+ indr.getString("provincia")
                + "\n" + "Via: "+ indr.getString("via") + "\n" + "CAP: " + indr.getString("cap") ;

        this.info = "";
    }

    public int getId() {
        return id;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }


    public int[] getPostiLiberi() {
        return postiLiberi;
    }

    public void setPostiLiberi(int[] postiLiberi) throws Exception {
        if (postiLiberi.length != TipoPosto.N_POSTI)
            throw new Exception("Numero posti liberi errato.");
        else
            for (int i = 0; i < TipoPosto.N_POSTI; i++) {
                this.postiLiberi[i] = postiLiberi[i];
            }
    }

    public double getPrezzoFestivi() {
        return prezzoFestivi;
    }

    public double getPrezzoLavorativi() {
        return prezzoLavorativi;
    }

    public String getIndirizzo_format(){return indirizzo_format;}

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getMacBT () {
        return this.macBT;
    }
}
