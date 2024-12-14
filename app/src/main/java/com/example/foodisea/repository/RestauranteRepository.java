package com.example.foodisea.repository;

import android.util.Log;

import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Restaurante;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Repositorio que maneja todas las operaciones relacionadas con restaurantes en Firestore.
 * Proporciona métodos para crear, leer y gestionar datos de restaurantes.
 */
public class RestauranteRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_RESTAURANTES = "restaurantes";
    private static final String COLLECTION_PEDIDOS = "pedidos";

    public RestauranteRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Obtiene todos los restaurantes con estado "Activo".
     *
     * @return Task con la lista de restaurantes activos
     */
    public Task<List<Restaurante>> getRestaurantesActivos() {
        return db.collection(COLLECTION_RESTAURANTES)
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

    /**
     * Obtiene un restaurante específico por su ID.
     *
     * @param restauranteId ID del restaurante a buscar
     * @return Task con el restaurante encontrado o null si no existe
     */
    public Task<Restaurante> getRestauranteById(String restauranteId) {
        return db.collection(COLLECTION_RESTAURANTES)
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

    /**
     * Obtiene las ventas (pedidos entregados) de un restaurante en un rango de fechas.
     *
     * @param restauranteId ID del restaurante
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return Task con la lista de pedidos encontrados
     */
    public Task<List<Pedido>> getVentasPorRestaurante(String restauranteId, Date fechaInicio, Date fechaFin) {
        return db.collection(COLLECTION_PEDIDOS)
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

    /**
     * Obtiene el restaurante asociado a un administrador específico.
     *
     * @param adminId ID del administrador del restaurante
     * @return Task con el restaurante encontrado o null si no existe
     */
    public Task<Restaurante> getRestauranteByAdminId(String adminId) {
        return db.collection(COLLECTION_RESTAURANTES)
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

    /**
     * Crea un nuevo restaurante en Firestore.
     *
     * @param restaurante Objeto Restaurante a crear
     * @return Task con el ID del restaurante creado
     */
    public Task<DocumentReference> createRestaurante(Restaurante restaurante) {
        try {
            return db.collection(COLLECTION_RESTAURANTES).add(restaurante);
        } catch (Exception e) {
            Log.e("FirestoreError", "Error al guardar el restaurante", e);
            throw e;
        }
    }

    /**
     * Actualiza el estado de un restaurante.
     *
     * @param restauranteId ID del restaurante
     * @param nuevoEstado Nuevo estado a establecer
     * @return Task<Void>
     */
    public Task<Void> actualizarEstadoRestaurante(String restauranteId, String nuevoEstado) {
        return db.collection(COLLECTION_RESTAURANTES)
                .document(restauranteId)
                .update("estado", nuevoEstado);
    }
}