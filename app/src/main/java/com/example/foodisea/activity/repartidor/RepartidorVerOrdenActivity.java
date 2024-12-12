package com.example.foodisea.activity.repartidor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.OrderItemAdapter;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.databinding.ActivityRepartidorVerOrdenBinding;
import com.example.foodisea.item.OrderItem;


import java.util.ArrayList;
import java.util.List;

public class RepartidorVerOrdenActivity extends AppCompatActivity {

    private ActivityRepartidorVerOrdenBinding binding;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtén los datos pasados desde el intent
        Intent intent = getIntent();
        String orderNumber = intent.getStringExtra("pedidoId");
        String customerName = intent.getStringExtra("clienteNombre");
        String address = intent.getStringExtra("direccionEntrega");
        double price = intent.getDoubleExtra("precio", 0);

        // Setear los datos de la orden
        binding.tvTotalPrice.setText(String.format("%.2f", price));
        binding.orderNumberText.setText(orderNumber);
        binding.customerName.setText(customerName);
        binding.customerAddress.setText(address);
        // Configurar el RecyclerView con el adaptador de items de la orden
//        OrderItemAdapter orderItemsAdapter = new OrderItemAdapter(getOrderItemList());
//        binding.orderItemsRecyclerView.setAdapter(orderItemsAdapter);
//        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Botón para empezar el delivery
        binding.startDeliveryBtn.setOnClickListener(v -> {
            Intent intentStartDelivery = new Intent(RepartidorVerOrdenActivity.this, RepartidorDeliveryMapActivity.class);
            intentStartDelivery.putExtra("customerName", customerName);
            startActivity(intentStartDelivery);
        });

        // Inicializar botones generales
        setupButtonListeners();
        initializeComponents();
    }

    private void initializeComponents() {
        binding = ActivityRepartidorVerOrdenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtonListeners() {
        binding.btnBack.setOnClickListener(v -> {finish();});
    }
}