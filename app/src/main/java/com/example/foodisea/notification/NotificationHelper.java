package com.example.foodisea.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodisea.R;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private final FirebaseFirestore db;
    private final Context context;
    private static final String FCM_API = "https://fcm.googleapis.com/v1/projects/foodisea-f50e9/messages:send";
    private String accessToken;
    private final ExecutorService executor;

    public NotificationHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        initializeCredentials();
    }

    private void initializeCredentials() {
        executor.execute(() -> {
            try {
                InputStream serviceAccount = context.getResources().openRawResource(R.raw.service_account);
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(serviceAccount)
                        .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
                googleCredentials.refresh();
                accessToken = googleCredentials.getAccessToken().getTokenValue();
                Log.d(TAG, "Credenciales inicializadas correctamente");
            } catch (IOException e) {
                Log.e(TAG, "Error al inicializar credenciales", e);
            }
        });
    }

    public void enviarNotificacionCambioEstadoPedido(Pedido pedido, String estadoAnterior) {
        db.collection("usuarios")
                .document(pedido.getClienteId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario cliente = documentSnapshot.toObject(Usuario.class);
                    if (cliente != null && cliente.getFcmToken() != null) {
                        String titulo = "Actualización de tu pedido";
                        String mensaje = generarMensajeEstadoPedido(pedido.getEstado(), estadoAnterior);

                        Map<String, String> data = new HashMap<>();
                        data.put("pedidoId", pedido.getId());
                        data.put("tipo", "cambio_estado_pedido");
                        data.put("estadoAnterior", estadoAnterior);
                        data.put("estadoNuevo", pedido.getEstado());

                        enviarNotificacionFCM(cliente.getFcmToken(), titulo, mensaje, data);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener datos del cliente", e));
    }

    private String generarMensajeEstadoPedido(String estadoNuevo, String estadoAnterior) {
        switch (estadoNuevo) {
            case "Recibido":
                return "Tu pedido ha sido recibido y está siendo procesado";
            case "En preparación":
                return "¡Tu pedido está siendo preparado!";
            case "Recogiendo pedido":
                return "Un repartidor está en camino a recoger tu pedido";
            case "En camino":
                return "¡Tu pedido está en camino!";
            case "Entregado":
                return "Tu pedido ha sido entregado. ¡Buen provecho!";
            default:
                return "El estado de tu pedido ha cambiado de " + estadoAnterior + " a " + estadoNuevo;
        }
    }

    private void enviarNotificacionFCM(String token, String titulo, String mensaje, Map<String, String> data) {
        executor.execute(() -> {
            try {
                JSONObject message = new JSONObject();

                // Configurar notificación
                JSONObject notification = new JSONObject();
                notification.put("title", titulo);
                notification.put("body", mensaje);

                // Configurar mensaje FCM
                JSONObject fcmMessage = new JSONObject();
                message.put("token", token);
                message.put("notification", notification);

                // Agregar datos adicionales si existen
                if (data != null) {
                    message.put("data", new JSONObject(data));
                }

                fcmMessage.put("message", message);

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_API, fcmMessage,
                        response -> Log.d(TAG, "Notificación enviada exitosamente"),
                        error -> Log.e(TAG, "Error enviando notificación: " + error.getMessage())) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + accessToken);
                        return headers;
                    }
                };

                requestQueue.add(request);
            } catch (JSONException e) {
                Log.e(TAG, "Error al crear mensaje FCM", e);
            }
        });
    }

    public void enviarNotificacionNuevoPedidoRepartidor(String repartidorId, Pedido pedido) {
        db.collection("usuarios")
                .document(repartidorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario repartidor = documentSnapshot.toObject(Usuario.class);
                    if (repartidor != null && repartidor.getFcmToken() != null) {
                        String titulo = "Nuevo pedido disponible";
                        String mensaje = "Hay un nuevo pedido para entregar cerca de tu ubicación";

                        Map<String, String> data = new HashMap<>();
                        data.put("pedidoId", pedido.getId());
                        data.put("tipo", "nuevo_pedido_disponible");
                        data.put("restauranteId", pedido.getRestauranteId());

                        enviarNotificacionFCM(repartidor.getFcmToken(), titulo, mensaje, data);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener datos del repartidor", e));
    }

    public void enviarNotificacionAsignacionRepartidor(Pedido pedido, String repartidorId) {
        // Notificar al cliente
        db.collection("usuarios")
                .document(pedido.getClienteId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Usuario cliente = documentSnapshot.toObject(Usuario.class);
                    if (cliente != null && cliente.getFcmToken() != null) {
                        String titulo = "Repartidor asignado";
                        String mensaje = "Un repartidor ha sido asignado a tu pedido";

                        Map<String, String> data = new HashMap<>();
                        data.put("pedidoId", pedido.getId());
                        data.put("tipo", "asignacion_repartidor");
                        data.put("repartidorId", repartidorId);

                        enviarNotificacionFCM(cliente.getFcmToken(), titulo, mensaje, data);
                    }
                });
    }

    public void enviarNotificacionVerificacionCompletada(String pedidoId) {
        // Primero obtener el pedido para tener los IDs
        db.collection("pedidos")
                .document(pedidoId)
                .get()
                .addOnSuccessListener(pedidoDoc -> {
                    if (pedidoDoc.exists()) {
                        String clienteId = pedidoDoc.getString("clienteId");
                        String repartidorId = pedidoDoc.getString("repartidorId");

                        if (clienteId != null && repartidorId != null) {
                            // Usar UsuarioRepository para obtener los usuarios
                            UsuarioRepository usuarioRepository = new UsuarioRepository();

                            Task<Usuario> clienteTask = usuarioRepository.getUserById(clienteId);
                            Task<Usuario> repartidorTask = usuarioRepository.getUserById(repartidorId);

                            Tasks.whenAllSuccess(clienteTask, repartidorTask)
                                    .addOnSuccessListener(results -> {
                                        Usuario cliente = (Usuario) results.get(0);
                                        Usuario repartidor = (Usuario) results.get(1);

                                        // Enviar notificación al cliente
                                        if (cliente != null && cliente.getFcmToken() != null) {
                                            String titulo = "¡Pedido completado!";
                                            String mensaje = "La entrega y el pago han sido confirmados";

                                            Map<String, String> dataCliente = new HashMap<>();
                                            dataCliente.put("tipo", "verificacion_completa");
                                            dataCliente.put("pedidoId", pedidoId);
                                            dataCliente.put("rol", "CLIENTE");
                                            dataCliente.put("destino", "ClienteConfirmacionEntregaActivity");

                                            enviarNotificacionFCM(cliente.getFcmToken(), titulo, mensaje, dataCliente);
                                        }

                                        // Enviar notificación al repartidor
                                        if (repartidor != null && repartidor.getFcmToken() != null) {
                                            String titulo = "¡Pedido completado!";
                                            String mensaje = "La entrega y el pago han sido confirmados";

                                            Map<String, String> dataRepartidor = new HashMap<>();
                                            dataRepartidor.put("tipo", "verificacion_completa");
                                            dataRepartidor.put("pedidoId", pedidoId);
                                            dataRepartidor.put("rol", "REPARTIDOR");
                                            dataRepartidor.put("destino", "RepartidorConfirmacionEntregaActivity");

                                            enviarNotificacionFCM(repartidor.getFcmToken(), titulo, mensaje, dataRepartidor);
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error al obtener usuarios: " + e.getMessage()));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al obtener pedido: " + e.getMessage()));
    }

}
