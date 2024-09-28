package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.activity.cliente.ClienteCarritoActivity;
import com.example.foodisea.adapter.adminRes.CartaAdapter;
import com.example.foodisea.databinding.ActivityAdminResCartaBinding;
import com.example.foodisea.entity.Producto;

import java.util.ArrayList;
import java.util.List;

public class AdminResCartaActivity extends AppCompatActivity {

    ActivityAdminResCartaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResCartaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_res_carta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        // Inicializa la lista de productos
        List<Producto> productList = new ArrayList<>();
        // Agregar productos a la lista
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));
        productList.add(new Producto("Burger Ferguson", 40.00, R.drawable.burger, false));
        productList.add(new Producto("Rockin' Burgers", 45.00, R.drawable.burger, true));

        CartaAdapter adapter = new CartaAdapter();
        adapter.setContext(AdminResCartaActivity.this);
        adapter.setListaProductos(productList);

        // Configura el GridLayoutManager con 2 columnas
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columnas

        binding.rvProductos.setAdapter(adapter);
        binding.rvProductos.setLayoutManager(gridLayoutManager);

        // funcion de los botones
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

    }
}