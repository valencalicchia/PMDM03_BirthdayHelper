package com.example.gestordecumples;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

public class ViewContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);

        setupActionBar();

        Intent intent = getIntent();
        Optional<Serializable> optContacto = Optional.ofNullable(intent.getSerializableExtra("contacto"));
        optContacto.ifPresent(contacto -> mostrarContacto((Contact) contacto));
    }

    /**
     * Configura la ActionBar con título y logo.
     */
    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.seccion_editar);
            ab.setDisplayShowHomeEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher_round);
            ab.setDisplayUseLogoEnabled(true);
        }
    }

    /**
     * Muestra los datos del contacto en la interfaz.
     */
    private void mostrarContacto(Contact contacto) {
        // Configuración de campos de texto
        EditText ptNombre = findViewById(R.id.etNombre);
        ptNombre.setText(contacto.getNombre());
        ptNombre.setInputType(InputType.TYPE_NULL); // Campo de solo lectura

        CheckBox cbNotificacion = findViewById(R.id.cbNotificacion);
        cbNotificacion.setChecked("1".equals(contacto.getTipoNotif()));

        // Configuración del spinner con teléfonos
        Spinner sp = findViewById(R.id.spinTelfs);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contacto.getSetTelefonos());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adaptador);
        sp.setSelection(adaptador.getPosition(contacto.getTelefono())); // Selección inicial

        // Configuración de imagen
        ImageView autoRetrato = findViewById(R.id.ivFoto);
        if (contacto.getRutaImagen() != null) {
            autoRetrato.setImageURI(Uri.parse(contacto.getRutaImagen().toString()));
        }
        autoRetrato.setAdjustViewBounds(true);
        autoRetrato.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Configuración de fecha de nacimiento
        EditText etCumple = findViewById(R.id.etFechaNaci);
        etCumple.setText(contacto.getFechaNacimiento());
        etCumple.setInputType(InputType.TYPE_NULL);
        etCumple.setFocusable(false);

        // Configuración de mensaje
        EditText etMens = findViewById(R.id.etMensaje);
        etMens.setText(contacto.getMensaje());

        // Configuración de botones
        Button btnEditar = findViewById(R.id.btnEditar);
        btnEditar.setOnClickListener(v -> llamarIntentEditarContacto(contacto));

        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardarDatos(contacto));
    }

    /**
     * Llama al intent para editar el contacto en la aplicación de contactos.
     */
    private void llamarIntentEditarContacto(Contact contacto) {
        String lookupKey = obtenerLookUpId(contacto.getId());
        if (lookupKey != null) {
            Uri selectedContactUri = ContactsContract.Contacts.getLookupUri(contacto.getId(), lookupKey);
            Intent editIntent = new Intent(Intent.ACTION_EDIT);
            editIntent.setDataAndType(selectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            editIntent.putExtra("finishActivityOnSaveCompleted", true);
            startActivity(editIntent);
        } else {
            mostrarToast("No se pudo obtener la información del contacto.", true);
        }
    }

    /**
     * Obtiene el LookUpKey del contacto a partir de su ID.
     */
    private String obtenerLookUpId(int contactId) {
        String[] projection = {ContactsContract.Contacts.LOOKUP_KEY};
        String selection = ContactsContract.Contacts._ID + " = ?";
        String[] selectionArgs = {String.valueOf(contactId)};

        try (Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
            }
        }
        return null;
    }

    /**
     * Guarda los datos editados del contacto en la base de datos.
     */
    private void guardarDatos(Contact contacto) {
        HashMap<String, String> mapDatos = new HashMap<>();
        mapDatos.put("ID", String.valueOf(contacto.getId()));

        CheckBox cbNotificacion = findViewById(R.id.cbNotificacion);
        mapDatos.put("TipoNotif", cbNotificacion.isChecked() ? "1" : "0");

        Spinner spTelf = findViewById(R.id.spinTelfs);
        mapDatos.put("Telefono", spTelf.getSelectedItem().toString());

        EditText etCumple = findViewById(R.id.etFechaNaci);
        mapDatos.put("FechaNacimiento", etCumple.getText().toString());

        EditText etMens = findViewById(R.id.etMensaje);
        mapDatos.put("Mensaje", etMens.getText().toString());

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        boolean result = dbHelper.guardarContactoDesdeVerContacto(Optional.of(mapDatos));

        mostrarToast(result ? "Datos guardados correctamente." : "Error al guardar los datos.", !result);
    }

    /**
     * Muestra un mensaje tipo Toast.
     */
    private void mostrarToast(String mensaje, boolean esError) {
        Toast toast = Toast.makeText(this, mensaje, Toast.LENGTH_LONG);
        View view = toast.getView();

        if (view != null) {
            view.setBackgroundColor(esError ? Color.RED : Color.parseColor("#ababab"));
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            toast.show();
        }

        new Handler().postDelayed(() -> {
            if (!esError) {
                Intent intent = new Intent(ViewContactActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, Toast.LENGTH_LONG);
    }
}
