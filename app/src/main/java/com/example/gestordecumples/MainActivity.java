package com.example.gestordecumples;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TimePicker;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gestordecumples.helpers.alarm.AlarmUtils;
import com.example.gestordecumples.models.Contact;
import com.example.gestordecumples.helpers.ContactHelper;
import com.example.gestordecumples.helpers.DBHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_NOTIFICATIONS_PERMISSION = 2;
    private static final int REQUEST_SMS_PERMISSION = 3;
    private DBHelper dBHelper;
    private int alarmID = 1;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        ActionBar ab = getSupportActionBar();
        if (ab != null){

            ab.setDisplayShowHomeEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher_round);
            ab.setDisplayUseLogoEnabled(true);
        }

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        }
        else{
            inicializarBD();
            mostrarListaContactos();
        }
    }

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
            MainActivity.this.startActivity(i);
        });
    }

    private void confFelicitaciones(){

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String finalHour, finalMinute;

                finalHour = "" + selectedHour;
                finalMinute = "" + selectedMinute;
                if (selectedHour < 10) finalHour = "0" + selectedHour;
                if (selectedMinute < 10) finalMinute = "0" + selectedMinute;

                Calendar today = Calendar.getInstance();

                today.set(Calendar.HOUR_OF_DAY, selectedHour);
                today.set(Calendar.MINUTE, selectedMinute);
                today.set(Calendar.SECOND, 0);

                SharedPreferences.Editor edit = settings.edit();
                edit.putString("hour", finalHour);
                edit.putString("minute", finalMinute);

                //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
                edit.putInt("alarmID", alarmID);
                edit.putLong("alarmTime", today.getTimeInMillis());

                edit.commit();

                Toast.makeText(MainActivity.this, getString(R.string.changed_to, finalHour + ":" + finalMinute), Toast.LENGTH_LONG).show();

                AlarmUtils.setAlarm(alarmID, today.getTimeInMillis(), MainActivity.this);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();
    }
}