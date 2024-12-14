package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClienteProductoBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.CarritoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClienteProductoActivity extends AppCompatActivity {
    private ActivityClienteProductoBinding binding;
    private CarritoRepository carritoRepository;
    private ProductoRepository productoRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;
    private String restauranteId;
    private Cliente clienteActual;
    private Producto currentProduct;
    private int quantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        // Inicializar repositories y managers
        carritoRepository = new CarritoRepository();
        productoRepository = new ProductoRepository();
        sessionManager = SessionManager.getInstance(this);
        loadingDialog = new LoadingDialog(this);

        // Obtener datos del intent
        String productoId = getIntent().getStringExtra("productoId");
        restauranteId = getIntent().getStringExtra("restauranteId");

        if (productoId == null || restauranteId == null) {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar el ciclo de vida del carrusel
        binding.carousel.registerLifecycle(getLifecycle());

        // Configurar botones y listeners
        setupListeners();
        setupWindowInsets();

        // Cargar datos del producto
        cargarProducto(productoId);

        // Actualizar contador del carrito
        actualizarContadorCarrito();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarProducto(String productoId) {
        loadingDialog.show("Cargando producto...");

        productoRepository.getProductoById(productoId)
                .addOnSuccessListener(producto -> {
                    loadingDialog.dismiss();
                    currentProduct = producto;

                    // Cargar imágenes en el carrusel
                    List<CarouselItem> carouselItems = new ArrayList<>();
                    for (String imageUrl : producto.getImagenes()) {
                        carouselItems.add(new CarouselItem(imageUrl));
                    }
                    binding.carousel.setData(carouselItems);

                    // Actualizar resto de la UI
                    binding.productDetailName.setText(producto.getNombre());
                    binding.productDetailPrice.setText(String.format(Locale.getDefault(), "S/.%.2f", producto.getPrecio()));
                    binding.tvDescripcionProduct.setText(producto.getDescripcion());

                    // Verificar si el producto está agotado
                    if (producto.isOutOfStock()) {
                        binding.btnAddToCart.setEnabled(false);
                        binding.btnAddToCart.setText("Agotado");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar el producto: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupListeners() {
        // Botón volver
        binding.btnBack.setOnClickListener(v -> finish());

        // Botón carrito
        binding.btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteCarritoActivity.class);
            intent.putExtra("restauranteId",restauranteId);
            startActivity(intent);
        });

        // Botones de cantidad
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                actualizarBotonAgregar();
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
            actualizarBotonAgregar();
        });

        // Botón agregar al carrito
        binding.btnAddToCart.setOnClickListener(v -> agregarAlCarrito());
    }

    private void actualizarBotonAgregar() {
        binding.btnAddToCart.setEnabled(quantity > 0);
    }

    private void agregarAlCarrito() {
        if (currentProduct == null || quantity <= 0) return;

        clienteActual = sessionManager.getClienteActual();
        if (clienteActual == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show("Agregando al carrito...");

        carritoRepository.agregarProductoAlCarrito(clienteActual.getId(), restauranteId, currentProduct, quantity)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
                    actualizarContadorCarrito();
                    quantity = 0;
                    binding.tvQuantity.setText("0");
                    actualizarBotonAgregar();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("diferentes restaurantes")) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Carrito con productos de otro restaurante")
                                .setMessage("Ya tienes productos de otro restaurante en tu carrito. ¿Deseas vaciar el carrito actual y agregar este producto?")
                                .setPositiveButton("Aceptar", (dialog, which) -> {
                                    limpiarCarritoYAgregarProducto();
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void limpiarCarritoYAgregarProducto() {
        Cliente clienteActual = sessionManager.getClienteActual();
        if (clienteActual == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show("Limpiando carrito...");

        carritoRepository.limpiarCarrito(clienteActual.getId(), restauranteId)
                .addOnSuccessListener(aVoid -> agregarAlCarrito())
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al limpiar el carrito: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarContadorCarrito() {
        clienteActual = sessionManager.getClienteActual();
        if (clienteActual == null) return;

        carritoRepository.obtenerCarritoActivo(clienteActual.getId(),restauranteId)
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
        actualizarContadorCarrito();
    }
}