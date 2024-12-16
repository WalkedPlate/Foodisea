package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.adminRes.CartaAdapter;
import com.example.foodisea.databinding.ActivityAdminResCartaBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Producto;
import com.example.foodisea.repository.ProductoRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminResCartaActivity extends AppCompatActivity {

    ActivityAdminResCartaBinding binding;
    private ProductoRepository productoRepository;
    private CartaAdapter adapter;
    private List<Producto> listaCompletaProductos = new ArrayList<>();
    private String categoriaSeleccionada = "TODOS"; // Por defecto
    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResCartaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();
        setupCategoriaListeners();

        // Inicializar repository
        productoRepository = new ProductoRepository();

        //Inicializar al admin
        sessionManager = SessionManager.getInstance(this);
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();

        // Resaltar el botón TODOS inicialmente
        binding.btnTodos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_dark)));
        binding.btnTodos.setTextColor(ColorStateList.valueOf(getColor(R.color.white)));
        cargarProductos();


        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminResNuevoProductoActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new CartaAdapter();
        adapter.setContext(AdminResCartaActivity.this);
        adapter.setListaPlatos(new ArrayList<>()); // Inicializar con lista vacía

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvProductos.setAdapter(adapter);
        binding.rvProductos.setLayoutManager(gridLayoutManager);
    }

    private void cargarProductos() {
        String restauranteId = administradorRestauranteActual.getRestauranteId();

        productoRepository.obtenerProductosPorRestaurante(restauranteId)
                .addOnSuccessListener(productos -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.rvProductos.setVisibility(View.VISIBLE);

                    // Guardar la lista completa
                    listaCompletaProductos = productos;
                    // Aplicar el filtro actual
                    filtrarProductos();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.rvProductos.setVisibility(View.VISIBLE);
                    binding.tvNoProducts.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupCategoriaListeners() {
        binding.btnTodos.setOnClickListener(v -> {
            cambiarCategoria("TODOS", binding.btnTodos);
        });

        binding.btnPlatos.setOnClickListener(v -> {
            cambiarCategoria("Plato", binding.btnPlatos);
        });

        binding.btnBebidas.setOnClickListener(v -> {
            cambiarCategoria("Bebida", binding.btnBebidas);
        });
    }

    private void cambiarCategoria(String categoria, Button botonSeleccionado) {
        // Resetear todos los botones a estilo normal
        binding.btnTodos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnTodos.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));
        binding.btnPlatos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnPlatos.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));
        binding.btnBebidas.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnBebidas.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));

        // Resaltar el botón seleccionado
        botonSeleccionado.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_dark)));
        botonSeleccionado.setTextColor(ColorStateList.valueOf(getColor(R.color.white)));

        categoriaSeleccionada = categoria;

        // Limpiar la lista actual
        adapter.setListaPlatos(new ArrayList<>());

        // Mostrar el loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvProductos.setVisibility(View.GONE);

        // Volver a cargar los productos con el nuevo filtro
        cargarProductos();
    }

    private void filtrarProductos() {
        List<Producto> productosFiltrados;
        if (categoriaSeleccionada.equals("TODOS")) {
            productosFiltrados = new ArrayList<>(listaCompletaProductos);
        } else {
            productosFiltrados = listaCompletaProductos.stream()
                    .filter(producto -> producto.getCategoria().equals(categoriaSeleccionada))
                    .collect(Collectors.toList());
        }

        adapter.setListaPlatos(productosFiltrados);

        // Mostrar mensaje si no hay productos
        binding.tvNoProducts.setVisibility(
                productosFiltrados.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos(); // Recargar productos cuando vuelvas a la actividad
    }
}