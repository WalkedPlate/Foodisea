package com.example.foodisea.activity.superadmin;

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

public class SuperAdminAgregarAdministradorActivity extends AppCompatActivity {
    ActivitySuperAdminAgregarAdministradorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuperAdminAgregarAdministradorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura el botón de guardar restaurante
        binding.btnCrearAdministrador.setOnClickListener(v -> {
            String nombre = binding.etNombre.getText().toString();
            String dni = binding.etDNI.getText().toString();
            String fechaNacimiento = binding.etFechaNacimiento.getText().toString();
            String correo = binding.etCorreo.getText().toString();
            String telefono = binding.etTelefono.getText().toString();
            String domicilio = binding.etDomicilio.getText().toString();
            String estadoUsuario = binding.spinnerAdministrador.getText().toString();

            if (!nombre.isEmpty() && !dni.isEmpty() && !fechaNacimiento.isEmpty() && !correo.isEmpty() && !telefono.isEmpty() && !domicilio.isEmpty() && !estadoUsuario.isEmpty()) {
                //lógica para guardar el administrador (agregar lógica aquí para enviar datos al servidor o base de datos)
                Toast.makeText(this, "Administrador guardado exitosamente", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad después de guardar
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"Activo", "Inactivo"});
        binding.spinnerAdministrador.setAdapter(adapter);
    }
}