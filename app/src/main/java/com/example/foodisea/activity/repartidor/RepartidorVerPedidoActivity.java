package com.example.foodisea.activity.repartidor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.ProductosPedidoAdapter;
import com.example.foodisea.databinding.ActivityRepartidorVerPedidoBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.ChatRepository;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RepartidorVerPedidoActivity extends AppCompatActivity {

    private ActivityRepartidorVerPedidoBinding binding;
    private PedidoRepository pedidoRepository;
    private ProductoRepository productoRepository;
    private LoadingDialog loadingDialog;
    private String pedidoId;
    private final ChatRepository chatRepository = new ChatRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupButtonListeners();

        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null) {
            Toast.makeText(this, "Error al cargar el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDetallesPedido();
    }

    private void initializeComponents() {
        // Primero inflar el binding y setear el content view
        binding = ActivityRepartidorVerPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Luego habilitar Edge to Edge y configurar insets
        EdgeToEdge.enable(this);
        setupWindowInsets();

        // Inicializar otros componentes
        pedidoRepository = new PedidoRepository(this);
        productoRepository = new ProductoRepository();
        loadingDialog = new LoadingDialog(this);

        // Configurar RecyclerView
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtonListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void cargarDetallesPedido() {
        loadingDialog.show("Cargando detalles del pedido...");

        pedidoRepository.getPedidoConDetalles(pedidoId)
                .addOnSuccessListener(pedidoConDetalles -> {
                    cargarDetallesProductos(pedidoConDetalles);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar los detalles del pedido",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void cargarDetallesProductos(PedidoConDetalles pedidoConDetalles) {
        List<ProductoCantidad> productosCantidad = pedidoConDetalles.getPedido().getProductos();
        Map<String, Producto> productosDetalles = new HashMap<>();
        List<Task<Void>> productTasks = new ArrayList<>();

        // Cargar detalles de cada producto
        for (ProductoCantidad pc : productosCantidad) {
            Task<Void> productTask = productoRepository.getProductoById(pc.getProductoId())
                    .continueWith(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            productosDetalles.put(pc.getProductoId(), task.getResult());
                        }
                        return null;
                    });
            productTasks.add(productTask);
        }

        // Cuando todos los productos estén cargados, mostrar la información completa
        Tasks.whenAllComplete(productTasks)
                .addOnSuccessListener(tasks -> {
                    loadingDialog.dismiss();
                    mostrarDetallesPedido(pedidoConDetalles, productosDetalles);
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDetallesPedido(PedidoConDetalles pedidoConDetalles,
                                       Map<String, Producto> productosDetalles) {
        Pedido pedido = pedidoConDetalles.getPedido();
        Restaurante restaurante = pedidoConDetalles.getRestaurante();

        // Información básica del pedido
        String pedidoId = pedido.getId();
        String displayId = pedidoId.length() >= 5
                ? pedidoId.substring(0, 5).toUpperCase()
                : pedidoId.toUpperCase();
        binding.orderNumberText.setText(String.format("Pedido #%s", displayId));

        // Información del restaurante
        binding.restaurantName.setText(restaurante.getNombre());
        binding.restaurantAddress.setText(restaurante.getDireccion());

        // Obtener información del cliente
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        usuarioRepository.getUserById(pedido.getClienteId())
                .addOnSuccessListener(usuario -> {
                    binding.customerName.setText(String.format("%s %s",
                            usuario.getNombres(), usuario.getApellidos()));
                })
                .addOnFailureListener(e -> {
                    binding.customerName.setText("Cliente no encontrado");
                });

        binding.customerAddress.setText(pedido.getDireccionEntrega());

        // Configurar RecyclerView para los productos
        ProductosPedidoAdapter adapter = new ProductosPedidoAdapter(
                pedido.getProductos(),
                productosDetalles
        );
        binding.orderItemsRecyclerView.setAdapter(adapter);

        // Mostrar el monto total
        binding.tvTotalPrice.setText(String.format("S/. %.2f", 15.00));

        // Configurar botón de aceptar pedido
        binding.startDeliveryBtn.setOnClickListener(v -> aceptarPedido(pedido));
    }


    private void aceptarPedido(Pedido pedido) {
        loadingDialog.show("Verificando disponibilidad...");
        String repartidorId = SessionManager.getInstance(this).getRepartidorActual().getId();
        String restauranteId = pedido.getRestauranteId();

        // Primero verificar si el repartidor está disponible
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        usuarioRepository.getUserById(repartidorId)
                .addOnSuccessListener(usuario -> {
                    // Convertir el usuario a repartidor
                    if (!(usuario instanceof Repartidor)) {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Error: Usuario no es un repartidor",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Repartidor repartidor = (Repartidor) usuario;
                    if (!"Disponible".equals(repartidor.getDisposicion())) {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "No puedes aceptar pedidos mientras estés ocupado",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Si está disponible, ejecutar todas las operaciones necesarias
                    Tasks.whenAllComplete(
                                    // Asignar repartidor al pedido
                                    pedidoRepository.asignarRepartidorAPedido(pedido.getId(), repartidorId),
                                    // Actualizar disposición del repartidor a Ocupado
                                    pedidoRepository.actualizarDisposicionRepartidor(repartidorId, "Ocupado")
                            )
                            .addOnSuccessListener(tasks -> {
                                // Crear el chat
                                chatRepository.crearChat(pedido.getId(), restauranteId, repartidorId, pedido.getClienteId())
                                        .addOnSuccessListener(chatId -> {
                                            loadingDialog.dismiss();
                                            Intent intent = new Intent(this, RepartidorDeliveryMapActivity.class);
                                            intent.putExtra("pedidoId", pedido.getId());
                                            intent.putExtra("chatId", chatId);
                                            intent.putExtra("clienteId", pedido.getClienteId());
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingDialog.dismiss();
                                            Toast.makeText(this, "Error al crear el chat",
                                                    Toast.LENGTH_SHORT).show();
                                            // Revertir cambios en caso de error
                                            revertirCambios(pedido.getId(), repartidorId);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Error al aceptar el pedido",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error al verificar disponibilidad",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Método auxiliar para revertir cambios en caso de error
    private void revertirCambios(String pedidoId, String repartidorId) {
        // Revertir estado del pedido y disposición del repartidor
        Tasks.whenAllComplete(
                pedidoRepository.asignarRepartidorAPedido(pedidoId, null), // Quitar asignación del repartidor
                pedidoRepository.actualizarDisposicionRepartidor(repartidorId, "Disponible")
        ).addOnFailureListener(e -> {
            Log.e("RepartidorVerPedido", "Error al revertir cambios", e);
        });
    }
}