package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.databinding.ActivityAdminResPerfilBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminResPerfilActivity extends AppCompatActivity {

    private ActivityAdminResPerfilBinding binding;
    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        initializeComponents();
        setupListeners();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivityAdminResPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al administrador de restaurante logueado
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();

        // Actualizar la UI con los datos del administrador
        updateUIWithUserData();

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Actualiza la UI con los datos del administrador de restaurante actual
     */
    private void updateUIWithUserData() {
        if (administradorRestauranteActual != null) {
            // Actualizar datos del administrador del restaurante
            binding.tvUserName.setText(administradorRestauranteActual.obtenerNombreCompleto());

            // Cargar imagen de perfil
            if (administradorRestauranteActual.getFoto() != null && !administradorRestauranteActual.getFoto().isEmpty()) {
                Glide.with(this)
                        .load(administradorRestauranteActual.getFoto())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.error_image)
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivUserPhoto);
            }

            // Mostrar tipo de usuario
            binding.tvTypeUser.setText("Administrador de restaurante");
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
            Intent intent = new Intent(this, AdminResInfoPerfilActivity.class);
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