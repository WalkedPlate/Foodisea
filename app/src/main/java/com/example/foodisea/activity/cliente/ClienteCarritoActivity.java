package com.example.foodisea.activity.cliente;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.adapter.CarritoAdapter;
import com.example.foodisea.entity.ProductoCarrito;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClienteCarritoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarritoAdapter adapter;
    private List<ProductoCarrito> productoCarritos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_carrito);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Inicializar el RecyclerView
        recyclerView = findViewById(R.id.rvCartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar la lista de productos del carrito
        productoCarritos = new ArrayList<>();

        // Agregar ítems de prueba al carrito
        productoCarritos.add(new ProductoCarrito("Hamburguesa",64.00, R.drawable.burger, 2));
        productoCarritos.add(new ProductoCarrito("Hamburguesa VB", 32.00, R.drawable.burger, 1));

        // Inicializar el adaptador y asignarlo al RecyclerView
        adapter = new CarritoAdapter(productoCarritos);
        recyclerView.setAdapter(adapter);
    }
}