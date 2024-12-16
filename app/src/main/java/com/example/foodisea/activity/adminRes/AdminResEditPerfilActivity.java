package com.example.foodisea.activity.adminRes;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityAdminResEditPerfilBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.AdministradorRestaurante;

public class AdminResEditPerfilActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    ActivityAdminResEditPerfilBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        // Obtener al admin logueado
        sessionManager = SessionManager.getInstance(this);
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();

    }

    private void initializeComponents() {
        binding = ActivityAdminResEditPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

}