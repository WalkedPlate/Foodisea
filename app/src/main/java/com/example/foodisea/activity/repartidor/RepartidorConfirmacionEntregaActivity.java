package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityRepartidorChatBinding;
import com.example.foodisea.databinding.ActivityRepartidorConfirmacionEntregaBinding;

public class RepartidorConfirmacionEntregaActivity extends AppCompatActivity {

    private ActivityRepartidorConfirmacionEntregaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar ViewBinding
        binding = ActivityRepartidorConfirmacionEntregaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar EdgeToEdge (si es necesario)
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Acción del botón cerrar
        binding.btnClose.setOnClickListener(v -> finish());

        // Acción del botón "VOLVER AL INICIO"
        binding.btnTracking.setOnClickListener(v -> {
            Intent intent = new Intent(RepartidorConfirmacionEntregaActivity.this, RepartidorMainActivity.class);
            startActivity(intent);

            // Finalizar esta actividad para que no pueda ser alcanzada de nuevo
            finish();
        });
    }


}