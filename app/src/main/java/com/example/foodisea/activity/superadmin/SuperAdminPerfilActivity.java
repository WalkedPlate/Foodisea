package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteInfoPerfilActivity;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClientePerfilBinding;
import com.example.foodisea.databinding.ActivitySuperAdminPerfilBinding;
import com.example.foodisea.model.Superadmin;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SuperAdminPerfilActivity extends AppCompatActivity {

    private ActivitySuperAdminPerfilBinding binding;
    private SessionManager sessionManager;
    private Superadmin superadminActual;

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
        binding = ActivitySuperAdminPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al administrador de restaurante logueado
        superadminActual = sessionManager.getSuperadminActual();
        updateUIWithUserData();

        // Actualizar la UI con los datos del administrador

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Actualiza la UI con los datos del cliente actual
     */
    private void updateUIWithUserData() {
        if (superadminActual != null) {
            // Actualizar el nombre del superadmin
            binding.tvUserName.setText(superadminActual.obtenerNombreCompleto());

            // Si tienes una foto de perfil, también podrías actualizarla aquí
            // Por ejemplo, si usas Glide para cargar imágenes:
        /*
        if (clienteActual.getPhotoUrl() != null && !clienteActual.getPhotoUrl().isEmpty()) {
            Glide.with(this)
                .load(clienteActual.getPhotoUrl())
                .circleCrop()
                .into(binding.ivUserPhoto);
        }
        */
        }
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
     * Maneja el proceso de cierre de sesión usando SessionManager
     */
    private void handleLogout() {
        // Mostrar indicador de progreso si es necesario
        showProgress();

        sessionManager.logout()
                .addOnSuccessListener(aVoid -> {
                    hideProgress();
                    // Redirigir al login y limpiar el stack de actividades
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    // Mostrar error al usuario
                    Toast.makeText(this,
                            "Error al cerrar sesión. Intente nuevamente.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Muestra un indicador de progreso durante el cierre de sesión
     */
    private void showProgress() {
        // Aquí se puede implementar la lógica de mostrar progreso
        // Por ejemplo, usando un ProgressDialog o ProgressBar
    }

    /**
     * Oculta el indicador de progreso
     */
    private void hideProgress() {
        // Aquí se ocultastu indicador del progreso
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}