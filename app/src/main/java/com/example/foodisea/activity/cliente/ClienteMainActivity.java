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
        List<String> categorias5 = Arrays.asList("Parillada", "Anticuchos");
        List<String> categorias6 = Arrays.asList("Japonesa", "Coreana");
        List<String> categorias7 = Arrays.asList("Pasta", "Italiana", "Salads");
        List<String> categorias8 = Arrays.asList("Tacos", "Burritos", "Chile");


        // Añadir restaurantes a la lista
        restaurantList.add(new Restaurante("Rose Garden Restaurant", "2118 Thornridge Cir. Syracuse", "123-456-7890", categorias1, 4.7, Arrays.asList("restaurant_image"), null, "Restaurante especializado en hamburguesas artesanales, donde la calidad de los ingredientes" +
                "        y el sabor único se combinan para ofrecer una experiencia culinariainigualable."));
        restaurantList.add(new Restaurante("Burger Place", "3118 Thornridge Cir. Syracuse", "123-456-7891", categorias2, 4.5, Arrays.asList("burger_image"), null, "Disfruta de las hamburguesas más jugosas y deliciosas, preparadas con ingredientes frescos y pan artesanal. "));
        restaurantList.add(new Restaurante("Italian Bistro", "4118 Thornridge Cir. Syracuse", "123-456-7892", categorias3, 2.8, Arrays.asList("bistro"), null, "Sumérgete en los sabores auténticos de Italia con nuestras pastas frescas, pizzas al horno de leña y salsas caseras."+
                "Disfruta de un ambiente acogedor y de una experiencia culinaria inolvidable. "));
        restaurantList.add(new Restaurante("Sushi House", "5118 Thornridge Cir. Syracuse", "123-456-7893", categorias4, 4.6, Arrays.asList("sushi_house"), null,"Descubre la frescura y el arte del sushi en cada bocado. Ofrecemos rollos tradicionales y creativos, preparados con los mejores ingredientes. ¡Una auténtica experiencia japonesa en un solo lugar!"));
        restaurantList.add(new Restaurante("Parilla Grill", "6118 Thornridge Cir. Syracuse", "123-456-7894", categorias5, 3.6, Arrays.asList("grill"), null, "Carnes a la brasa, jugosas y llenas de sabor, preparadas al punto perfecto. " +
                "Disfruta de cortes seleccionados y un ambiente cálido para los amantes de la parrillada. ¡Una experiencia a la parrilla única!"));
        restaurantList.add(new Restaurante("Sushi Zen", "7118 Thornridge Cir. Syracuse", "123-456-7895", categorias6, 4.6, Arrays.asList("sushi_zen"), null, "Deléitate con una variedad de sushi fresco y de calidad excepcional, cuidadosamente preparado por expertos. Desde nigiris hasta rolls especiales, cada plato es una obra maestra de sabor. " +
                "¡Vive la esencia de la cocina japonesa!"));
        restaurantList.add(new Restaurante("Bella Pasta", "8118 Thornridge Cir. Syracuse", "123-456-7896", categorias7, 2.8, Arrays.asList("bella_pasta"), null, "El lugar donde cada plato está preparado con ingredientes de la más alta calidad y siguiendo recetas tradicionales. ¡El rincón perfecto para los amantes de la cocina italiana!"));
        restaurantList.add(new Restaurante("Taquito Feliz", "9118 Thornridge Cir. Syracuse", "123-456-7897", categorias8, 1.6, Arrays.asList("taquitp"), null, "Auténticos tacos mexicanos con tortillas frescas y los mejores ingredientes. Sabores tradicionales y opciones irresistibles para todos los gustos. ¡Disfruta cada mordida!"));


        return restaurantList ;
    }


}