package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.cliente.DetallePedidoAdapter;
import com.example.foodisea.databinding.ActivityClienteCompraDetailsBinding;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.item.OrderItem;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ClienteCompraDetailsActivity extends AppCompatActivity {
    private ActivityClienteCompraDetailsBinding binding;
    private PedidoRepository pedidoRepository;
    private DetallePedidoAdapter detallePedidoAdapter;
    private String pedidoId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteCompraDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase y Repository
        db = FirebaseFirestore.getInstance();
        pedidoRepository = new PedidoRepository(this);
        pedidoId = getIntent().getStringExtra("pedidoId");

        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el ID del pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        cargarPedido();
    }

    private void setupUI() {
        // Configurar RecyclerView
        detallePedidoAdapter = new DetallePedidoAdapter(new ArrayList<>());
        binding.orderItemsRecyclerView.setAdapter(detallePedidoAdapter);
        binding.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar botones
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmEntrega.setOnClickListener(v -> {
            Intent comprobante = new Intent(this, ClienteComprobanteQrActivity.class);
            comprobante.putExtra("pedidoId", pedidoId);
            startActivity(comprobante);
        });
    }

    private void cargarPedido() {
        pedidoRepository.getPedidoConDetalles(pedidoId)
                .addOnSuccessListener(pedidoConDetalles -> {
                    if (pedidoConDetalles != null) {
                        actualizarUI(pedidoConDetalles);
                        cargarProductos(pedidoConDetalles.getPedido().getProductos());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el pedido: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarProductos(List<ProductoCantidad> productosCantidad) {
        List<Task<DocumentSnapshot>> productTasks = new ArrayList<>();

        for (ProductoCantidad pc : productosCantidad) {
            Task<DocumentSnapshot> productTask = db.collection("productos")
                    .document(pc.getProductoId())
                    .get();
            productTasks.add(productTask);
        }

        Tasks.whenAllSuccess(productTasks)
                .addOnSuccessListener(documentSnapshots -> {
                    List<OrderItem> orderItems = new ArrayList<>();
                    for (int i = 0; i < documentSnapshots.size(); i++) {
                        DocumentSnapshot doc = (DocumentSnapshot) documentSnapshots.get(i);
                        Producto producto = doc.toObject(Producto.class);
                        if (producto != null) {
                            producto.setId(doc.getId());
                            ProductoCantidad pc = productosCantidad.get(i);

                            String imageUrl = null;
                            if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
                                imageUrl = producto.getImagenes().get(0);  // Tomamos la primera imagen
                            }

                            orderItems.add(new OrderItem(
                                    producto.getNombre(),
                                    producto.getPrecio(),
                                    pc.getCantidad(),
                                    imageUrl
                            ));
                        }
                    }
                    detallePedidoAdapter.setItems(orderItems);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los productos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarUI(PedidoConDetalles pedidoConDetalles) {
        Pedido pedido = pedidoConDetalles.getPedido();

        // Actualizar dirección
        binding.tvAddress.setText(pedido.getDireccionEntrega());

        // Actualizar montos
        double totalConsumo = pedido.getMontoTotal() - 15.0; // Asumiendo delivery fijo de 15
        binding.tvTotalConsumo.setText(String.format("S/. %.2f", totalConsumo));
        binding.tvDelivery.setText("S/. 15.00");
        binding.tvTotalPago.setText(String.format("S/. %.2f", pedido.getMontoTotal()));

        // Mostrar/ocultar botón de confirmar entrega
        binding.btnConfirmEntrega.setVisibility(
                "En camino".equals(pedido.getEstado()) ? View.VISIBLE : View.GONE
        );
    }
}