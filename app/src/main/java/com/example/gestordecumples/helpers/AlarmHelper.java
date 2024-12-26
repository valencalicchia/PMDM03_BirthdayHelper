package com.example.gestordecumples.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.gestordecumples.models.Contact;
import com.example.gestordecumples.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class AlarmHelper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Alarma","Recibida");
        gestionarAviso(context);
    }

    private void gestionarAviso(Context context){

        DBHelper bd = new DBHelper(context);
        Optional<ArrayList<Contact>> opList = bd.quienCumpleHoy();
        bd.close();

        HashSet<String> setNombres = new HashSet<>();
        HashSet<Contact> setContacts = new HashSet<>();

        opList.ifPresent(contactos -> contactos
                .stream()
                .peek( con -> setNombres.add(con.getNombre())) //Como mínimo se muestra notificación
                .forEach(c -> {
                    if (c.getTipoNotif().equals("1")) {
                        setContacts.add(c);
                    }
                }));

        String nombres = setNombres.stream().collect(Collectors.joining(", "));
        mandarNotificacion(context, nombres);
        mandarSms(context, setContacts);
    }

    private void mandarNotificacion(Context context, String contactos){

        String canalId = "CHANNEL_ID_101";
        //CharSequence name = context.getString(R.string.canal_notificacion);
        CharSequence name = "Canal";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(canalId, name, importance);
        channel.setDescription("Canal predeterminado para notificaciones");

        Notification notify =  new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.torta)
                .setContentTitle("Cumpleaños Helper")
                .setContentText("Despliega para ver todo el texto")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Feliz cumpleaños a l@s siguientes afortunad@s: "+ contactos))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.torta))
                .build();

        //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager notificationManager =  context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(777, notify);
    }

    private void mandarSms(Context context, HashSet<Contact> mapContactos){
        try{
            mapContactos
                    .stream()
                    .filter( c -> (c.getTelefono() != null)  &&  (!c.getTelefono().isEmpty()) )
                    .forEach( co ->{
                        SmsManager smsManager=SmsManager.getDefault();
                        smsManager.sendTextMessage(co.getTelefono(),null,"Mensaje: " + co.getMensaje(),null,null);
                        System.out.println("Enviado mensaje en método mandarSms");
                        Toast.makeText(context.getApplicationContext(),"SMS enviado a " + co.getNombre(),Toast.LENGTH_SHORT).show();
                    });

            Toast t = Toast.makeText(context.getApplicationContext(),"SMS enviados, compruebe en la mensajería de su Telf",Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER,0,0);
            View v = t.getView();
            v.setBackgroundColor(Color.parseColor("#ababab"));
            t.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
