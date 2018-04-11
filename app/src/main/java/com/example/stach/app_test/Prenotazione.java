package com.example.stach.app_test;

import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Prenotazione {
    private Date scadenza;
    private int id;
    private int idParcheggio;
    private int idTipo;
    private String codice;
    private boolean avvisata;

    public Prenotazione(int id, Date scadenza, int idParcheggio, int idTipo, String codice) throws Exception {
        if (scadenza == null)
            throw new Exception("Data scadenza non può essere null.");

        this.id = id;
        this.scadenza = scadenza;
        this.idParcheggio = idParcheggio;
        this.idTipo = idTipo;
        this.codice = codice;
        this.avvisata = false;
    }

    public Prenotazione(String prenotazione) throws Exception {
        JSONObject jobj = new JSONObject(prenotazione);

        this.id = jobj.getInt("idPrenotazione");
        this.idParcheggio = jobj.getInt("idParcheggio");
        this.idTipo = jobj.getInt("idPosto");
        this.scadenza = stringToDate(jobj.getString("data"), "yyyy-MM-dd HH:mm:ss");
        if (scadenza == null)
            throw new Exception("Formato data scadenza prenotazione errato.");

        this.codice = jobj.getString("codice");
        this.avvisata = false;
    }

    public int getId() {
        return id;
    }

    public Date getScadenza() {
        return scadenza;
    }

    public int getIdParcheggio() {
        return idParcheggio;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public String getCodice() {
        return codice;
    }

    public long getTempoScadenza() {
        Date now = new Date();
        return scadenza.getTime() - now.getTime();
    }

    private Date stringToDate(String data, String format) {
        Date stringDate = null;

        if (data == null)
            return null;

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
        simpledateformat.setTimeZone(tz);

        try {
            stringDate = simpledateformat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stringDate;
    }

    public boolean isAlreadyNotified() {
        return avvisata;
    }

    public void Notified() {
        avvisata = true;
    }
}
