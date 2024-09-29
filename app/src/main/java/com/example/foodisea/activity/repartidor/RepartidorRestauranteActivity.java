package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.PedidosAdapter;
import com.example.foodisea.databinding.ActivityRepartidorRestauranteBinding;
import com.example.foodisea.entity.Pedido;

import java.util.ArrayList;
import java.util.List;

public class RepartidorRestauranteActivity extends AppCompatActivity {

    private ActivityRepartidorRestauranteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // binding
        binding = ActivityRepartidorRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar insets del sistema para la ventana
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar RecyclerView para mostrar los pedidos
        binding.rvPedidos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        List<Pedido> pedidosList = getPedidosList(); // Método para obtener los datos de pedidos
        PedidosAdapter adapter = new PedidosAdapter(this, pedidosList);
        binding.rvPedidos.setAdapter(adapter);

        // Configurar el título con la cantidad de pedidos
        binding.tvPedidosTitle.setText(String.format("Pedidos (%d)", pedidosList.size()));

        // Obtener los datos pasados desde el intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("name");
        float restaurantRating = intent.getFloatExtra("rating", 0);
        int restaurantImage = intent.getIntExtra("image", R.drawable.restaurant_image);
        String restaurantLocation = intent.getStringExtra("location");

        // Asignar los datos del restaurante al UI
        binding.tvRestaurantName.setText(restaurantName);
        binding.tvRestaurantAddress.setText(restaurantLocation);

        // Iniciar los botones
        setupButtonListeners();

    }

    private List<Pedido> getPedidosList() {
        // Obtener desde bd
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(new Pedido("#162432", "John Gomez", "2118 Thornridge Cir. Syracuse", 15.0));
        pedidos.add(new Pedido("#182432", "David Fernandez", "2118 Thornridge Cir. Syracuse", 25.0));
        pedidos.add(new Pedido("#202432", "Juan Perez", "2118 Thornridge Cir. Syracuse", 15.0));
        pedidos.add(new Pedido("#262432", "Maria Diaz", "2118 Thornridge Cir. Syracuse", 15.0));
        return pedidos;
    }

    private void setupButtonListeners() {

        //Botón para regresar
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        //Botón de favoritos
        binding.btnFavorite.setOnClickListener(v -> {
            //...
        });

    }
}