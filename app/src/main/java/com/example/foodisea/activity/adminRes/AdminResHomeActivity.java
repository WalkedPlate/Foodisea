package com.example.foodisea.activity.adminRes;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import com.example.foodisea.databinding.ActivityAdminResHomeBinding;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.RestauranteRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AdminResHomeActivity extends AppCompatActivity {

    private ActivityAdminResHomeBinding binding;
    private RestauranteRepository restauranteRepository;
    //private FirebaseAuth auth;
    private FirebaseStorage storage;
    private static final String TAG = "AdminResHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase
        restauranteRepository = new RestauranteRepository();
        //auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar botones
        setupButtons();

        // Cargar datos del restaurante
        loadRestaurantData();

        createNotificationChannel();

        binding.btnNotifications.setOnClickListener(view -> {
            lanzarNotificacion();
        });
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
            // Implementar navegación a reportes
        });
    }

    private void loadRestaurantData() {
        //String userId = auth.getCurrentUser().getUid();
        String userId = "AR001";

        restauranteRepository.getRestauranteByAdminId(userId)
                .addOnSuccessListener(restaurante -> {
                    if (restaurante != null) {
                        updateUI(restaurante);
                        loadRestaurantImage(restaurante.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant data", e);
                    Toast.makeText(this, "Error al cargar los datos del restaurante",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRestaurantImage(String restaurantId) {
        // Referencia a la imagen del restaurante en Storage
        StorageReference imageRef = storage.getReference()
                .child("restaurants")
                .child(restaurantId)
                .child("profile.jpg"); // O el nombre que uses para la imagen principal

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Cargar la imagen usando Glide
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.restaurant_image)
                            .error(R.drawable.restaurant_image)
                            .centerCrop()
                            .into(binding.imageView2);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading restaurant image", e);
                    // Si falla la carga, dejamos la imagen por defecto
                    binding.imageView2.setImageResource(R.drawable.restaurant_image);
                });
    }

    private void updateUI(Restaurante restaurante) {
        // Actualizar nombre y descripción
        binding.nombreRestaurant.setText(restaurante.getNombre());
        binding.descripcionRestaurant.setText(restaurante.getDescripcion());

        // Actualizar rating
        String ratingText = String.format("%.1f", restaurante.getRating());
        binding.tvRestaurantRating.setText(ratingText);

        // Actualizar mensaje de bienvenida
        //String welcomeMessage = getString(R.string.welcome) + " " + restaurante.getNombre();
        //binding.tvWelcome.setText(welcomeMessage);
    }

    String channelId = "channelDefaultPri";
    public void createNotificationChannel(){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, "Canal notificaciones default",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Canal para notificaciones con prioridad default");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            askPermission();
        }
    }

    public void askPermission(){
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(AdminResHomeActivity.this,new String[]{POST_NOTIFICATIONS},101);
        }
    }

    public void lanzarNotificacion(){
        Intent intent = new Intent(this,AdminResPedidosActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Se ha creado el pedido #1234")
                .setContentText("Tocar para ver más detalles")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if(ActivityCompat.checkSelfPermission(this,POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(1,builder.build());
        }
    }
}