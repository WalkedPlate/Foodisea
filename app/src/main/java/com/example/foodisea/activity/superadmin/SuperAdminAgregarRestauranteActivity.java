package com.example.foodisea.activity.superadmin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminAgregarRestauranteBinding;

public class SuperAdminAgregarRestauranteActivity extends AppCompatActivity {
    ActivitySuperAdminAgregarRestauranteBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuperAdminAgregarRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura el botón de guardar restaurante
        binding.btnCrearRestaurante.setOnClickListener(v -> {
            String nombre = binding.etNombre.getText().toString(); // Ajustado a etNombre
            String ruc = binding.etRuc.getText().toString();
            String web = binding.etWeb.getText().toString();
            String telefono = binding.etTelefono.getText().toString();
            String categoria = binding.etCategoria.getText().toString();
            String direccion = binding.etDireccion.getText().toString();

            if (!nombre.isEmpty() && !direccion.isEmpty() && !telefono.isEmpty() && !categoria.isEmpty()) {
                // Lógica para guardar el restaurante
                Toast.makeText(this, "Restaurante guardado exitosamente", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad después de guardar
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });
    }
}