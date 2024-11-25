package com.example.foodisea.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.foodisea.R;
import com.example.foodisea.model.Pedido;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationHelper {
    public static final String ADMIN_CHANNEL_ID = "admin_channel";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/foodisea-f50e9/messages:send";

    private final Context context;
    private final FirebaseFirestore db;
    private final ExecutorService executorService;
    private String accessToken;
    private boolean isInitializing = false;

    public NotificationHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        createNotificationChannels();
        // Inicializar en segundo plano
        initializeAdminSDKAsync();
    }

    private void initializeAdminSDKAsync() {
        if (isInitializing) return;
        isInitializing = true;

        executorService.execute(() -> {
            try {
                InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                        .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));

                credentials.refresh();
                accessToken = credentials.getAccessToken().getTokenValue();

                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d("NotificationHelper", "SDK inicializado exitosamente");
                });
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.e("NotificationHelper", "Error inicializando credenciales", e);
                });
            } finally {
                isInitializing = false;
            }
        });
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel adminChannel = new NotificationChannel(
                    ADMIN_CHANNEL_ID,
                    "Notificaciones Administrador",
                    NotificationManager.IMPORTANCE_HIGH
            );
            adminChannel.setDescription("Notificaciones para administradores de restaurante");
            adminChannel.enableLights(true);
            adminChannel.enableVibration(true);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public void sendNewOrderNotification(Pedido pedido) {
        // Si el token no está listo, inicializar primero
        if (accessToken == null) {
            initializeAdminSDKAsync();
            // Esperar un poco antes de intentar enviar
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                sendNewOrderNotificationInternal(pedido);
            }, 2000);
        } else {
            sendNewOrderNotificationInternal(pedido);
        }
    }

    private void sendNewOrderNotificationInternal(Pedido pedido) {
        db.collection("restaurantes")
                .document(pedido.getRestauranteId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String adminFcmToken = documentSnapshot.getString("adminFcmToken");
                        if (adminFcmToken != null) {
                            sendFcmNotification(
                                    adminFcmToken,
                                    "Nuevo Pedido",
                                    "Pedido #" + pedido.getId() + " recibido",
                                    pedido.getId()
                            );
                        }
                    }
                });
    }

    private void sendFcmNotification(String token, String title, String message, String orderId) {
        // Verificar si necesitamos refrescar el token
        if (accessToken == null) {
            initializeAdminSDKAsync();
        }

        executorService.execute(() -> {
            try {
                // Crear el objeto JSON para la notificación
                JSONObject messageObj = new JSONObject();
                messageObj.put("token", token);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", message);
                messageObj.put("notification", notification);

                JSONObject data = new JSONObject();
                data.put("orderId", orderId);
                data.put("type", "NEW_ORDER");
                data.put("click_action", "OPEN_ORDER_DETAIL");
                messageObj.put("data", data);

                JSONObject root = new JSONObject();
                root.put("message", messageObj);

                // Configurar la conexión HTTP
                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; UTF-8");
                conn.setDoOutput(true);

                // Enviar la notificación
                try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                    writer.write(root.toString());
                    writer.flush();
                }

                // Leer la respuesta
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d("NotificationHelper", "Notificación enviada: " + response.toString());
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.e("NotificationHelper", "Error enviando notificación", e);
                });
            }
        });
    }

    public void updateFcmToken(String userId, String token) {
        db.collection("usuarios")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid ->
                        Log.d("NotificationHelper", "Token FCM actualizado"))
                .addOnFailureListener(e ->
                        Log.e("NotificationHelper", "Error actualizando token", e));
    }

    // Método para actualizar el token de un restaurante específicamente
    public void updateRestaurantFcmToken(String restaurantId, String token) {
        db.collection("restaurantes")
                .document(restaurantId)
                .update("adminFcmToken", token)
                .addOnSuccessListener(aVoid ->
                        Log.d("NotificationHelper", "Token FCM del restaurante actualizado"))
                .addOnFailureListener(e ->
                        Log.e("NotificationHelper", "Error actualizando token del restaurante", e));
    }

    // Método para manejar renovación manual del token de acceso
    public void refreshAccessToken() {
        initializeAdminSDKAsync();
    }

    // Método para liberar recursos
    public void shutdown() {
        executorService.shutdown();
    }

    // Método para obtener el Channel ID según el tipo de usuario
    public String getChannelId(String userType) {
        // Por ahora solo tenemos el canal de admin, pero podemos expandir
        return ADMIN_CHANNEL_ID;
    }
}