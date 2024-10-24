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
        // Extraer datos del mensaje
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String type = data.get("type");
        String pedidoId = data.get("pedidoId");

        // Crear el intent según el tipo
        Intent intent;
        switch (type) {
            case NotificationType.NUEVO_PEDIDO:
                intent = new Intent(this, AdminResPedidosActivity.class);
                break;
            case NotificationType.PEDIDO_EN_CAMINO:
            case NotificationType.PEDIDO_PREPARACION:
                intent = new Intent(this, ClienteTrackingActivity.class);
                break;
            case NotificationType.PEDIDO_ASIGNADO:
                intent = new Intent(this, RepartidorVerOrdenActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }

        // Añadir pedidoId al intent si existe
        if (pedidoId != null) {
            intent.putExtra("pedidoId", pedidoId);
        }

        // Crear y mostrar la notificación
        createNotificationChannel();
        mostrarNotificacion(title, body, intent);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "----------------------------------------");
        Log.d("FCM_TOKEN", "Nuevo token: " + token);
        Log.d("FCM_TOKEN", "----------------------------------------");

        // Guardar el token usando TokenManager
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        tokenManager.guardarToken(token);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Canal notificaciones default",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para notificaciones con prioridad default");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void mostrarNotificacion(String title, String body, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(
                    (int) System.currentTimeMillis(),
                    builder.build()
            );
        }
    }
}