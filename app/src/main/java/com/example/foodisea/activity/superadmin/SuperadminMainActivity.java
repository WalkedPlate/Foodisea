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
        binding.btnDashboard.setOnClickListener(v->{
            Intent dashboard = new Intent(this, SuperAdminSelectionActivity.class);
            startActivity(dashboard);
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