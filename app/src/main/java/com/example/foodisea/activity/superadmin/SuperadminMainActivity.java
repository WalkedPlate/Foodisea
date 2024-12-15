package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.RepartidorPerfilActivity;
import com.example.foodisea.databinding.ActivitySuperAdminPerfilBinding;
import com.example.foodisea.databinding.ActivitySuperadminMainBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Superadmin;

public class SuperadminMainActivity extends AppCompatActivity {

    ActivitySuperadminMainBinding binding;
    private SessionManager sessionManager;
    private Superadmin superadminActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

    }


    private void initializeComponents() {
        binding = ActivitySuperadminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);
        superadminActual = sessionManager.getSuperadminActual();

        // Actualizar la UI con los datos del super administrador
        updateUIWithUserData();

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
        binding.btnClientes.setOnClickListener(v ->{
            Intent clientes = new Intent(this, SuperAdminGestionUsuariosActivity.class);
            startActivity(clientes);
        });

        binding.btnRestaurantes.setOnClickListener(v ->{
            Intent restaurantes = new Intent(this, SuperAdminGestionRestauranteActivity.class);
            startActivity(restaurantes);
        });

        binding.btnRepartidores.setOnClickListener(v ->{
            Intent repartidores = new Intent(this, SuperAdminGestionRepartidorActivity.class);
            startActivity(repartidores);
        });

        binding.btnAdministradores.setOnClickListener(v ->{
            Intent administradores = new Intent(this, SuperAdminGestionAdministradoresActivity.class);
            startActivity(administradores);
        });

        // boton logs
        binding.btnLogs.setOnClickListener(v->{
            Intent dashboard = new Intent(this, SuperAdminLogsActivity.class);
            startActivity(dashboard);
        });

        // boton perfil
        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, SuperAdminPerfilActivity.class);
            startActivity(intent);
        });
    }

    private void updateUIWithUserData() {
        try {
            String welcomeMessage = String.format("¡Hola %s!, empecemos a hacer grandes cosas",
                    superadminActual.getNombres().split(" ")[0]);
            binding.tvWelcome.setText(welcomeMessage);
        } catch (Exception e) {
            binding.tvWelcome.setText("¡Hola, empecemos a hacer grandes cosas");
            Log.e("AdminResHomeActivity", "Error al configurar mensaje de bienvenida", e);
        }

    }
}