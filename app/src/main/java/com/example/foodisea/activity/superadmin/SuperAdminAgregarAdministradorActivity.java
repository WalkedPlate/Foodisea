package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminAgregarAdministradorBinding;
import com.example.foodisea.databinding.ActivitySuperAdminAgregarRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSolicitudesRepartidorBinding;

public class SuperAdminAgregarAdministradorActivity extends AppCompatActivity {
    ActivitySuperAdminAgregarAdministradorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        // Configura el botón de guardar restaurante
        binding.btnCrearAdministrador.setOnClickListener(v -> {
            String nombre = binding.etNombres.getText().toString();
            String dni = binding.etDni.getText().toString();
            String fechaNacimiento = binding.etFechaNacimiento.getText().toString();
            String correo = binding.etCorreo.getText().toString();
            String telefono = binding.etTelefono.getText().toString();
//            String domicilio = binding.etDomicilio.getText().toString();

            if (!nombre.isEmpty() && !dni.isEmpty() && !fechaNacimiento.isEmpty() && !correo.isEmpty() && !telefono.isEmpty()) {
                //lógica para guardar el administrador (agregar lógica aquí para enviar datos al servidor o base de datos)
                Toast.makeText(this, "Administrador guardado exitosamente", Toast.LENGTH_SHORT).show();
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
        binding = ActivitySuperAdminAgregarAdministradorBinding.inflate(getLayoutInflater());
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