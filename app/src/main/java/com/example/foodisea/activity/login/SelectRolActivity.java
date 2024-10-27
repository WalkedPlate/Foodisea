package com.example.foodisea.activity.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySelectRolBinding;

public class SelectRolActivity extends AppCompatActivity {
    /**
     * ViewBinding para acceder a las vistas
     */
    ActivitySelectRolBinding binding;

    // Constantes para identificar roles
    public static final String EXTRA_TIPO_USUARIO = "tipo_usuario";
    public static final String TIPO_CLIENTE = "Cliente";
    public static final String TIPO_REPARTIDOR = "Repartidor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySelectRolBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana para una correcta visualización
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura las vistas y listeners iniciales
     */
    private void setupViews() {
        setupClickListeners();
    }

    /**
     * Configura los listeners para todos los elementos interactivos
     */
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnRepartidores.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(EXTRA_TIPO_USUARIO, TIPO_REPARTIDOR);
            startActivity(intent);
        });

        binding.btnClientes.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(EXTRA_TIPO_USUARIO, TIPO_CLIENTE);
            startActivity(intent);
        });
    }
}