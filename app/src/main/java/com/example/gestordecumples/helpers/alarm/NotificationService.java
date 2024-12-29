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
    private static final int NOTIFICATION_ID = 1;

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        super("SERVICE");
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        // Llamar a la función gestionarAviso antes de crear la notificación
        gestionarAviso(context);
    }

    private void gestionarAviso(Context context) {
        DBHelper bd = new DBHelper(context);
        Optional<ArrayList<Contact>> opList = bd.quienCumpleHoy();
        bd.close();

        HashSet<String> setNombres = new HashSet<>();
        HashSet<Contact> setContacts = new HashSet<>();

        opList.ifPresent(contactos -> contactos
                .stream()
                .peek(con -> setNombres.add(con.getNombre()))
                .forEach(c -> {
                    if (c.getTipoNotif().equals("1")) {
                        setContacts.add(c);
                    }
                }));

        String nombres = setNombres.stream().collect(Collectors.joining(", "));
        mandarNotificacion(context, nombres);
        mandarSms(context, setContacts);
    }

    private void mandarNotificacion(Context context, String contactos) {
        String canalId = getString(R.string.app_name);
        CharSequence name = "Canal";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(canalId, name, importance);
        channel.setDescription("Canal predeterminado para notificaciones");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.torta)
                .setContentTitle("Cumpleaños Helper")
                .setContentText("Despliega para ver todo el texto")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Feliz cumpleaños a l@s siguientes afortunad@s: " + contactos))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.torta))
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);

        // Iniciar el servicio en primer plano con la notificación
        startForeground(NOTIFICATION_ID, notification);
    }

    private void mandarSms(Context context, HashSet<Contact> mapContactos) {
        try {
            mapContactos.stream()
                    .filter(c -> (c.getTelefono() != null) && (!c.getTelefono().isEmpty()))
                    .forEach(co -> {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(co.getTelefono(), null, "Mensaje: " + co.getMensaje(), null, null);
                        System.out.println("Enviado mensaje en método mandarSms");
                        Toast.makeText(context.getApplicationContext(), "SMS enviado a " + co.getNombre(), Toast.LENGTH_SHORT).show();
                    });

            Toast t = Toast.makeText(context.getApplicationContext(), "SMS enviados, compruebe en la mensajería de su Telf", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            View v = t.getView();
            v.setBackgroundColor(Color.parseColor("#ababab"));
            t.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
