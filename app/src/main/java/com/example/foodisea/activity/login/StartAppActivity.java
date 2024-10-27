package com.example.foodisea.activity.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityStartAppBinding;

public class StartAppActivity extends AppCompatActivity {

    ActivityStartAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStartAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        // funciones de botones
        binding.btnInicioSesion.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        binding.btnRegistrate.setOnClickListener(view -> {
            Intent intent = new Intent(this, SelectRolActivity.class);
            startActivity(intent);
        });
    }
}