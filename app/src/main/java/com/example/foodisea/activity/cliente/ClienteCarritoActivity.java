package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.CarritoAdapter;
import com.example.foodisea.model.Plato;
import com.example.foodisea.model.PlatoCantidad;

import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClienteCarritoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarritoAdapter adapter;


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


        // Inicializar el adaptador y asignarlo al RecyclerView
        adapter = new CarritoAdapter(this,getPlatosCarrito(),false);
        recyclerView.setAdapter(adapter);

        // funcion de los botones
        Button btnBack = findViewById(R.id.btnBack);
        Button btnCheckout = findViewById(R.id.btnCheckout);

        btnBack.setOnClickListener(v -> {
            // Acción para regresar
            finish(); // Cierra la actividad actual y regresa
        });

        btnCheckout.setOnClickListener(v -> {
            // Acción para ir al carrito de compras
            Intent checkout = new Intent(this, ClienteCheckoutActivity.class);
            startActivity(checkout);
        });
    }

    // Obtener desde bd
    private List<PlatoCantidad> getPlatosCarrito() {
        // Inicializar la lista de productos del carrito
        List<PlatoCantidad> platoCarritos = new ArrayList<>();

        // Agregar ítems de prueba al carrito
        platoCarritos.add(new PlatoCantidad("PlatoId1",3));
        platoCarritos.add(new PlatoCantidad("PlatoId2", 4));
        platoCarritos.add(new PlatoCantidad("PlatoId3",8));
        platoCarritos.add(new PlatoCantidad("PlatoId4", 6));


        return platoCarritos;
    }

}