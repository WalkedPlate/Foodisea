package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityAdminResDetallesPedidosBinding;

public class AdminResDetallesPedidosActivity extends AppCompatActivity {

    ActivityAdminResDetallesPedidosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        String idPedido = intent.getStringExtra("idPedido");
        String direccionDestino = intent.getStringExtra("direccionDestino");
        String nombreCliente = intent.getStringExtra("nombreCliente");
        String telefono = intent.getStringExtra("telefono");
        String metodoPago = intent.getStringExtra("metodoPago");
        String estadoPago = intent.getStringExtra("estadoPago");

        super.onCreate(savedInstanceState);
        binding = ActivityAdminResDetallesPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.idPedido.setText(idPedido);
        binding.tvAddress.setText(direccionDestino);
        binding.idNombreCliente.setText(nombreCliente);
        binding.idTelefono.setText(telefono);
        binding.metodoPago.setText(metodoPago);
        binding.estadoPago.setText(estadoPago);
        binding.btnBack.setOnClickListener(view -> {
            Intent pedidos = new Intent(this, AdminResPedidosActivity.class);
            startActivity(pedidos);
        });
    }
}