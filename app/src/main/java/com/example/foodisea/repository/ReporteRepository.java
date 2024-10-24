package com.example.foodisea.repository;

import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.model.Reporte;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReporteRepository {
    private final FirebaseFirestore db;

    public ReporteRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Método auxiliar para obtener precios de productos
    private Task<Map<String, Double>> obtenerPreciosProductos(String restauranteId) {
        return db.collection("productos")
                .whereEqualTo("restauranteId", restauranteId)
                .get()
                .continueWith(task -> {
                    Map<String, Double> preciosProductos = new HashMap<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        preciosProductos.put(doc.getId(), doc.getDouble("precio"));
                    }
                    return preciosProductos;
                });
    }

    // Generar reporte de ventas por restaurante
    public Task<Reporte> generarReporteVentasRestaurante(String restauranteId, Date fechaInicio, Date fechaFin) {
        return obtenerPreciosProductos(restauranteId).continueWithTask(preciosTask -> {
            Map<String, Double> preciosProductos = preciosTask.getResult();

            return db.collection("pedidos")
                    .whereEqualTo("restauranteId", restauranteId)
                    .whereEqualTo("estado", "Entregado")
                    .whereGreaterThanOrEqualTo("fechaPedido", fechaInicio)
                    .whereLessThanOrEqualTo("fechaPedido", fechaFin)
                    .get()
                    .continueWith(pedidosTask -> {
                        Map<String, Integer> ventasPorProducto = new HashMap<>();
                        double totalVentas = 0;

                        for (DocumentSnapshot doc : pedidosTask.getResult()) {
                            Pedido pedido = doc.toObject(Pedido.class);
                            if (pedido != null) {
                                for (ProductoCantidad pc : pedido.getProductos()) {
                                    ventasPorProducto.merge(pc.getProductoId(), pc.getCantidad(), Integer::sum);
                                }
                                totalVentas += pedido.getTotal(preciosProductos);
                            }
                        }

                        Reporte reporte = new Reporte();
                        reporte.setRestauranteId(restauranteId);
                        reporte.setFechaInicio(fechaInicio);
                        reporte.setFechaFin(fechaFin);
                        reporte.setVentasPorProducto(ventasPorProducto);
                        reporte.setTotalVentas(totalVentas);

                        return reporte;
                    });
        });
    }

    // Guardar reporte generado
    public Task<Void> guardarReporte(Reporte reporte) {
        return db.collection("reportes")
                .document()
                .set(reporte);
    }

    // Obtener reporte por ID
    public Task<Reporte> getReportePorId(String reporteId) {
        return db.collection("reportes")
                .document(reporteId)
                .get()
                .continueWith(task -> task.getResult().toObject(Reporte.class));
    }

    // Obtener ventas por producto en un período
    public Task<Map<String, Integer>> getVentasPorProducto(String restauranteId, Date fechaInicio, Date fechaFin) {
        return db.collection("pedidos")
                .whereEqualTo("restauranteId", restauranteId)
                .whereEqualTo("estado", "Entregado")
                .whereGreaterThanOrEqualTo("fechaPedido", fechaInicio)
                .whereLessThanOrEqualTo("fechaPedido", fechaFin)
                .get()
                .continueWith(task -> {
                    Map<String, Integer> ventasPorProducto = new HashMap<>();

                    for (DocumentSnapshot doc : task.getResult()) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        if (pedido != null) {
                            for (ProductoCantidad pc : pedido.getProductos()) {
                                ventasPorProducto.merge(pc.getProductoId(), pc.getCantidad(), Integer::sum);
                            }
                        }
                    }

                    return ventasPorProducto;
                });
    }

    // Obtener ventas por cliente en un período
    public Task<Map<String, Double>> getVentasPorCliente(String restauranteId, Date fechaInicio, Date fechaFin) {
        return obtenerPreciosProductos(restauranteId).continueWithTask(preciosTask -> {
            Map<String, Double> preciosProductos = preciosTask.getResult();

            return db.collection("pedidos")
                    .whereEqualTo("restauranteId", restauranteId)
                    .whereEqualTo("estado", "Entregado")
                    .whereGreaterThanOrEqualTo("fechaPedido", fechaInicio)
                    .whereLessThanOrEqualTo("fechaPedido", fechaFin)
                    .get()
                    .continueWith(pedidosTask -> {
                        Map<String, Double> ventasPorCliente = new HashMap<>();

                        for (DocumentSnapshot doc : pedidosTask.getResult()) {
                            Pedido pedido = doc.toObject(Pedido.class);
                            if (pedido != null) {
                                ventasPorCliente.merge(
                                        pedido.getClienteId(),
                                        pedido.getTotal(preciosProductos),
                                        Double::sum
                                );
                            }
                        }

                        return ventasPorCliente;
                    });
        });
    }
}
