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
import com.example.foodisea.databinding.ActivitySuperAdminAgregarRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSolicitudesRepartidorBinding;

public class SuperAdminAgregarRestauranteActivity extends AppCompatActivity {
    ActivitySuperAdminAgregarRestauranteBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        // Configura el botón de guardar restaurante
        binding.btnCrearRestaurante.setOnClickListener(v -> {
            String nombre = binding.etNombreRes.getText().toString(); // Ajustado a etNombre
            //String ruc = binding.etRuc.getText().toString();
            String telefono = binding.etTelefono.getText().toString();
           // String categoria = binding.etCategoria.getText().toString();
            //String direccion = binding.etDireccion.getText().toString();

            if (!nombre.isEmpty()  && !telefono.isEmpty()) {
                // Lógica para guardar el restaurante
                Toast.makeText(this, "Restaurante guardado exitosamente", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad después de guardar
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySuperAdminAgregarRestauranteBinding.inflate(getLayoutInflater());
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

    }
}