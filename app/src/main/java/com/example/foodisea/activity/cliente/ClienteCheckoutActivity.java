package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.CarritoAdapter;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClienteCheckoutBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.CarritoRepository;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClienteCheckoutActivity extends AppCompatActivity {
    private ActivityClienteCheckoutBinding binding;
    private CarritoAdapter checkoutAdapter;
    private CarritoRepository carritoRepository;
    private ProductoRepository productoRepository;
    private LoadingDialog loadingDialog;
    private Cliente clienteActual;
    private double totalConsumption = 0.0;
    private static final double DELIVERY_FEE = 15.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializaciones
        carritoRepository = new CarritoRepository();
        productoRepository = new ProductoRepository();
        loadingDialog = new LoadingDialog(this);
        clienteActual = SessionManager.getInstance(this).getClienteActual();

        if (clienteActual == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar botones y direcciones
        setupViews();

        // Cargar datos
        cargarCarrito();
    }

    private void setupRecyclerView() {
        checkoutAdapter = new CarritoAdapter(this, new ArrayList<>(), true, null);
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderItemsRecyclerView.setAdapter(checkoutAdapter);
    }

    private void setupViews() {
        // Cargar dirección del cliente
        binding.tvAddress.setText(clienteActual.getDireccion());

        // Configurar listeners
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEditOrder.setOnClickListener(v -> {
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });

        binding.btnEditAddress.setOnClickListener(v -> {
            // TODO: Implementar edición de dirección
            Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show();
        });

        binding.btnAccept.setOnClickListener(v -> confirmarPedido());
    }

    private void cargarCarrito() {
        loadingDialog.show("Cargando resumen...");

        carritoRepository.obtenerCarritoActivo(clienteActual.getId())
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Carrito carrito = documentSnapshot.toObject(Carrito.class);
                    if (carrito == null || carrito.getProductos() == null || carrito.getProductos().isEmpty()) {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Cargar productos
                    List<Task<Producto>> productoTasks = new ArrayList<>();
                    for (ProductoCantidad pc : carrito.getProductos()) {
                        productoTasks.add(productoRepository.getProductoById(pc.getProductoId()));
                    }

                    Tasks.whenAllComplete(productoTasks)
                            .addOnSuccessListener(tasks -> {
                                List<CarritoAdapter.CarritoItem> cartItems = new ArrayList<>();
                                for (int i = 0; i < tasks.size(); i++) {
                                    Task<Producto> task = productoTasks.get(i);
                                    if (task.isSuccessful()) {
                                        Producto producto = task.getResult();
                                        if (producto != null) {
                                            cartItems.add(new CarritoAdapter.CarritoItem(
                                                    carrito.getProductos().get(i), producto
                                            ));
                                        }
                                    }
                                }

                                checkoutAdapter.updateItems(cartItems);
                                totalConsumption = carrito.getTotal();
                                actualizarTotales();
                                loadingDialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Error al cargar productos: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar carrito: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarTotales() {
        // Total consumo
        binding.tvTotalConsumo.setText(String.format(Locale.getDefault(), "S/. %.2f", totalConsumption));

        // Delivery
        binding.tvDelivery.setText(String.format(Locale.getDefault(), "S/. %.2f", DELIVERY_FEE));

        // Total final
        double totalFinal = totalConsumption + DELIVERY_FEE;
        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", totalFinal));
    }

    private void confirmarPedido() {
        // Verificar dirección
        if (binding.tvAddress.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una dirección de entrega",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar pedido")
                .setMessage("¿Estás seguro de que deseas realizar este pedido?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    crearPedido();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearPedido() {
        loadingDialog.show("Procesando pedido...");

        carritoRepository.obtenerCarritoActivo(clienteActual.getId())
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Error: Carrito no encontrado",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Carrito carrito = documentSnapshot.toObject(Carrito.class);
                    if (carrito == null) {
                        loadingDialog.dismiss();
                        return;
                    }

                    // Crear nuevo pedido
                    Pedido pedido = new Pedido();
                    pedido.setClienteId(clienteActual.getId());
                    pedido.setRestauranteId(carrito.getRestauranteId());
                    pedido.setProductos(carrito.getProductos());
                    pedido.setEstado("Recibido");
                    pedido.setFechaPedido(new Date());
                    pedido.setDireccionEntrega(binding.tvAddress.getText().toString());
                    pedido.setMontoTotal(totalConsumption + DELIVERY_FEE);
                    // El repartidor se asignará después
                    pedido.setRepartidorId(null);
                    // Estos campos se llenarán más tarde
                    pedido.setCodigoQrId(null);
                    pedido.setPagoId(null);

                    // Guardar pedido usando PedidoRepository
                    PedidoRepository pedidoRepository = new PedidoRepository(this);
                    pedidoRepository.crearPedido(pedido)
                            .addOnSuccessListener(aVoid -> {
                                // Limpiar carrito después de crear el pedido exitosamente
                                carritoRepository.limpiarCarrito(clienteActual.getId())
                                        .addOnSuccessListener(aVoid2 -> {
                                            loadingDialog.dismiss();
                                            // Ir a pantalla de confirmación
                                            Intent intent = new Intent(this, ConfirmacionPedido.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingDialog.dismiss();
                                            Toast.makeText(this, "Error al limpiar carrito: " +
                                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Error al crear pedido: " +
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al obtener carrito: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCarrito();
    }
}