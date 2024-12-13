package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.RestauranteAdapter;
import com.example.foodisea.databinding.ActivitySuperAdminGestionRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSelectionBinding;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.RestauranteRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperAdminGestionRestauranteActivity extends AppCompatActivity {
    ActivitySuperAdminGestionRestauranteBinding binding;
    RestauranteRepository restauranteRepository;

    List<Restaurante> listaRestaurantes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperAdminGestionRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnAddRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(SuperAdminGestionRestauranteActivity.this, SuperAdminAgregarRestauranteActivity.class);
            startActivity(intent);
        });

        binding.btnHome.setOnClickListener(v-> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });

        restauranteRepository = new RestauranteRepository();
        cargarListaRestaurantes();

    }

    private void cargarListaRestaurantes() {
        restauranteRepository.getRestaurantesActivos()
                .addOnSuccessListener(restaurantes ->{
                    listaRestaurantes = restaurantes;
                    // Configurar el adaptador
                    RestauranteAdapter adapter = new RestauranteAdapter(this, listaRestaurantes);
                    binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
                    binding.rvRestaurants.setAdapter(adapter);
                })
                .addOnFailureListener( e-> {
                        Toast.makeText(this, "Error al cargar restaurantes: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                });

    }
}