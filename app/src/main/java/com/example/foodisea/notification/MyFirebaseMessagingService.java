package com.example.foodisea.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.adminRes.AdminResPedidosActivity;
import com.example.foodisea.activity.cliente.ClienteCompraDetailsActivity;
import com.example.foodisea.activity.cliente.ClienteConfirmacionEntregaActivity;
import com.example.foodisea.activity.cliente.ClienteTrackingActivity;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.activity.login.StartAppActivity;
import com.example.foodisea.activity.repartidor.RepartidorConfirmacionEntregaActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "delivery_notifications";
    private static final String CHANNEL_NAME = "Delivery";
    private static final String CHANNEL_DESC = "Notificaciones de la app de delivery";
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = SessionManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Verificar si el usuario está logueado antes de procesar mensajes
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuario no logueado, ignorando mensaje");
            return;
        }

        // Manejar datos personalizados
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Manejar notificación
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            mostrarNotificacion(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // Intentar actualizar usando SessionManager primero
        if (sessionManager.isLoggedIn()) {
            sessionManager.actualizarTokenFCM(token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token actualizado exitosamente via SessionManager"))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error actualizando token via SessionManager", e);
                        // Si falla, intentar actualizar directamente
                        guardarTokenEnFirestore(token);
                    });
        } else {
            // Si no hay sesión activa, guardar directamente
            guardarTokenEnFirestore(token);
        }
    }

    private void guardarTokenEnFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token actualizado exitosamente en Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error actualizando token en Firestore", e));
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String tipo = data.get("tipo");
        if (tipo == null) return;

        String titulo = "";
        String mensaje = "";
        Intent intent = null;

        Usuario usuarioActual = sessionManager.getUsuarioActual();
        String tipoUsuario = usuarioActual != null ? usuarioActual.getTipoUsuario() : "";

        switch (tipo) {
            case "cambio_estado_pedido":
                if ("Cliente".equals(tipoUsuario)) {
                    titulo = "Actualización de pedido";
                    mensaje = "El estado de tu pedido ha cambiado a: " + data.get("estadoNuevo");
                    String pedidoId = data.get("pedidoId");
                    if (pedidoId != null) {
                        intent = new Intent(this, ClienteCompraDetailsActivity.class);
                        intent.putExtra("pedidoId", pedidoId);
                    }
                }
                break;

            case "asignacion_repartidor":
                if ("Cliente".equals(tipoUsuario)) {
                    titulo = "Repartidor Asignado";
                    mensaje = "Un repartidor ha sido asignado a tu pedido";
                    String pedidoId = data.get("pedidoId");
                    if (pedidoId != null) {
                        intent = new Intent(this, ClienteTrackingActivity.class);
                        intent.putExtra("pedidoId", pedidoId);
                    }
                }
                break;

            case "nuevo_pedido_disponible":
                if ("Repartidor".equals(tipoUsuario)) {
                    titulo = "Nuevo Pedido Disponible";
                    mensaje = "Hay un nuevo pedido para entregar cerca de tu ubicación";
                    String pedidoId = data.get("pedidoId");
                    if (pedidoId != null) {
                        intent = new Intent(this, RepartidorMainActivity.class);
                        intent.putExtra("pedidoId", pedidoId);
                        intent.putExtra("abrirPedido", true); // Flag para abrir directamente el pedido
                    }
                }
                break;

            case "nuevo_pedido_restaurante":
                if ("AdministradorRestaurante".equals(tipoUsuario)) {
                    titulo = "Nuevo Pedido";
                    mensaje = "Has recibido un nuevo pedido en tu restaurante";
                    String pedidoId = data.get("pedidoId");
                    if (pedidoId != null) {
                        intent = new Intent(this, AdminResPedidosActivity.class);
                        intent.putExtra("pedidoId", pedidoId);
                        intent.putExtra("abrirPedido", true);
                    }
                }
                break;

            case "verificacion_completa":
                String rol = data.get("rol");
                String pedidoId = data.get("pedidoId");
                String destino = data.get("destino");

                if ("CLIENTE".equals(rol) && "Cliente".equals(tipoUsuario)) {
                    titulo = "¡Pedido completado!";
                    mensaje = "La entrega y el pago han sido confirmados";
                    intent = new Intent(this, ClienteConfirmacionEntregaActivity.class);
                    intent.putExtra("pedidoId", pedidoId);
                } else if ("REPARTIDOR".equals(rol) && "Repartidor".equals(tipoUsuario)) {
                    titulo = "¡Pedido completado!";
                    mensaje = "La entrega y el pago han sido confirmados";
                    intent = new Intent(this, RepartidorConfirmacionEntregaActivity.class);
                    intent.putExtra("pedidoId", pedidoId);
                }
                break;

        }

        // Si no se definió un intent específico o el tipo de usuario no corresponde
        if (intent == null) {
            intent = new Intent(this, StartAppActivity.class);
        }

        mostrarNotificacion(titulo, mensaje, data, intent);
    }

    private void mostrarNotificacion(String titulo, String mensaje, Map<String, String> data) {
        mostrarNotificacion(titulo, mensaje, data, new Intent(this, StartAppActivity.class));
    }

    private void mostrarNotificacion(String titulo, String mensaje, Map<String, String> data, Intent intent) {
        // No mostrar notificación si está vacía
        if (titulo.isEmpty() || mensaje.isEmpty()) {
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Agregar datos extras al intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        // Generar ID único para cada notificación basado en el timestamp
        int notificationId = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        createNotificationChannel();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(titulo)
                        .setContentText(mensaje)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}