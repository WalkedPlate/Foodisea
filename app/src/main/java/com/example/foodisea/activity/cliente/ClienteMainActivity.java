package com.example.foodisea.activity.cliente;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.RestauranteAdapter;
import com.example.foodisea.entity.Restaurante;

import java.util.ArrayList;
import java.util.List;

public class ClienteMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView rvRestautants = findViewById(R.id.rvRestaurants);

        // Crear una lista de restaurantes
        List<Restaurante> restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image));
        // Añade más restaurantes según necesites
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image));

        // Configurar el adaptador
        RestauranteAdapter adapter = new RestauranteAdapter(restaurantList);
        rvRestautants.setAdapter(adapter);
        rvRestautants.setLayoutManager(new LinearLayoutManager(this));
    }
}