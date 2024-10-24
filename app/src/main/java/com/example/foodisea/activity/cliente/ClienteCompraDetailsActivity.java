package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.DetallePedidoAdapter;
import com.example.foodisea.adapter.cliente.PlatoAdapter;
import com.example.foodisea.adapter.repartidor.OrderItemAdapter;
import com.example.foodisea.databinding.ActivityClienteCompraDetailsBinding;
import com.example.foodisea.entity.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class ClienteCompraDetailsActivity extends AppCompatActivity {

    ActivityClienteCompraDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityClienteCompraDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Configurar el RecyclerView con el adaptador de items de la orden
        DetallePedidoAdapter detallePedidoAdapter = new DetallePedidoAdapter(getOrderItemList());
        binding.orderItemsRecyclerView.setAdapter(detallePedidoAdapter);
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // funcion de los botones
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnConfirmEntrega.setOnClickListener(v -> {
            // Acción para mostrar el QR q el repartidor debe escanear
            Intent comprobante = new Intent(this, ClienteComprobanteQrActivity.class);
            startActivity(comprobante);
        });
    }

    private List<OrderItem> getOrderItemList() {
        List<OrderItem> orderItemList = new ArrayList<>();

        // Datos basados en la imagen
        orderItemList.add(new OrderItem("Burger Ferguson", "P", 3, R.drawable.burger));
        orderItemList.add(new OrderItem("Rockin' Burgers", "M", 4, R.drawable.burger2));
        orderItemList.add(new OrderItem("Coca Cola", "P", 8, R.drawable.soda));
        orderItemList.add(new OrderItem("Crack' Burgers", "P", 3, R.drawable.burger2));

        return orderItemList;
    }

}