package com.example.foodisea.activity.adminRes;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClientePerfilActivity;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityAdminResHomeBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.notification.NotificationHelper;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.RestauranteRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminResHomeActivity extends AppCompatActivity {

    private ActivityAdminResHomeBinding binding;
    private RestauranteRepository restauranteRepository;
    private FirebaseStorage storage;
    private static final String TAG = "AdminResHomeActivity";
    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;
    private PedidoRepository pedidoRepository;
    private NotificationHelper notificationHelper;
    private static final String CHANNEL_ID = "admin_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupUI();
        validateAndLoadData();
        handleNotificationIntent(getIntent());
    }

    private void initializeComponents() {
        // Inicializar repositorios y servicios
        restauranteRepository = new RestauranteRepository();
        pedidoRepository = new PedidoRepository(this);
        storage = FirebaseStorage.getInstance();
        sessionManager = SessionManager.getInstance(this);
        notificationHelper = new NotificationHelper(this);



        // Configurar Edge to Edge
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar notificaciones
        createNotificationChannel();
        // Movemos getFCMToken() a después de la validación de sesión
    }

    private void setupUI() {
        setupButtons();
        setupNotificationButton();
        try {
            String welcomeMessage = String.format("¡Hola %s, administra tu restaurant",
                    administradorRestauranteActual.getNombres().split(" ")[0]);
            binding.tvWelcome.setText(welcomeMessage);
        } catch (Exception e) {
            // En caso de algún error con el nombre, usar mensaje genérico
            binding.tvWelcome.setText("¡Hola, administra tu restaurant");
            Log.e("AdminResHomeActivity", "Error al configurar mensaje de bienvenida", e);
        }

    }

    private void setupButtons() {
        binding.btnCarta.setOnClickListener(v -> {
            Intent carta = new Intent(this, AdminResCartaActivity.class);
            startActivity(carta);
        });

        binding.btnPedidos.setOnClickListener(view -> {
            Intent pedidos = new Intent(this, AdminResPedidosActivity.class);
            startActivity(pedidos);
        });

        binding.btnReporte.setOnClickListener(view -> {
            Intent reportes = new Intent(this, AdminResReportesActivity.class);
            startActivity(reportes);
        });

        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminResPerfilActivity.class);
            startActivity(intent);
        });
    }

    private void setupNotificationButton() {
        binding.btnNotifications.setOnClickListener(view -> {
            if (administradorRestauranteActual == null) {
                Toast.makeText(this,
                        "Error: Administrador no inicializado",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (administradorRestauranteActual.getRestauranteId() == null) {
                Toast.makeText(this,
                        "Error: ID del restaurante no disponible",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostrar progreso
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Enviando notificación de prueba...");
            progressDialog.show();

            // Crear pedido de prueba
            Pedido pedidoPrueba = new Pedido();
            String pedidoId = "test-" + System.currentTimeMillis();
            pedidoPrueba.setId(pedidoId);
            pedidoPrueba.setRestauranteId(administradorRestauranteActual.getRestauranteId());

            Log.d(TAG, "Enviando notificación de prueba para restaurante: " +
                    administradorRestauranteActual.getRestauranteId());

            // Primero verificar/actualizar el token FCM
            getFCMToken(() -> {
                // Callback después de actualizar el token
                notificationHelper.sendNewOrderNotification(pedidoPrueba);

                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this,
                            "Notificación de prueba enviada para pedido: " + pedidoId,
                            Toast.LENGTH_LONG).show();
                }, 2000);
            });
        });
    }

    private void getFCMToken(Runnable onComplete) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token obtenido: " + token);

                        if (administradorRestauranteActual != null &&
                                administradorRestauranteActual.getRestauranteId() != null) {
                            updateRestaurantToken(
                                    administradorRestauranteActual.getRestauranteId(),
                                    token,
                                    onComplete
                            );
                        } else {
                            Log.e(TAG, "No se puede actualizar token: administrador o ID nulo");
                            onComplete.run();
                        }
                    } else {
                        Log.e(TAG, "Error obteniendo FCM token", task.getException());
                        onComplete.run();
                    }
                });
    }

    private void updateRestaurantToken(String restaurantId, String token, Runnable onComplete) {
        Log.d(TAG, "Actualizando token para restaurante: " + restaurantId);

        FirebaseFirestore.getInstance()
                .collection("restaurantes")
                .document(restaurantId)
                .update("adminFcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Token FCM actualizado exitosamente");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando token FCM", e);
                    onComplete.run();
                });
    }

    private void validateAndLoadData() {
        sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
            @Override
            public void onSessionValid(Usuario usuario) {
                if (usuario instanceof AdministradorRestaurante) {
                    administradorRestauranteActual = (AdministradorRestaurante) usuario;
                    Log.d(TAG, "Sesión válida para administrador: " +
                            administradorRestauranteActual.getId());

                    // Actualizar token FCM después de validar sesión
                    getFCMToken(() -> {
                        loadRestaurantData();
                    });
                } else {
                    Toast.makeText(AdminResHomeActivity.this,
                            "Acceso no autorizado", Toast.LENGTH_SHORT).show();
                    goToLogin();
                }
            }

            @Override
            public void onSessionError() {
                Log.e(TAG, "Error validando sesión");
                goToLogin();
            }
        });
    }

    private void loadRestaurantData() {
        if (administradorRestauranteActual == null || administradorRestauranteActual.getId() == null) {
            Log.e(TAG, "Administrador no inicializado correctamente");
            goToLogin();
            return;
        }

        restauranteRepository.getRestauranteByAdminId(administradorRestauranteActual.getId())
                .addOnSuccessListener(restaurante -> {
                    if (restaurante != null) {
                        updateUI(restaurante);
                        loadRestaurantImage(restaurante.getId());
                    } else {
                        Toast.makeText(this, "No se encontró el restaurante",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant data", e);
                    Toast.makeText(this, "Error al cargar los datos del restaurante",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRestaurantImage(String restaurantId) {
        StorageReference imageRef = storage.getReference()
                .child("restaurants")
                .child(restaurantId)
                .child("profile.jpg");

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.restaurant_image)
                            .error(R.drawable.restaurant_image)
                            .centerCrop()
                            .into(binding.imageView2);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant image", e);
                    binding.imageView2.setImageResource(R.drawable.restaurant_image);
                });
    }

    private void updateUI(Restaurante restaurante) {
        binding.nombreRestaurant.setText(restaurante.getNombre());
        binding.descripcionRestaurant.setText(restaurante.getDescripcion());
        String ratingText = String.format("%.1f", restaurante.getRating());
        binding.tvRestaurantRating.setText(ratingText);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones Administrador",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para administradores de restaurante");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            askPermission();
        }
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{POST_NOTIFICATIONS},
                    101
            );
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("fromNotification", false)) {
            String orderId = intent.getStringExtra("orderId");
            if (orderId != null) {
                showNewOrderDialog(orderId);
            }
        }
    }

    private void showNewOrderDialog(String orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Nuevo Pedido")
                .setMessage("Has recibido un nuevo pedido. ¿Deseas verlo ahora?")
                .setPositiveButton("Ver Pedido", (dialog, which) -> {
                    Intent pedidos = new Intent(this, AdminResPedidosActivity.class);
                    pedidos.putExtra("highlightOrderId", orderId);
                    startActivity(pedidos);
                })
                .setNegativeButton("Después", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationHelper != null) {
            notificationHelper.shutdown();
        }
    }
}