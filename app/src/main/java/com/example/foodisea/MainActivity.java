package com.example.foodisea;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityMainBinding;
import com.example.foodisea.firestore.FirestoreInitializer;

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

        /*
        // Inicializar Firestore
        firestoreInitializer = new FirestoreInitializer();

        // Botón para inicializar la base de datos
        binding.btnInitDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInitializing) {
                    initializeDatabase();
                } else {
                    Toast.makeText(MainActivity.this,
                            "La inicialización ya está en proceso",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

         */

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
    }

    /*
    private void initializeDatabase() {
        isInitializing = true;
        binding.btnInitDb.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    firestoreInitializer.initializeDatabase();

                    // Volver al hilo principal para actualizar la UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Base de datos inicializada correctamente",
                                    Toast.LENGTH_LONG).show();
                            binding.btnInitDb.setEnabled(true);
                            binding.progressBar.setVisibility(View.GONE);
                            isInitializing = false;
                        }
                    });
                } catch (Exception e) {
                    // Manejar cualquier error durante la inicialización
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Error al inicializar la base de datos: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            binding.btnInitDb.setEnabled(true);
                            binding.progressBar.setVisibility(View.GONE);
                            isInitializing = false;
                        }
                    });
                }
            }
        }).start();
    }

     */
}