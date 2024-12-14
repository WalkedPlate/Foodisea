package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.adminRes.CartaAdapter;
import com.example.foodisea.adapter.adminRes.ProductoDetalleAdapter;
import com.example.foodisea.databinding.ActivityAdminResDetallesPedidosBinding;
import com.example.foodisea.dto.ProductoClaseCantidad;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.ProductoRepository;
import com.github.mikephil.charting.charts.BarChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdminResDetallesPedidosActivity extends AppCompatActivity {

    ActivityAdminResDetallesPedidosBinding binding;
    private ProductoDetalleAdapter adapter;
    private List<ProductoCantidad> listaIdProductosCantidad = new ArrayList<>();
    private ProductoRepository productoRepository;
    private HashMap<String, Producto> mapaProductoIdAProducto = new HashMap<>();
    private List<ProductoClaseCantidad> listaProductosCantidad = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        String idPedido = intent.getStringExtra("idPedido");
        String direccionDestino = intent.getStringExtra("direccionDestino");
        String nombreCliente = intent.getStringExtra("nombreCliente");
        String telefono = intent.getStringExtra("telefono");
        String metodoPago = intent.getStringExtra("metodoPago");
        String estadoPago = intent.getStringExtra("estadoPago");
        Double montoPedido = intent.getDoubleExtra("montoPedido",0.0);
        listaIdProductosCantidad = (List<ProductoCantidad>) intent.getSerializableExtra("listaIdProductosCantidad");

        super.onCreate(savedInstanceState);
        binding = ActivityAdminResDetallesPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.idPedido.setText(idPedido);
        binding.tvAddress.setText(direccionDestino);
        binding.idNombreCliente.setText(nombreCliente);
        binding.idTelefono.setText(telefono);
        binding.montoPedido.setText("s/. " + montoPedido);
        Double precioDelivery = 4.0;
        Double montoTotal = montoPedido + precioDelivery;
        binding.montoTotal.setText("s/. " + montoTotal);
        binding.btnBack.setOnClickListener(view -> {
            Intent pedidos = new Intent(this, AdminResPedidosActivity.class);
            startActivity(pedidos);
        });

        productoRepository = new ProductoRepository();
        cargarMapaProductos();

    }

    private void crearListaProductosClaseCantidad() {
        for (ProductoCantidad productoCantidad : listaIdProductosCantidad) {
            if (mapaProductoIdAProducto.containsKey(productoCantidad.getProductoId())) {
                ProductoClaseCantidad productoClaseCantidad = new ProductoClaseCantidad();
                productoClaseCantidad.setProducto(mapaProductoIdAProducto.get(productoCantidad.getProductoId()));
                productoClaseCantidad.setCantidad(productoCantidad.getCantidad());
                listaProductosCantidad.add(productoClaseCantidad);
            }
        }
    }


    private void setupRecyclerView() {
        adapter = new ProductoDetalleAdapter();
        adapter.setContext(AdminResDetallesPedidosActivity.this);


        adapter.setListaProductosCantidad(listaProductosCantidad); // Inicializar con lista vacía

        binding.rvProductos.setAdapter(adapter);
        binding.rvProductos.setLayoutManager(new LinearLayoutManager(AdminResDetallesPedidosActivity.this));
    }

    // Obtiene la lista de productos para construir el mapa de ID a nombre
    public void cargarMapaProductos() {
        productoRepository.obtenerProductosPorRestaurante("REST001") // Método que obtenga todos los productos
                .addOnSuccessListener(productos -> {
                    for (Producto producto : productos) {
                        mapaProductoIdAProducto.put(producto.getId(), producto);
                    }
                    crearListaProductosClaseCantidad();
                    setupRecyclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", e.getMessage());
                });
    }



}