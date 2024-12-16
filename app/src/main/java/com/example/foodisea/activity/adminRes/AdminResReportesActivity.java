package com.example.foodisea.activity.adminRes;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityAdminResReportesBinding;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Pedido;
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

import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminResReportesActivity extends AppCompatActivity {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(AdminResReportesActivity.class);
    private PedidoRepository pedidoRepository;
    private ProductoRepository productoRepository;
    private List<PedidoConCliente> listaPedidosConCliente = new ArrayList<>();
    private HashMap<String, String> mapaProductoIdANombre = new HashMap<>();

    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    ActivityAdminResReportesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminResReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener al admin logueado
        sessionManager = SessionManager.getInstance(this);
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();

        // boton
        binding.btnBack.setOnClickListener(view -> finish());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ejemplo de datos
        Map<String, Integer> ventasPorUsuario = new HashMap<>();
        ventasPorUsuario.put("Usuario1", 80);
        ventasPorUsuario.put("Usuario2", 60);
        ventasPorUsuario.put("Usuario3", 40);

        pedidoRepository = new PedidoRepository(this);
        productoRepository = new ProductoRepository();

        // Configurar gráficos
        cargarMapaProductos();
        obtenerListaPedidos();



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

    private void setupBarChart2(BarChart chart, Map<String,Double> data, String label) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
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

    // Obtiene la lista de productos para construir el mapa de ID a nombre
    public void cargarMapaProductos() {
        productoRepository.obtenerProductosPorRestaurante(administradorRestauranteActual.getRestauranteId()) // Método que obtenga todos los productos
                .addOnSuccessListener(productos -> {
                    for (Producto producto : productos) {
                        mapaProductoIdANombre.put(producto.getId(), producto.getNombre());
                    }
                    // Una vez que el mapa esté cargado, puedes llamar a obtenerListaPedidos
                    obtenerListaPedidos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("error", e.getMessage());
                });
    }

    // Obtiene la lista de pedidos y genera el resumen con nombres de productos
    public void obtenerListaPedidos() {
        BarChart barChartPlatos = findViewById(R.id.barChartPlatos);
        BarChart barChartUsuarios = findViewById(R.id.barChartUsuarios);
        pedidoRepository.getPedidosActivosRestaurante("REST001")
                .addOnSuccessListener(pedidos -> {
                    // Guardar la lista completa
                    listaPedidosConCliente = pedidos;

                    // Sumar cantidades y usar nombres de productos
                    HashMap<String, Integer> listaProductoConCantidadTotal = sumaCantidadProductosConNombres(listaPedidosConCliente);

                    //Suma
                    HashMap<String,Double> listaUsuarioPorVentaTotal = sumaVentasPorUsuario(listaPedidosConCliente);

                    //Configurar grafico
                    setupBarChart2(barChartUsuarios, listaUsuarioPorVentaTotal, "Ventas por Usuario");

                    // Configurar el gráfico de barras con los nombres en lugar de IDs
                    setupBarChart(barChartPlatos, listaProductoConCantidadTotal, "Ventas por Plato");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar pedidos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //Metodo para obtener las ventas total por usuario
    public HashMap<String,Double> sumaVentasPorUsuario(List<PedidoConCliente> listaPedidosConCliente){
        HashMap<String,Double> listaUsuarioPorVentaTotal = new HashMap<>();
        Double montoTotal = 0.0;
        for (PedidoConCliente pedidoConCliente : listaPedidosConCliente){
            if(listaUsuarioPorVentaTotal.containsKey(pedidoConCliente.getCliente().getDocumentoId())){
                montoTotal = listaUsuarioPorVentaTotal.get(pedidoConCliente.getCliente().getDocumentoId()) + pedidoConCliente.getPedido().getMontoTotal();
                listaUsuarioPorVentaTotal.put(pedidoConCliente.getCliente().getDocumentoId(),montoTotal);
            } else{
                listaUsuarioPorVentaTotal.put(pedidoConCliente.getCliente().getDocumentoId(),pedidoConCliente.getPedido().getMontoTotal());
            }
        }
        return listaUsuarioPorVentaTotal;
    }
}