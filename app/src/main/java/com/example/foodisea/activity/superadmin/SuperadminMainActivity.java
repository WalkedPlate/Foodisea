package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.repartidor.RepartidorPerfilActivity;
import com.example.foodisea.databinding.ActivitySuperadminMainBinding;

public class SuperadminMainActivity extends AppCompatActivity {

    ActivitySuperadminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperadminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // funcion botones
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
}