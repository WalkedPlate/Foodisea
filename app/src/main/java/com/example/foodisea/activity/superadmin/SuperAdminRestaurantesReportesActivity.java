package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminRestaurantesReportesBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSolicitudesRepartidorBinding;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.ProductoRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuperAdminRestaurantesReportesActivity extends AppCompatActivity {

    ActivitySuperAdminRestaurantesReportesBinding binding;
    private PedidoRepository pedidoRepository;
    private ProductoRepository productoRepository;
    private List<PedidoConCliente> listaPedidosConCliente = new ArrayList<>();
    private HashMap<String, String> mapaProductoIdANombre = new HashMap<>();
    private Double promedio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        Intent intent = getIntent();
        String restauranteId = intent.getStringExtra("restauranteId");

        pedidoRepository=new PedidoRepository(this);
        productoRepository=new ProductoRepository();
        cargarMapaProductos(restauranteId);
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySuperAdminRestaurantesReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnHome.setOnClickListener(v-> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });
    }

    private void setupBarChart(BarChart chart, Map<String, Integer> data, String label) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.invalidate(); // Refrescar gráfico
    }

    // Obtiene la lista de pedidos y genera el resumen con nombres de productos
    public void obtenerListaPedidos(String restauranteId) {
        BarChart barChartPlatos = findViewById(R.id.barChartPlatos);
        pedidoRepository.getPedidosActivosRestaurante(restauranteId)
                .addOnSuccessListener(pedidos -> {
                    // Guardar la lista completa
                    listaPedidosConCliente = pedidos;

                    // Sumar cantidades y usar nombres de productos
                    HashMap<String, Integer> listaProductoConCantidadTotal = sumaCantidadProductosConNombres(listaPedidosConCliente);

                    //Obtiene promedio:
                    promedio=obtenerGananciaPromedioPorOrden();


                    // Configurar el gráfico de barras con los nombres en lugar de IDs
                    setupBarChart(barChartPlatos, listaProductoConCantidadTotal, "Ventas por Plato");

                    binding.nroOrdenesTotales.setText(String.valueOf(listaPedidosConCliente.size()));
                    binding.gananciaPromedio.setText( "S/ "+ promedio);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar pedidos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", e.getMessage());
                });
    }

    // Obtiene la lista de productos para construir el mapa de ID a nombre
    public void cargarMapaProductos(String restauranteId) {
        productoRepository.obtenerProductosPorRestaurante(restauranteId) // Método que obtenga todos los productos
                .addOnSuccessListener(productos -> {
                    for (Producto producto : productos) {
                        mapaProductoIdANombre.put(producto.getId(), producto.getNombre());
                    }
                    // Una vez que el mapa esté cargado, puedes llamar a obtenerListaPedidos
                    obtenerListaPedidos(restauranteId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", e.getMessage());
                });
    }

    // Método modificado para usar nombres de productos
    public HashMap<String, Integer> sumaCantidadProductosConNombres(List<PedidoConCliente> listaPedidosConCliente) {
        HashMap<String, Integer> listaProductoConCantidadTotal = new HashMap<>();

        for (PedidoConCliente pedidoConCliente : listaPedidosConCliente) {
            List<ProductoCantidad> listaProductos = pedidoConCliente.getPedido().getProductos();
            for (ProductoCantidad productoCantidad : listaProductos) {
                String nombreProducto = mapaProductoIdANombre.get(productoCantidad.getProductoId());

                if (nombreProducto != null) {
                    listaProductoConCantidadTotal.put(nombreProducto,
                            listaProductoConCantidadTotal.getOrDefault(nombreProducto, 0) + productoCantidad.getCantidad());
                } else {
                    Log.d("error", "Producto con ID " + productoCantidad.getProductoId() + " no encontrado en el mapa.");
                }
            }
        }

        return listaProductoConCantidadTotal;
    }

    public double obtenerGananciaPromedioPorOrden() {
        double promedio = 0.0;
        for (PedidoConCliente pedidoConCliente : listaPedidosConCliente) {
            promedio += pedidoConCliente.getPedido().getMontoTotal();
        }
        promedio = promedio / listaPedidosConCliente.size();

        // Redondear a dos decimales
        promedio = Math.round(promedio * 100.0) / 100.0;

        return promedio;
    }

}