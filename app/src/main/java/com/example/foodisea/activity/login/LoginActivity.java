package com.example.foodisea.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnEntrarApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    Toast.makeText(LoginActivity.this, "Se ha iniciado sesión", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validación del campo correo
        if (binding.etCorreo.getText().toString().trim().isEmpty()) {
            binding.etCorreoLayout.setError("Ingresa tu correo");
            isValid = false;
        } else {
            binding.etCorreoLayout.setError(null);
        }

        // Validación del campo de Altura
        if (binding.etPassword.getText().toString().trim().isEmpty()) {
            binding.etPasswordLayout.setError("Ingresa tu contraseña");
            isValid = false;
        } else {
            binding.etPasswordLayout.setError(null);
        }


        return isValid;
    }
}