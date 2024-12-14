package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.CarritoAdapter;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.databinding.ActivityClienteCarritoBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.CarritoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClienteCarritoActivity extends AppCompatActivity implements CarritoAdapter.CarritoItemListener {
    private ActivityClienteCarritoBinding binding;
    private CarritoAdapter adapter;
    private CarritoRepository carritoRepository;
    private ProductoRepository productoRepository;
    private Cliente clienteActual;
    private LoadingDialog loadingDialog;
    private List<CarritoAdapter.CarritoItem> cartItems = new ArrayList<>();
    private double totalCarrito = 0.0;
    private String restauranteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteCarritoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener restauranteId del intent
        restauranteId = getIntent().getStringExtra("restauranteId");
        if (restauranteId == null) {
            Toast.makeText(this, "Error: ID de restaurante no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar
        carritoRepository = new CarritoRepository();
        productoRepository = new ProductoRepository();
        loadingDialog = new LoadingDialog(this);
        clienteActual = SessionManager.getInstance(this).getClienteActual();

        if (clienteActual == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWindowInsets();
        setupRecyclerView();
        setupListeners();
        cargarCarrito();
    }
    /**
     * Configura los insets de la ventana
     */
    private void setupWindowInsets() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void setupRecyclerView() {
        adapter = new CarritoAdapter(this, cartItems, false, this);
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent checkout = new Intent(this, ClienteCheckoutActivity.class);
            checkout.putExtra("restauranteId", restauranteId); // Pasar el restauranteId
            startActivity(checkout);
        });
    }

    private void cargarCarrito() {
        loadingDialog.show("Cargando carrito...");

        // Obtener restauranteId del intent
        String restauranteId = getIntent().getStringExtra("restauranteId");

        carritoRepository.obtenerCarritoActivo(clienteActual.getId(),restauranteId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Carrito carrito = documentSnapshot.toObject(Carrito.class);
                        if (carrito != null && carrito.getProductos() != null) {
                            cargarProductosCarrito(carrito.getProductos());
                            totalCarrito = carrito.getTotal();
                            actualizarTotal();
                        } else {
                            mostrarCarritoVacio();
                        }
                    } else {
                        mostrarCarritoVacio();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar el carrito: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarProductosCarrito(List<ProductoCantidad> productos) {
        List<Task<Producto>> productoTasks = new ArrayList<>();

        for (ProductoCantidad pc : productos) {
            Task<Producto> task = productoRepository.getProductoById(pc.getProductoId());
            productoTasks.add(task);
        }

        Tasks.whenAllComplete(productoTasks)
                .addOnSuccessListener(tasks -> {
                    cartItems.clear();
                    for (int i = 0; i < tasks.size(); i++) {
                        Task<Producto> task = productoTasks.get(i);
                        if (task.isSuccessful()) {
                            Producto producto = task.getResult();
                            if (producto != null) {
                                cartItems.add(new CarritoAdapter.CarritoItem(
                                        productos.get(i), producto
                                ));
                            }
                        }
                    }
                    adapter.updateItems(cartItems);
                    loadingDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarCarritoVacio() {
        loadingDialog.dismiss();
        cartItems.clear();
        adapter.updateItems(cartItems);
        totalCarrito = 0.0;
        actualizarTotal();
        binding.btnCheckout.setEnabled(false);
    }

    private void actualizarTotal() {
        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", totalCarrito));
    }

    @Override
    public void onQuantityChanged(String productoId, int newQuantity) {
        loadingDialog.show("Actualizando cantidad...");

        double precioProducto = 0.0;
        for (CarritoAdapter.CarritoItem item : cartItems) {
            if (item.getProducto().getId().equals(productoId)) {
                precioProducto = item.getProducto().getPrecio();
                break;
            }
        }

        carritoRepository.actualizarCantidadProducto(clienteActual.getId(),
                        restauranteId, productoId, newQuantity, precioProducto)
                .addOnSuccessListener(aVoid -> cargarCarrito())
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al actualizar cantidad: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteItem(String productoId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Estás seguro de que quieres eliminar este producto del carrito?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    loadingDialog.show("Eliminando producto...");
                    carritoRepository.eliminarProductoDelCarrito(clienteActual.getId(),
                                    restauranteId, productoId)
                            .addOnSuccessListener(aVoid -> cargarCarrito())
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Error al eliminar producto: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clienteActual != null) {
            cargarCarrito();
        }
    }
}