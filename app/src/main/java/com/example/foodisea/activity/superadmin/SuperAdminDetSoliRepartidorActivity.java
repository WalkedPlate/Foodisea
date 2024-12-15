package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminDetSoliRepartidorBinding;
import com.example.foodisea.databinding.ActivitySuperAdminInfoPerfilBinding;
import com.example.foodisea.manager.SessionManager;

public class SuperAdminDetSoliRepartidorActivity extends AppCompatActivity {

    ActivitySuperAdminDetSoliRepartidorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        binding = ActivitySuperAdminDetSoliRepartidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }
}