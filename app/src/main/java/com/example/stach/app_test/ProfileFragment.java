package com.example.stach.app_test;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ProfileFragment extends FragmentWithOnBack {

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.fragment_profile, container, false);

        if (Parametri.nome != null && Parametri.data_di_scadenza != null) {
            ImageView mImageView;
            TextView t_nome;
            TextView t_cognome;
            TextView t_data;
            TextView t_telefono;
            TextView t_email;
            TextView t_saldo;
            TextView t_user;

            mImageView = view.findViewById(R.id.image_profile);
            mImageView.setImageResource(R.mipmap.ic_profile);

            t_nome = view.findViewById(R.id.Nome);
            t_nome.setText(Parametri.nome);

            t_cognome = view.findViewById(R.id.Cognome);
            t_cognome.setText(Parametri.cognome);
            t_telefono = view.findViewById(R.id.Telefono);
            t_telefono.setText(Parametri.telefono);

            t_data = view.findViewById(R.id.Data_Nascita);
            t_data.setText(DateFormat.format("dd MMMM yyyy", stringToDate(Parametri.data_nascita, "yyyy-MM-dd")).toString());

            t_email = view.findViewById(R.id.Email);
            t_email.setText(Parametri.email);

            t_saldo = view.findViewById(R.id.Saldo);
            t_saldo.setText(Parametri.saldo);

            t_user = view.findViewById(R.id.Username);
            t_user.setText(Parametri.username);

            Button button_cambia_credenziali = view.findViewById((R.id.buttonCambiaCredenziali));

            button_cambia_credenziali.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Cambia_credenziali cambia_credenziali = new Cambia_credenziali();
                    fragmentTransaction.replace(R.id.fram, cambia_credenziali);
                    fragmentTransaction.addToBackStack("Fragment_change_parameters");
                    fragmentTransaction.commit();
                }
            });
        }
        return view;
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

    @Override
    public boolean onBackPressed() {
        getActivity().setTitle("Trova parcheggio");
        FindYourParkingFragment fragment = new FindYourParkingFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, fragment, "Fragment Find Park");
        fragmentTransaction.commit();
        return true;
    }
}
