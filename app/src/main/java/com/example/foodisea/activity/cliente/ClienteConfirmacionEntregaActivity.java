package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityClienteConfirmacionEntregaBinding;

public class ClienteConfirmacionEntregaActivity extends AppCompatActivity {

    private ActivityClienteConfirmacionEntregaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usar ViewBinding
        binding = ActivityClienteConfirmacionEntregaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar ajustes de EdgeToEdge (si es necesario)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar acción del botón "VER MIS PEDIDOS"
        binding.btnTracking.setOnClickListener(view -> {
            Intent intent = new Intent(ClienteConfirmacionEntregaActivity.this, ClienteHistorialPedidosActivity.class);
            startActivity(intent);

            // Finalizar esta actividad para que no pueda ser alcanzada de nuevo
            finish();
        });
    }
}