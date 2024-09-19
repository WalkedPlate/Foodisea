package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;


import com.example.foodisea.R;
import com.example.foodisea.adapter.ProductoAdapter;
import com.example.foodisea.entity.Producto;

import java.util.ArrayList;
import java.util.List;

public class ClienteRestauranteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_restaurante);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa el RecyclerView
        RecyclerView rvProducts = findViewById(R.id.rvProducts);

        // Configura el GridLayoutManager con 2 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columnas
        rvProducts.setLayoutManager(gridLayoutManager);

        // Inicializa la lista de productos
        List<Producto> productList = new ArrayList<>();
        // Agregar productos a la lista
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));

        // Configura el adaptador (el adaptador que has creado para mostrar los productos)
        ProductoAdapter adapter = new ProductoAdapter(this, productList);
        rvProducts.setAdapter(adapter);


        // Obtén los datos pasados desde el intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("name");
        float restaurantRating = intent.getFloatExtra("rating", 0);
        int restaurantImage = intent.getIntExtra("image", R.drawable.restaurant_image);

        // Configura la vista con los datos recibidos
        ImageView ivRestaurantImage = findViewById(R.id.ivRestaurantImage);
        TextView tvRestaurantName = findViewById(R.id.tvRestaurantName);
        TextView tvRestaurantRating = findViewById(R.id.tvRestaurantRating);

        ivRestaurantImage.setImageResource(restaurantImage);
        tvRestaurantName.setText(restaurantName);
        tvRestaurantRating.setText(String.valueOf(restaurantRating));

        // funcion de los botones
        Button btnBack = findViewById(R.id.btnBack);
        Button btnCart = findViewById(R.id.btnCart);
        TextView tvCartItemCount = findViewById(R.id.tvCartItemCount);

        btnBack.setOnClickListener(v -> {
            // Acción para regresar
            finish(); // Cierra la actividad actual y regresa
        });

        btnCart.setOnClickListener(v -> {
            // Acción para ir al carrito de compras
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });



    }
}