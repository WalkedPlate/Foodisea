package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminDetalleRestauranteBinding;

public class SuperAdminDetalleRestauranteActivity extends AppCompatActivity {

    ActivitySuperAdminDetalleRestauranteBinding binding;

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
        binding = ActivitySuperAdminDetalleRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        binding.btnEdit.setOnClickListener(v-> {
            Intent editRest = new Intent(this, SuperAdminEditRestauranteActivity.class);
            startActivity(editRest);
        });
        binding.btnReporte.setOnClickListener(v -> {
            Intent reporteRest = new Intent(this, SuperAdminRestaurantesReportesActivity.class);
            startActivity(reporteRest);
        });
    }
}