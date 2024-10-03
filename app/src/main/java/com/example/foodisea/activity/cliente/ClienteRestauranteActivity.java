package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;


import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.PlatoAdapter;
import com.example.foodisea.databinding.ActivityClienteRestauranteBinding;
import com.example.foodisea.model.Plato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClienteRestauranteActivity extends AppCompatActivity {

    ActivityClienteRestauranteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityClienteRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Configura el GridLayoutManager con 2 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columnas
        binding.rvProducts.setLayoutManager(gridLayoutManager);


        // Configura el adaptador (el adaptador que has creado para mostrar los productos)
        PlatoAdapter adapter = new PlatoAdapter(this, getPlatosList());
        binding.rvProducts.setAdapter(adapter);


        // Obtén los datos pasados desde el intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("name");
        double restaurantRating = intent.getDoubleExtra("rating", 0);
        String restaurantImage = intent.getStringExtra("image");
        String restaurantDesc = intent.getStringExtra("descripcion");

        // Configura la vista con los datos recibidos
        binding.ivRestaurantImage.setImageResource(getResources().getIdentifier(restaurantImage, "drawable", getPackageName()));
        binding.tvRestaurantName.setText(restaurantName);
        binding.tvRestaurantRating.setText(String.valueOf(restaurantRating));
        binding.tvDescripcionRest.setText(restaurantDesc);

        // funcion de los botones
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnCart.setOnClickListener(v -> {
            // Acción para ir al carrito de compras
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });



    }

    // Obtener desde bd
    private List<Plato> getPlatosList() {
        List<Plato> platoList = new ArrayList<>();

        // Agregar platos a la lista
        platoList.add(new Plato(
                UUID.randomUUID().toString(), // Generar un ID único para el Plato
                "Burger Ferguson",             // Nombre del Plato
                "Deliciosa hamburguesa con queso y bacon", // Descripción
                10.00,                         // Precio
                Arrays.asList("burger"), // Lista de URLs de imágenes
                "Plato",                       // Categoría
                false                          // Disponibilidad (outOfStock)
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Rockin' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                15.30,
                Arrays.asList("burger2"),
                "Plato",
                true
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Coca Cola",
                "Refresco de cola",
                5.00,
                Arrays.asList("soda"),
                "Bebida",
                false
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Pepsi",
                "Refresco de cola",
                4.50,
                Arrays.asList("soda2"),
                "Bebida",
                false
        ));


        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Chirox' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                12.80,
                Arrays.asList("burger3"),
                "Plato",
                true
        ));

        platoList.add(new Plato(
                UUID.randomUUID().toString(),
                "Crack' Burgers",
                "Hamburguesa clásica con ingredientes frescos",
                25.00,
                Arrays.asList("burger4"),
                "Plato",
                true
        ));


        return platoList;
    }

}