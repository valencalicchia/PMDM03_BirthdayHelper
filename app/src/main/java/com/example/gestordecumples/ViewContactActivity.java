package com.example.gestordecumples;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestordecumples.models.Contact;
import com.example.gestordecumples.helpers.DBHelper;
import com.example.gestordecumples.helpers.TimeHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

public class ViewContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);

        ActionBar ab = getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.seccion_editar);
            ab.setDisplayShowHomeEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher_round);
            ab.setDisplayUseLogoEnabled(true);
        }

        Intent intent = getIntent();
        Optional<Serializable> optContacto = Optional.ofNullable(intent.getSerializableExtra("contacto"));
        mostrarContacto(optContacto);
    }

    private void mostrarContacto(Optional<Serializable> optContacto){

        if (optContacto.isPresent()){
            Contact contacto = (Contact)optContacto.get();
            EditText ptNombre = findViewById(R.id.etNombre);
            ptNombre.setText(contacto.getNombre());
            ptNombre.setInputType(InputType.TYPE_NULL);

            CheckBox cbNotificacion = findViewById(R.id.cbNotificacion);
            cbNotificacion.setChecked(contacto.getTipoNotif().equals("1"));

            Spinner sp = findViewById(R.id.spinTelfs);
            ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contacto.getSetTelefonos());
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(adaptador);
            sp.setSelection(adaptador.getPosition(contacto.getTelefono()));//El teléfono guardado como principal en la BD saldrá selecciado

            ImageView autoRetrato = findViewById(R.id.ivFoto);
            if( contacto.getRutaImagen() != null){
                autoRetrato.setImageURI(Uri.parse(contacto.getRutaImagen().toString()));
            }else{
                //autoRetrato.setImageResource(R.drawable.support);
            }
            autoRetrato.setAdjustViewBounds(true);
            autoRetrato.setScaleType(ImageView.ScaleType.CENTER_CROP);

            EditText etCumple = findViewById(R.id.etFechaNaci);
            etCumple.setText(contacto.getFechaNacimiento()); ///Recuperar fecha y cargarla en data pikcer???
            etCumple.setInputType(InputType.TYPE_NULL);
            etCumple.setFocusable(false);
            etCumple.setOnClickListener(v -> {
                TimeHelper.DatePickerHelper newFragment = new TimeHelper.DatePickerHelper();
                newFragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener(){

                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        // +1 because January is zero
                        final String selectedDate = day + " / " + (month+1) + " / " + year;
                        etCumple.setText(selectedDate);
                    }
                });

                newFragment.show(getSupportFragmentManager(), "datePicker");
            });


            EditText etMens = findViewById(R.id.etMensaje);
            etMens.setText(contacto.getMensaje());

            Button btnEditar = findViewById(R.id.btnEditar);
            btnEditar.setOnClickListener(v -> llamarIntentEditarContacto(contacto));

            Button btnGuardar = findViewById(R.id.btnGuardar);
            btnGuardar.setOnClickListener(v -> guardarDatos(contacto));
        }
    }

    private void llamarIntentEditarContacto(Contact contacto){

        String mCurrentLookupKey = obtenerLookUpId((contacto.getId()));
        Uri selectedContactUri = ContactsContract.Contacts.getLookupUri(contacto.getId(), mCurrentLookupKey);
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setDataAndType(selectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        editIntent.putExtra("finishActivityOnSaveCompleted", true);
        startActivity(editIntent);
    }


    private String obtenerLookUpId(int idContact ) {

        String[] proyeccion = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY};
        String filtro = ContactsContract.Contacts._ID + " = ?";
        String[] args_filtro = {String.valueOf(idContact)};
        Cursor cur = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, proyeccion, filtro, args_filtro, null);

        String lookUpKey = null;
        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {
                int numLookUpKey = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                lookUpKey = cur.getString(numLookUpKey);
            }
        }
        cur.close();
        return lookUpKey;
    }

    private void guardarDatos(Contact contac){
        HashMap<String,String> mapDatos = new HashMap<>();
        mapDatos.put("ID",String.valueOf(contac.getId()));

        CheckBox cbNotificacion = findViewById(R.id.cbNotificacion);
        String noti;
        if (cbNotificacion.isChecked()){
            noti = "1";
        }else{
            noti = "0";
        }
        mapDatos.put("TipoNotif",noti);

        Spinner spTelf = findViewById(R.id.spinTelfs);
        String telf = "";
        if(spTelf.getSelectedItem() != null){
            telf = spTelf.getSelectedItem().toString();
        }
        mapDatos.put("Telefono",telf);

        EditText etCumple = findViewById(R.id.etFechaNaci);
        String cumple;
        cumple = etCumple.getText().toString();
        mapDatos.put("FechaNacimiento",cumple);

        EditText etMens = findViewById(R.id.etMensaje);
        String mens;
        mens = etMens.getText().toString();
        mapDatos.put("Mensaje",mens);


        DBHelper bd = new DBHelper(getApplicationContext());
        boolean result = bd.guardarContactoDesdeVerContacto(Optional.of(mapDatos));
        Toast t =Toast.makeText(this.getApplicationContext(),"OK",Toast.LENGTH_LONG);
        if (result){
            t = Toast.makeText(this.getApplicationContext(),"OK",Toast.LENGTH_LONG);
        }
        t.setGravity(Gravity.CENTER,0,0);
        View v = t.getView();
        v.setBackgroundColor(Color.parseColor("#ababab"));
        t.show();
    }
}
