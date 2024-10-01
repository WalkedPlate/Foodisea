package com.example.foodisea.activity.cliente;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.RestauranteAdapter;
import com.example.foodisea.databinding.ActivityClienteMainBinding;
import com.example.foodisea.model.Restaurante;

import java.util.ArrayList;
import java.util.Arrays;
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



        // Configurar el adaptador
        RestauranteAdapter adapter = new RestauranteAdapter(this, getRestauranteList());
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(adapter);
    }

    private List<Restaurante> getRestauranteList() {
        // Obtener desde bd
        List<Restaurante> restaurantList  = new ArrayList<>();
        // Datos de la lista
        List<String> categorias1 = Arrays.asList("Burger", "Chicken", "Ribs", "Wings");
        List<String> categorias2 = Arrays.asList("Burgers", "Fries", "Shakes");
        List<String> categorias3 = Arrays.asList("Pizza", "Pasta", "Salads");
        List<String> categorias4 = Arrays.asList("Sushi", "Rolls", "Noodles");

        // Añadir restaurantes a la lista
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.7, Arrays.asList("restaurant_image","burger_image"), null));
        restaurantList.add(new Restaurante("Burger Place", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias2, 4.5, Arrays.asList("burger_image"), null));
        restaurantList.add(new Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias3, 4.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 4.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.6, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias2, 2.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 1.6, Arrays.asList("burger_image"), null));
        restaurantList.add(new Restaurante("Italian Bistro", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias3, 2.8, Arrays.asList("restaurant_image"), null));
        restaurantList.add(new Restaurante("Sushi House", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias4, 3.6, Arrays.asList("burger_image"), null));


        return restaurantList ;
    }


}