package com.example.foodisea.activity.repartidor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.RestauranteAdapter;
import com.example.foodisea.adapter.repartidor.RestaurantRepartidorAdapter;
import com.example.foodisea.databinding.ActivityClienteMainBinding;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.entity.OrderItem;
import com.example.foodisea.entity.Restaurante;

import java.util.ArrayList;
import java.util.List;

public class RepartidorMainActivity extends AppCompatActivity {

    ActivityRepartidorMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // binding
        binding = ActivityRepartidorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Configurar el adaptador
        RestaurantRepartidorAdapter adapter = new RestaurantRepartidorAdapter(this, getRestauranteList());
        binding.recyclerViewRestaurantes.setAdapter(adapter);
        binding.recyclerViewRestaurantes.setLayoutManager(new LinearLayoutManager(this));


        // Inicializar botones
        setupButtonListeners();
    }

    private void setupButtonListeners() {

        //Botón del perfil
        binding.btnProfile.setOnClickListener(v -> {

        });

        //Botón del delivery?
        binding.btnOrders.setOnClickListener(v -> {

        });
    }

    private List<Restaurante> getRestauranteList() {
        // Crear una lista de restaurantes
        List<Restaurante> restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        // Añadir más restaurantes
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "Burger - Chicken - Ribs - Wings", 4.7f, R.drawable.restaurant_image, "2118 Thornridge Cir. Syracuse"));
        restaurantList.add(new Restaurante("Burger Place", "Burgers - Fries - Shakes", 4.5f, R.drawable.burger_image, "2118 Thornridge Cir. Syracuse"));

        return restaurantList;
    }


}