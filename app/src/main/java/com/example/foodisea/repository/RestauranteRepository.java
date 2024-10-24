package com.example.foodisea.repository;

import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestauranteRepository {
    private final FirebaseFirestore db;

    public RestauranteRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Obtener todos los restaurantes activos
    public Task<List<Restaurante>> getRestaurantesActivos() {
        return db.collection("restaurantes")
                .whereEqualTo("estado", "Activo")
                .get()
                .continueWith(task -> {
                    List<Restaurante> restaurantes = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Restaurante restaurante = doc.toObject(Restaurante.class);
                        if (restaurante != null) {
                            restaurante.setId(doc.getId());
                            restaurantes.add(restaurante);
                        }
                    }
                    return restaurantes;
                });
    }

    // Obtener restaurante específico
    public Task<Restaurante> getRestauranteById(String restauranteId) {
        return db.collection("restaurantes")
                .document(restauranteId)
                .get()
                .continueWith(task -> {
                    Restaurante restaurante = task.getResult().toObject(Restaurante.class);
                    if (restaurante != null) {
                        restaurante.setId(task.getResult().getId());
                    }
                    return restaurante;
                });
    }

    // Obtener ventas por restaurante
    public Task<List<Pedido>> getVentasPorRestaurante(String restauranteId, Date fechaInicio, Date fechaFin) {
        return db.collection("pedidos")
                .whereEqualTo("restauranteId", restauranteId)
                .whereEqualTo("estado", "Entregado")
                .whereGreaterThanOrEqualTo("fechaPedido", fechaInicio)
                .whereLessThanOrEqualTo("fechaPedido", fechaFin)
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(doc.getId());
                            pedidos.add(pedido);
                        }
                    }
                    return pedidos;
                });
    }

    // Obtener restaurante por ID del administrador
    public Task<Restaurante> getRestauranteByAdminId(String adminId) {
        return db.collection("restaurantes")
                .whereEqualTo("administradorId", adminId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.getResult().isEmpty()) {
                        return null;
                    }
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    Restaurante restaurante = doc.toObject(Restaurante.class);
                    if (restaurante != null) {
                        restaurante.setId(doc.getId());
                    }
                    return restaurante;
                });
    }
}
