package com.example.foodisea.repository;

import com.example.foodisea.model.VerificacionEntrega;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VerificacionEntregaRepository {
    private final FirebaseFirestore db;
    private final CollectionReference verificacionCollection;
    private static final String COLLECTION_NAME = "verificaciones_entrega";

    public VerificacionEntregaRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.verificacionCollection = db.collection(COLLECTION_NAME);
    }

    public Task<DocumentReference> crear(VerificacionEntrega verificacion) {
        return verificacionCollection.add(verificacion);
    }

    public Task<VerificacionEntrega> actualizar(String id, VerificacionEntrega verificacion) {
        return db.collection(COLLECTION_NAME)
                .document(id)
                .set(verificacion)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return verificacion;
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<DocumentSnapshot> obtenerPorId(String id) {
        return verificacionCollection.document(id).get();
    }

    public Task<DocumentSnapshot> obtenerPorPedidoId(String pedidoId) {
        return verificacionCollection
                .whereEqualTo("pedidoId", pedidoId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0);
                    }
                    return null;
                });
    }

    public Task<Void> confirmarEntrega(String id) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("entregaConfirmada", true);
        updates.put("fechaEntregaConfirmada", new Date());

        return verificacionCollection.document(id).update(updates);
    }

    public Task<Void> confirmarPago(String id) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("pagoConfirmado", true);
        updates.put("fechaPagoConfirmado", new Date());

        return verificacionCollection.document(id).update(updates);
    }

    // Método útil para verificar si ambas confirmaciones están completas
    public Task<Boolean> verificarConfirmacionesCompletas(String id) {
        return verificacionCollection.document(id).get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        VerificacionEntrega verificacion = document.toObject(VerificacionEntrega.class);
                        return verificacion != null &&
                                verificacion.isEntregaConfirmada() &&
                                verificacion.isPagoConfirmado();
                    }
                    return false;
                });
    }
}
