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

public class ContactHelper extends ArrayAdapter<Contact> {

    private final LayoutInflater inflater;

    public ContactHelper(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_row, parent, false);

            holder = new ViewHolder();
            holder.imagen = convertView.findViewById(R.id.imagenContacto);
            holder.tvNombre = convertView.findViewById(R.id.txtContactoNombre);
            holder.tvDatos = convertView.findViewById(R.id.txtDatos);
            holder.tvNotificacion = convertView.findViewById(R.id.txtContactoNotificacion);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contacto = getItem(position);

        if (contacto != null && contacto.getRutaImagen() != null) {
            holder.imagen.setImageURI(Uri.parse(contacto.getRutaImagen().toString()));
        }

        holder.imagen.setAdjustViewBounds(true);
        holder.imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        holder.tvNombre.setText(contacto != null ? contacto.getNombre() : "N/A");

        if (contacto != null) {
            String datos = String.format("%s    %s",
                    contacto.getFechaNacimiento() != null ? contacto.getFechaNacimiento() : "N/A",
                    contacto.getTelefono() != null ? contacto.getTelefono() : "N/A");
            holder.tvDatos.setText(datos);

            int notiTxtResId = "1".equals(contacto.getTipoNotif()) ? R.string.sms : R.string.solo_noti;
            holder.tvNotificacion.setText(notiTxtResId);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imagen;
        TextView tvNombre;
        TextView tvDatos;
        TextView tvNotificacion;
    }
}
