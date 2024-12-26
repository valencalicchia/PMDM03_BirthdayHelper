package com.example.gestordecumples;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gestordecumples.models.Contact;
import com.example.gestordecumples.helpers.AlarmHelper;
import com.example.gestordecumples.helpers.ContactHelper;
import com.example.gestordecumples.helpers.DBHelper;
import com.example.gestordecumples.helpers.TimeHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS_PERMISSION = 1;
    private DBHelper dBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        if (ab != null){

            ab.setDisplayShowHomeEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher_round);
            ab.setDisplayUseLogoEnabled(true);
        }

        pedirPermisosParaLeerContactos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.confFeli) {
            confFelicitaciones();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRestart(){
        super.onRestart();

        ListView listView = findViewById(R.id.listViewContactos);
        listView.invalidate();
        mostrarListaContactos();
    }

    private void pedirPermisosParaLeerContactos(){
        //https://developer.android.com/training/permissions/requesting#java

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        } else {

            inicializarBD();
            mostrarListaContactos();
        }
    }

    private boolean comprobarPermisos(String permisoManifiesto){

        return (ContextCompat.checkSelfPermission(this, permisoManifiesto) != PackageManager.PERMISSION_GRANTED);
    }

    //Gestiona la contestación a la petición de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0){

            if (Arrays.stream(grantResults).allMatch(b-> b == PackageManager.PERMISSION_GRANTED)){
                inicializarBD();
                mostrarListaContactos();
            }
        }
    }


    private void inicializarBD(){

        dBHelper = new DBHelper(MainActivity.this);

    }

    private void mostrarListaContactos(){

        if ( !dBHelper.getOptListContactos().isPresent()){
            dBHelper.getAndGenerateOptListContactos();
        }

        ContactHelper adapterContacto = new ContactHelper(this,R.id.listViewContactos,dBHelper.getOptListContactos().orElse(new ArrayList<>()));
        ListView listView = findViewById(R.id.listViewContactos);
        listView.setAdapter(adapterContacto);
        listView.setOnItemClickListener((parent, view, position, id) -> {

            Contact contacto = dBHelper.getOptListContactos().get().get(position);
            Intent i = new Intent(MainActivity.this.getApplicationContext(),ViewContactActivity.class);
            i.putExtra("contacto",contacto);
            Toast.makeText(MainActivity.this.getApplicationContext(), "Llegué."+contacto.getNombre(), Toast.LENGTH_LONG).show();
            MainActivity.this.startActivity(i);
        });
    }

    private void confFelicitaciones(){

        TimeHelper.TimePickerHelper timePickerFragment = new TimeHelper.TimePickerHelper();
        timePickerFragment.setOnTimeSetListener((view, hourOfDay, minute) -> {
            Log.w("NuevoTime","Hora: " + hourOfDay + " <|> Minutos: " + minute);
            confAlarma(hourOfDay,minute);
        });

        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void confAlarma(int hora, int minut){
        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(System.currentTimeMillis());
        calendario.set(Calendar.HOUR_OF_DAY,hora);
        calendario.set(Calendar.MINUTE,minut);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendario.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
    }

}