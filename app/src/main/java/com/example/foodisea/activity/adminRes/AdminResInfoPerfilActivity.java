package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityAdminResInfoPerfilBinding;
import com.example.foodisea.model.AdministradorRestaurante;

public class AdminResInfoPerfilActivity extends AppCompatActivity {

    ActivityAdminResInfoPerfilBinding binding;
    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        binding = ActivityAdminResInfoPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Obtener al admin res logueado
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();

        // Actualizar la UI con los datos del admin res
        updateUIWithUserData();

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminResEditPerfilActivity.class);
            startActivity(intent);
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateUIWithUserData() {
        if (administradorRestauranteActual != null) {
            // Actualizar datos del administrador del restaurante
            binding.tvTipoDocId.setText(administradorRestauranteActual.getRestauranteId());
            binding.tvUserName.setText(administradorRestauranteActual.obtenerNombreCompleto());
            binding.tvUserDocId.setText(administradorRestauranteActual.getDocumentoId());
            binding.tvUserBirthdate.setText(administradorRestauranteActual.getFechaNacimiento());
            binding.tvUserMail.setText(administradorRestauranteActual.getCorreo());
            binding.tvUserCell.setText(administradorRestauranteActual.getTelefono());
            binding.tvUserAddress.setText(administradorRestauranteActual.getDireccion());

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
}