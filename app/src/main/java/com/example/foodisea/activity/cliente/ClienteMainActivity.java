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
import com.example.foodisea.databinding.ActivityClienteMainBinding;
import com.example.foodisea.entity.Restaurante;

import java.util.ArrayList;
import java.util.List;

public class ClienteMainActivity extends AppCompatActivity {

    ActivityClienteMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // binding
        binding = ActivityClienteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // limites de pantalla
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Crear una lista de restaurantes
        List<Restaurante> restaurantList = new ArrayList<>();

        // Datos de la lista
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        // Añade más restaurantes
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));

        // Configurar el adaptador
        RestauranteAdapter adapter = new RestauranteAdapter(this, restaurantList);
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(adapter);
    }
}