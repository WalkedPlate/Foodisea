package com.example.foodisea.notificaciones;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResPedidosActivity;
import com.example.foodisea.activity.cliente.ClienteComprobanteQrActivity;
import com.example.foodisea.activity.cliente.ClienteTrackingActivity;
import com.example.foodisea.activity.repartidor.RepartidorVerOrdenActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String channelId = "channelDefaultPri";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title, body, type, pedidoId;

        // Si viene de Firebase Console
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Map<String, String> data = remoteMessage.getData();
            type = data.get("type");
            pedidoId = data.get("pedidoId");
        } else {
            // Si viene de data
            Map<String, String> data = remoteMessage.getData();
            title = data.get("title");
            body = data.get("body");
            type = data.get("type");
            pedidoId = data.get("pedidoId");
        }

        // Verificar el tipo
        if (type == null) {
            type = "DEFAULT";
        }

        // Crear el intent según el tipo
        Intent intent;
        if (type.equals(NotificationType.NUEVO_PEDIDO)) {
            // Para Administrador de Restaurante
            intent = new Intent(this, AdminResPedidosActivity.class);
        } else if (type.equals(NotificationType.PEDIDO_EN_CAMINO) ||
                type.equals(NotificationType.PEDIDO_PREPARACION)) {
            // Para Cliente
            intent = new Intent(this, ClienteTrackingActivity.class);
        } else if (type.equals(NotificationType.PEDIDO_ASIGNADO)) {
            // Para Repartidor
            intent = new Intent(this, RepartidorVerOrdenActivity.class);
        } else {
            // Por defecto a la pantalla principal
            intent = new Intent(this, MainActivity.class);
        }

        // Añadir flags necesarios para abrir la app desde background
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Añadir extras
        if (pedidoId != null) {
            intent.putExtra("pedidoId", pedidoId);
        }

        // Guardar el tipo de notificación
        if (type != null) {
            intent.putExtra("notificationType", type);
        }

        // Crear canal y mostrar notificación
        createNotificationChannel();
        mostrarNotificacion(title, body, intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Canal notificaciones default",
                    NotificationManager.IMPORTANCE_HIGH  // Cambiado a HIGH
            );
            channel.setDescription("Canal para notificaciones con prioridad default");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void mostrarNotificacion(String title, String body, Intent intent) {
        // Valores por defecto si son null
        if (title == null) title = "Nueva notificación";
        if (body == null) body = "Tienes un nuevo mensaje";

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Añade sonido y vibración

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(
                    (int) System.currentTimeMillis(),
                    builder.build()
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "----------------------------------------");
        Log.d("FCM_TOKEN", "Nuevo token: " + token);
        Log.d("FCM_TOKEN", "----------------------------------------");

        // Guardar el token
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        tokenManager.guardarToken(token);
    }
}