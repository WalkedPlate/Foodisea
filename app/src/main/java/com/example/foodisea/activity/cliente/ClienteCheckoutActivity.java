package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;

public class ClienteCheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // funcion de los botones
        Button btnBack = findViewById(R.id.btnBack);
        TextView btnEditOrder = findViewById(R.id.btnEditOrder);
        Button btnAccept = findViewById(R.id.btnAccept);

        btnBack.setOnClickListener(v -> {
            // Acción para regresar
            finish(); // Cierra la actividad actual y regresa
        });

        btnEditOrder.setOnClickListener(v -> {
            // Acción para ver el carrito
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });

        btnAccept.setOnClickListener(v -> {
            // Acción para ver el mensaje de confirmación
            Intent confirmacion = new Intent(this, ClienteCarritoActivity.class);
            startActivity(confirmacion);
        });
    }
}