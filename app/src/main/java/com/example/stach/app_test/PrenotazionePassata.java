package com.example.stach.app_test;

import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PrenotazionePassata {
    private Date data;
    private int id;
    private int idParcheggio;
    private int idTipo;
    private int permanenza;

    public PrenotazionePassata(int id, Date data, int idParcheggio, int idTipo, int permanenza) throws Exception {
        if (data == null)
            throw new Exception("Data scadenza non può essere null.");

        this.id = id;
        this.data = data;
        this.idParcheggio = idParcheggio;
        this.idTipo = idTipo;
        this.permanenza = permanenza;
    }

    public PrenotazionePassata(String prenotazione) throws Exception {
        JSONObject jobj = new JSONObject(prenotazione);

        this.id = jobj.getInt("idPrenotazione");
        this.idParcheggio = jobj.getInt("idParcheggio");
        this.idTipo = jobj.getInt("tipoParcheggio");
        this.data = stringToDate(jobj.getString("data"), "yyyy-MM-dd");
        if (data == null)
            throw new Exception("Formato data prenotazione errato.");

        this.permanenza = jobj.getInt("minutiPermanenza");
    }

    public int getId() {
        return id;
    }

    public Date getData() {
        return data;
    }

    public int getIdParcheggio() {
        return idParcheggio;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public int getOrePermanenza() {
        return permanenza;
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
}
