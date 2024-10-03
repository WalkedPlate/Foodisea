package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminGestionRepartidorBinding;
import com.example.foodisea.databinding.ActivitySuperAdminGestionRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminGestionUsuariosBinding;

public class SuperAdminGestionRepartidorActivity extends AppCompatActivity {
    ActivitySuperAdminGestionRepartidorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperAdminGestionRepartidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnHome.setOnClickListener(v -> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            startActivity(home);
        });
    }
}