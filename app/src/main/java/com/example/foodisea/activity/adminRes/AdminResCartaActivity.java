package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteCarritoActivity;
import com.example.foodisea.adapter.adminRes.CartaAdapter;
import com.example.foodisea.databinding.ActivityAdminResCartaBinding;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Plato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AdminResCartaActivity extends AppCompatActivity {

    ActivityAdminResCartaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResCartaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        CartaAdapter adapter = new CartaAdapter();
        adapter.setContext(AdminResCartaActivity.this);
        adapter.setListaPlatos(getPlatosList());

        // Configura el GridLayoutManager con 2 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columnas

        binding.rvProductos.setAdapter(adapter);
        binding.rvProductos.setLayoutManager(gridLayoutManager);

        // funcion de los botones
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnAddProduct.setOnClickListener(v ->{
            Intent intent = new Intent(this, AdminResNuevoProductoActivity.class);
            startActivity(intent);
        });

    }


    private List<Plato> getPlatosList() {
        List<Plato> platoList = new ArrayList<>();

    // Agregar platos a la lista
        platoList.add(new Plato(
                UUID.randomUUID().toString(), // Generar un ID único para el Plato
                "Burger Ferguson",             // Nombre del Plato
                "Deliciosa hamburguesa con queso y bacon", // Descripción
                40.00,                         // Precio
                Arrays.asList("burger_image"), // Lista de URLs de imágenes
                "Plato",                       // Categoría
                false                          // Disponibilidad (outOfStock)
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Rockin' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                45.00,
                Arrays.asList("burger_image"),
                "Plato",
                true
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Soda",
                "Refresco de cola",
                10.00,
                Arrays.asList("burger_image"),
                "Bebida",
                false
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Soda",
                "Refresco de cola",
                10.00,
                Arrays.asList("burger_image"),
                "Bebida",
                false
        ));


        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Rockin' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                45.00,
                Arrays.asList("burger_image"),
                "Plato",
                true
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Rockin' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                45.00,
                Arrays.asList("burger_image"),
                "Plato",
                true
        ));


        return platoList;
    }

}