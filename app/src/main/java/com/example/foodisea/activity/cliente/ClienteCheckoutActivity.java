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
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.CarritoAdapter;
import com.example.foodisea.model.Plato;
import com.example.foodisea.model.PlatoCantidad;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ClienteCheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarritoAdapter checkoutAdapter ;

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

        // Inicializar el RecyclerView
        recyclerView = findViewById(R.id.orderItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador y asignarlo al RecyclerView
        checkoutAdapter = new CarritoAdapter(this, getPlatosCarrito(), true); // isCheckout = true
        recyclerView.setAdapter(checkoutAdapter);


        // funcion de los botones
        Button btnBack = findViewById(R.id.btnBack);
        TextView btnEditOrder = findViewById(R.id.btnEditOrder);
        Button btnAccept = findViewById(R.id.btnAccept);

        btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        btnEditOrder.setOnClickListener(v -> {
            // Acción para ver el carrito
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });

        btnAccept.setOnClickListener(v -> {
            // Acción para ver el mensaje de confirmación
            Intent confirmacion = new Intent(this, ConfirmacionPedido.class);
            startActivity(confirmacion);
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
        platoCarritos.add(new PlatoCantidad("PlatoId4", 3));


        return platoCarritos;
    }
}