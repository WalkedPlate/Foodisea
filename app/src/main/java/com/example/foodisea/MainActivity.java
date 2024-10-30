package com.example.foodisea;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityMainBinding;
import com.example.foodisea.firestore.FirestoreInitializer;
import com.example.foodisea.notification.NotificationHelper;
import com.example.foodisea.notification.TokenManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Inicializando ViewBinding
    ActivityMainBinding binding;
    // Inicializador de la bd
    private FirestoreInitializer firestoreInitializer;
    private boolean isInitializing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NotificationHelper notificationHelper = new NotificationHelper(this);

        TokenManager tokenManager = new TokenManager(this);
        tokenManager.obtenerYGuardarToken();

        // Botón para Superadmin
        binding.btnRole1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SuperadminMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Cliente
        binding.btnRole2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClienteMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Repartidor
        binding.btnRole3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RepartidorMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Administrador de Restaurante
        binding.btnRole4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdminResHomeActivity.class);
                startActivity(intent);
            }
        });

        // Obtener y mostrar token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        // Mostrar en el logcat
                        Log.d("FCM_TOKEN", "----------------------------------------");
                        Log.d("FCM_TOKEN", "Token: " + token);
                        Log.d("FCM_TOKEN", "----------------------------------------");

                        // Mostrar en un Toast para copiarlo fácilmente
                        Toast.makeText(this,
                                "Token: " + token,
                                Toast.LENGTH_LONG).show();
                    }
                });


    }

    private void guardarTokenEnFirestore(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Por ahora lo guardamos con un ID de dispositivo único
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("timestamp", FieldValue.serverTimestamp());
        tokenData.put("platform", "android");

        db.collection("fcm_tokens")
                .document(deviceId)
                .set(tokenData)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token guardado exitosamente"))
                .addOnFailureListener(e -> Log.e("FCM", "Error guardando token", e));
    }
}