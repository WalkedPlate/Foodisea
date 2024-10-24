package com.example.foodisea.repository;

import com.example.foodisea.model.Pedido;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PedidoRepository {
    private final FirebaseFirestore db;

    public PedidoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Crear nuevo pedido
    public Task<Void> crearPedido(Pedido pedido) {
        return db.collection("pedidos")
                .document()
                .set(pedido);
    }

    // Actualizar estado del pedido
    public Task<Void> actualizarEstadoPedido(String pedidoId, String nuevoEstado) {
        return db.collection("pedidos")
                .document(pedidoId)
                .update("estado", nuevoEstado);
    }

    // Obtener pedidos activos de un repartidor
    public Task<List<Pedido>> getPedidosActivosRepartidor(String repartidorId) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Arrays.asList("En camino", "Recibido"))
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        pedidos.add(doc.toObject(Pedido.class));
                    }
                    return pedidos;
                });
    }

    // Obtener pedidos por cliente
    public Task<List<Pedido>> getPedidosPorCliente(String clienteId) {
        return db.collection("pedidos")
                .whereEqualTo("clienteId", clienteId)
                .orderBy("fechaPedido", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        pedidos.add(doc.toObject(Pedido.class));
                    }
                    return pedidos;
                });
    }
}
