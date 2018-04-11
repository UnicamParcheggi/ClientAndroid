package com.example.stach.app_test;

import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PrenotazioneDaPagare {

    private Date dataIngresso;
    private int id;
    private int idParcheggio;
    private int idTipo;
    private String codice;

    public PrenotazioneDaPagare(int id, Date dataIngresso, int idParcheggio, int idTipo, String codice) throws Exception {
        String format = "yyyy-MM-dd HH:mm:ss";
        this.dataIngresso = stringToDate((String) android.text.format.DateFormat.format(format, dataIngresso), format);
        if (this.dataIngresso == null)
            throw new Exception("La data d'ingresso non può essere null.");

        this.id = id;
        this.idParcheggio = idParcheggio;
        this.idTipo = idTipo;
        this.codice = codice;
    }

    public PrenotazioneDaPagare(String prenotazione) throws Exception {
        JSONObject jobj = new JSONObject(prenotazione);

        this.id = jobj.getInt("idPrenotazione");
        this.idParcheggio = jobj.getInt("idParcheggio");
        this.idTipo = jobj.getInt("tipoParcheggio");
        this.dataIngresso = stringToDate(jobj.getString("dataIngresso"), "yyyy-MM-dd HH:mm:ss");

        if (dataIngresso == null)
            throw new Exception("Formato data prenotazione errato.");

        this.codice = jobj.getString("codice");
    }

    public int getId() {
        return id;
    }

    public Date getDataIngresso() {
        return dataIngresso;
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

    public long getMinutiPermanenza() {
        Date now = new Date();
        return (((now.getTime() - dataIngresso.getTime()) / 1000) / 60);
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
