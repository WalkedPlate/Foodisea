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
        EdgeToEdge.enable(this);

        // binding
        binding = ActivityRepartidorVerOrdenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar insets del sistema para la ventana
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        OrderItemAdapter orderItemsAdapter = new OrderItemAdapter(getOrderItemList());
        binding.orderItemsRecyclerView.setAdapter(orderItemsAdapter);
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Botón para empezar el delivery
        binding.startDeliveryBtn.setOnClickListener(v -> {
            Intent intentStartDelivery = new Intent(RepartidorVerOrdenActivity.this, RepartidorDeliveryMapActivity.class);
            intentStartDelivery.putExtra("customerName", customerName);
            startActivity(intentStartDelivery);
        });

        // Inicializar botones generales
        setupButtonListeners();
    }


    private List<OrderItem> getOrderItemList() {
        List<OrderItem> orderItemList = new ArrayList<>();

        // Datos basados en la imagen
        orderItemList.add(new OrderItem("Pizza Calzone European", "P", 2, R.drawable.burger_image));
        orderItemList.add(new OrderItem("Pizza Calzone European", "M", 1, R.drawable.burger_image));
        orderItemList.add(new OrderItem("Pizza Calzone European", "P", 4, R.drawable.burger_image));
        orderItemList.add(new OrderItem("Pizza Calzone European", "P", 5, R.drawable.burger_image));
        orderItemList.add(new OrderItem("Pizza Calzone European", "P", 6, R.drawable.burger_image));
        orderItemList.add(new OrderItem("Pizza Calzone European", "P", 7, R.drawable.burger_image));

        return orderItemList;
    }

    private void setupButtonListeners() {

        //Botón para regresar
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

    }
}