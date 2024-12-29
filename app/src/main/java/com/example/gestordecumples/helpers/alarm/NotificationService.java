package com.example.gestordecumples.helpers.alarm;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.gestordecumples.MainActivity;
import com.example.gestordecumples.R;
import com.example.gestordecumples.helpers.DBHelper;
import com.example.gestordecumples.models.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class NotificationService extends IntentService {

    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1;
    Notification notification;


    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        super("SERVICE");
    }

    @SuppressLint("ForegroundServiceType")
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getString(R.string.app_name);
        Context context = this.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this, MainActivity.class);
        Resources res = this.getResources();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        String message = getString(R.string.app_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final int NOTIFY_ID = 0;
            String id = NOTIFICATION_CHANNEL_ID;
            String title = NOTIFICATION_CHANNEL_ID;
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifManager == null) {
                notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText(message)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher_foreground))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);

            // Usa un tipo de servicio válido en lugar de NONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);  // Cambio a un tipo permitido
            } else {
                startForeground(1, notification);
            }

        } else {
            pendingIntent = PendingIntent.getActivity(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher_foreground))
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setContentText(message).build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }




    private void gestionarAviso(Context context){

        DBHelper bd = new DBHelper(context);
        Optional<ArrayList<Contact>> opList = bd.quienCumpleHoy();
        bd.close();

        HashSet<String> setNombres = new HashSet<>();
        HashSet<Contact> setContacts = new HashSet<>();

        opList.ifPresent(contactos -> contactos
                .stream()
                .peek( con -> setNombres.add(con.getNombre()))
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