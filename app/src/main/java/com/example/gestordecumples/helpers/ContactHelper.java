package com.example.gestordecumples.helpers;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.gestordecumples.models.Contact;
import com.example.gestordecumples.R;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContactHelper extends ArrayAdapter<Contact> {

    private List<Contact> listContactos;

    public ContactHelper(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
        listContactos = objects;
    }

    @Override
    public View getView(int position, View cnvtView, ViewGroup prnt){
        return crearFilaPersonalizada(position, cnvtView, prnt);
    }

    public View crearFilaPersonalizada(int position, View convertView, ViewGroup parent){

        LayoutInflater inflador = LayoutInflater.from(getContext());
        View filaContacto = inflador.inflate(R.layout.contact_row, parent, false);

        ImageView imagen = (ImageView) filaContacto.findViewById(R.id.imagenContacto);
        if (listContactos.get(position).getRutaImagen() != null){
            imagen.setImageURI(Uri.parse(listContactos.get(position).getRutaImagen().toString()));//android.net.Uri not Serializable - java.net.URI SI serializable
        }else{
            //imagen.setImageResource(R.drawable.support_icon_512px);
        }
        imagen.setAdjustViewBounds(true);
        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView tvNombre = (TextView) filaContacto.findViewById(R.id.txtContactoNombre);
        tvNombre.setText(listContactos.get(position).getNombre());

        TextView tvDatos = (TextView) filaContacto.findViewById(R.id.txtDatos);
        String datos = String.format("%s%s",  Objects.toString(listContactos.get(position).getFechaNacimiento(), "N/A")+"    ", Objects.toString(listContactos.get(position).getTelefono(), "N/A"));
        tvDatos.setText(datos);

        TextView tvNotificacion = (TextView) filaContacto.findViewById(R.id.txtContactoNotificacion);
        String noti = listContactos.get(position).getTipoNotif();

        int notiTxtRstring=1;

        switch (noti){
            case "0":
                notiTxtRstring = R.string.solo_noti;
                break;
            case "1":
                notiTxtRstring = R.string.sms;
                break;
        }
        tvNotificacion.setText(notiTxtRstring);

        return filaContacto;
    }

}
