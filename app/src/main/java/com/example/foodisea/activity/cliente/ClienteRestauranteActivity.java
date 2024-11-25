package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.PlatoAdapter;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClienteRestauranteBinding;
import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.CarritoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClienteRestauranteActivity extends AppCompatActivity {

    ActivityClienteRestauranteBinding binding;
    private FirebaseStorage storage;
    private String restauranteId;
    private ProductoRepository productoRepository;
    private PlatoAdapter adapter;
    private SessionManager sessionManager;
    private Cliente clienteActual;
    private List<Producto> listaCompletaProductos = new ArrayList<>();
    private String categoriaSeleccionada = "TODOS";
    private CarritoRepository carritoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar componentes pricipales
        storage = FirebaseStorage.getInstance();
        productoRepository = new ProductoRepository();
        sessionManager = SessionManager.getInstance(this);
        clienteActual = sessionManager.getClienteActual();
        carritoRepository = new CarritoRepository();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent
        Intent intent = getIntent();
        restauranteId = intent.getStringExtra("restauranteId");
        String restaurantName = intent.getStringExtra("name");
        double restaurantRating = intent.getDoubleExtra("rating", 0);
        String imageRef = intent.getStringExtra("image");
        String restaurantDesc = intent.getStringExtra("descripcion");

        // Configurar la vista con los datos recibidos
        loadRestaurantImage(imageRef);
        binding.tvRestaurantName.setText(restaurantName);
        binding.tvRestaurantRating.setText(String.valueOf(restaurantRating));
        binding.tvDescripcionRest.setText(restaurantDesc);

        setupRecyclerView();
        setupCategoriaListeners();
        cargarProductos();

        // Configurar botones
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCart.setOnClickListener(v -> {
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            carrito.putExtra("restauranteId", restauranteId);
            startActivity(carrito);
        });

        // Actualizar contador del carrito
        actualizarContadorCarrito();
    }

    private void setupRecyclerView() {
        adapter = new PlatoAdapter(this, new ArrayList<>());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvProducts.setLayoutManager(gridLayoutManager);
        binding.rvProducts.setAdapter(adapter);
    }

    private void cargarProductos() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvProducts.setVisibility(View.GONE);

        productoRepository.obtenerProductosPorRestaurante(restauranteId)
                .addOnSuccessListener(productos -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.rvProducts.setVisibility(View.VISIBLE);
                    listaCompletaProductos = productos;
                    filtrarProductos();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.rvProducts.setVisibility(View.VISIBLE);
                    binding.tvNoProducts.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupCategoriaListeners() {
        binding.btnTodos.setOnClickListener(v -> cambiarCategoria("TODOS", binding.btnTodos));
        binding.btnPlatos.setOnClickListener(v -> cambiarCategoria("Plato", binding.btnPlatos));
        binding.btnBebidas.setOnClickListener(v -> cambiarCategoria("Bebida", binding.btnBebidas));

        // Resaltar TODOS por defecto
        binding.btnTodos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_dark)));
        binding.btnTodos.setTextColor(ColorStateList.valueOf(getColor(R.color.white)));
    }

    private void cambiarCategoria(String categoria, Button botonSeleccionado) {
        // Resetear botones
        binding.btnTodos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnTodos.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));
        binding.btnPlatos.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnPlatos.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));
        binding.btnBebidas.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_light)));
        binding.btnBebidas.setTextColor(ColorStateList.valueOf(getColor(R.color.black)));

        // Resaltar seleccionado
        botonSeleccionado.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.btn_dark)));
        botonSeleccionado.setTextColor(ColorStateList.valueOf(getColor(R.color.white)));

        categoriaSeleccionada = categoria;
        filtrarProductos();
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

        adapter.actualizarProductos(productosFiltrados);
        binding.tvNoProducts.setVisibility(productosFiltrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadRestaurantImage(String imageRef) {
        if (imageRef != null && !imageRef.isEmpty()) {
            if (imageRef.startsWith("https://")) {
                // Si ya es una URL completa, cargar directamente
                loadImageWithGlide(imageRef);
            } else {
                // Si es una referencia de Storage, obtener la URL de descarga
                StorageReference storageRef = storage.getReference().child(imageRef);
                storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> loadImageWithGlide(uri.toString()))
                        .addOnFailureListener(e -> {
                            binding.ivRestaurantImage.setImageResource(R.drawable.restaurant_image);
                            Log.e("ClienteRestauranteActivity", "Error loading image", e);
                        });
            }
        } else {
            binding.ivRestaurantImage.setImageResource(R.drawable.restaurant_image);
        }
    }

    private void loadImageWithGlide(String imageUrl) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.restaurant_image)
                .error(R.drawable.restaurant_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.ivRestaurantImage);
    }

    private void actualizarContadorCarrito() {

        carritoRepository.obtenerCarritoActivo(clienteActual.getId())
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Carrito carrito = documentSnapshot.toObject(Carrito.class);
                        if (carrito != null && carrito.getProductos() != null) {
                            int totalItems = 0;
                            for (ProductoCantidad pc : carrito.getProductos()) {
                                totalItems += pc.getCantidad();
                            }
                            binding.tvCartItemCount.setText(String.valueOf(totalItems));
                            binding.tvCartItemCount.setVisibility(totalItems > 0 ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        binding.tvCartItemCount.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar contador del carrito
        actualizarContadorCarrito();
    }
}