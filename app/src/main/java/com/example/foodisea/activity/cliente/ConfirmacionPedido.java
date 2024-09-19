package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;

public class ConfirmacionPedido extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmacion_pedido);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón de cerrar
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Intent inicio =new Intent(this, ClienteMainActivity.class);
            startActivity(inicio);
        });

        // Botón "Sigue tu orden"
        Button btnTracking = findViewById(R.id.btnTracking);
        btnTracking.setOnClickListener(v -> {
            // Aquí puedes abrir la pantalla para seguir la orden
            Intent intent = new Intent(this, ClienteTrackingActivity.class);
            startActivity(intent);
        });
    }
}