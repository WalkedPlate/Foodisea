package com.example.foodisea.repository;

import com.example.foodisea.model.Pago;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PagoRepository {
    private final FirebaseFirestore db;

    public PagoRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Registrar nuevo pago
    public Task<Void> registrarPago(Pago pago) {
        return db.collection("pagos")
                .document(pago.getId())
                .set(pago);
    }

    // Actualizar estado del pago
    public Task<Void> actualizarEstadoPago(String pagoId, String nuevoEstado) {
        return db.collection("pagos")
                .document(pagoId)
                .update("estadoPago", nuevoEstado);
    }

    // Obtener pagos por pedido
    public Task<Pago> getPagoPorPedido(String pedidoId) {
        return db.collection("pagos")
                .whereEqualTo("pedidoId", pedidoId)
                .get()
                .continueWith(task -> {
                    if (task.getResult().isEmpty()) {
                        return null;
                    }
                    return task.getResult().getDocuments().get(0).toObject(Pago.class);
                });
    }

    // Obtener historial de pagos de un cliente
    public Task<List<Pago>> getHistorialPagosCliente(String clienteId) {
        return db.collection("pagos")
                .whereEqualTo("clienteId", clienteId)
                .orderBy("fechaPago", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Pago> pagos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        pagos.add(doc.toObject(Pago.class));
                    }
                    return pagos;
                });
    }
}
