package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.databinding.ActivityClientePerfilBinding;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Activity que muestra el perfil del cliente y gestiona sus opciones
 */
public class ClientePerfilActivity extends AppCompatActivity {

    private ActivityClientePerfilBinding binding;
    private UsuarioRepository usuarioRepository;
    private SharedPreferences prefs;

    // Constantes para SharedPreferences
    private static final String PREF_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivityClientePerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usuarioRepository = new UsuarioRepository();
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.llPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteInfoPerfilActivity.class);
            startActivity(intent);
        });

        binding.llLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    /**
     * Muestra diálogo de confirmación para cerrar sesión
     */
    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> handleLogout())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Maneja el proceso de cierre de sesión
     */
    private void handleLogout() {
        // Cerrar sesión en Firebase
        usuarioRepository.logout();

        // Limpiar datos de sesión
        clearUserSession();

        // Redirigir al login y limpiar el stack de actividades
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Limpia los datos de sesión almacenados
     */
    private void clearUserSession() {
        prefs.edit()
                .clear()
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}