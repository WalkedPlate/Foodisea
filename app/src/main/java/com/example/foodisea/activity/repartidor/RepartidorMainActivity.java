package com.example.foodisea.activity.repartidor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.RestaurantRepartidorAdapter;
import com.example.foodisea.databinding.ActivityRepartidorMainBinding;
import com.example.foodisea.model.Restaurante;

import java.util.ArrayList;
import java.util.Arrays;
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

    private List<com.example.foodisea.model.Restaurante> getRestauranteList() {
        // Obtener desde bd
        List<com.example.foodisea.model.Restaurante> restaurantList  = new ArrayList<>();
        // Datos de la lista
        List<String> categorias1 = Arrays.asList("Burger", "Chicken", "Ribs", "Wings");
        List<String> categorias2 = Arrays.asList("Burgers", "Fries", "Shakes");
        List<String> categorias3 = Arrays.asList("Pizza", "Pasta", "Salads");
        List<String> categorias4 = Arrays.asList("Sushi", "Rolls", "Noodles");

        // Añadir restaurantes a la lista
        restaurantList.add(new com.example.foodisea.model.Restaurante("Rose Garden Restaurant", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.7, Arrays.asList("restaurant_image","burger_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Burger Place", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias2, 4.5, Arrays.asList("burger_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias3, 4.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 4.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.6, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias2, 2.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 1.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new com.example.foodisea.model.Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias3, 2.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 3.6, Arrays.asList("burger_image"), null));


        return restaurantList ;
    }


}