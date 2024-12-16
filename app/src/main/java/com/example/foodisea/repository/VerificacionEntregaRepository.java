package com.example.foodisea.repository;

import android.util.Log;

import com.example.foodisea.model.VerificacionEntrega;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
        Log.d("VerificacionRepo", "Iniciando confirmación de entrega para id: " + id);

        Map<String, Object> updates = new HashMap<>();
        updates.put("entregaConfirmada", true);
        updates.put("fechaEntregaConfirmada", new Date());

        return verificacionCollection.document(id)
                .update(updates)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d("VerificacionRepo", "Entrega marcada como confirmada, verificando estado completo...");
                        return verificarConfirmacionesCompletas(id);
                    }
                    Log.e("VerificacionRepo", "Error al marcar entrega: " + task.getException());
                    return Tasks.forException(task.getException());
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                        Log.d("VerificacionRepo", "Ambas confirmaciones completas, actualizando pedido...");
                        // Obtener el pedidoId para actualizar su estado
                        return verificacionCollection.document(id).get()
                                .continueWithTask(docTask -> {
                                    VerificacionEntrega verificacion = docTask.getResult().toObject(VerificacionEntrega.class);
                                    if (verificacion != null) {
                                        Log.d("VerificacionRepo", "Actualizando estado del pedido: " + verificacion.getPedidoId());
                                        return FirebaseFirestore.getInstance()
                                                .collection("pedidos")
                                                .document(verificacion.getPedidoId())
                                                .update(
                                                        "estado", "ENTREGADO",
                                                        "estadoVerificacion", "COMPLETADO"
                                                );
                                    }
                                    Log.e("VerificacionRepo", "Verificación no encontrada");
                                    return Tasks.forResult(null);
                                });
                    }
                    Log.d("VerificacionRepo", "Confirmación de entrega completada sin actualizar pedido");
                    return Tasks.forResult(null);
                })
                .addOnSuccessListener(__ ->
                        Log.d("VerificacionRepo", "Proceso de confirmación de entrega completado exitosamente"))
                .addOnFailureListener(e ->
                        Log.e("VerificacionRepo", "Error en proceso de confirmación de entrega: " + e.getMessage()));
    }

    public Task<Void> confirmarPago(String id) {
        Log.d("VerificacionRepo", "Iniciando confirmación de pago para id: " + id);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pagoConfirmado", true);
        updates.put("fechaPagoConfirmado", new Date());

        return verificacionCollection.document(id)
                .update(updates)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d("VerificacionRepo", "Pago marcado como confirmado, verificando estado completo...");
                        return verificarConfirmacionesCompletas(id);
                    }
                    Log.e("VerificacionRepo", "Error al marcar pago: " + task.getException());
                    return Tasks.forException(task.getException());
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                        Log.d("VerificacionRepo", "Ambas confirmaciones completas, actualizando pedido...");
                        return verificacionCollection.document(id).get()
                                .continueWithTask(docTask -> {
                                    VerificacionEntrega verificacion = docTask.getResult().toObject(VerificacionEntrega.class);
                                    if (verificacion != null) {
                                        Log.d("VerificacionRepo", "Actualizando estado del pedido: " + verificacion.getPedidoId());
                                        return FirebaseFirestore.getInstance()
                                                .collection("pedidos")
                                                .document(verificacion.getPedidoId())
                                                .update(
                                                        "estado", "ENTREGADO",
                                                        "estadoVerificacion", "COMPLETADO"
                                                );
                                    }
                                    Log.e("VerificacionRepo", "Verificación no encontrada");
                                    return Tasks.forResult(null);
                                });
                    }
                    Log.d("VerificacionRepo", "Confirmación de pago completada sin actualizar pedido");
                    return Tasks.forResult(null);
                })
                .addOnSuccessListener(__ ->
                        Log.d("VerificacionRepo", "Proceso de confirmación de pago completado exitosamente"))
                .addOnFailureListener(e ->
                        Log.e("VerificacionRepo", "Error en proceso de confirmación de pago: " + e.getMessage()));
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

    public Task<Boolean> esUltimaVerificacion(String verificacionId) {
        Log.d("VerificacionRepo", "Verificando si es última verificación: " + verificacionId);
        return verificacionCollection.document(verificacionId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        VerificacionEntrega verificacion = task.getResult().toObject(VerificacionEntrega.class);
                        boolean entregaConfirmada = verificacion.isEntregaConfirmada();
                        boolean pagoConfirmado = verificacion.isPagoConfirmado();

                        Log.d("VerificacionRepo", "Estado verificaciones - Entrega: " + entregaConfirmada + ", Pago: " + pagoConfirmado);
                        // Si ambas están confirmadas, esta fue la última
                        return entregaConfirmada && pagoConfirmado;
                    }
                    return false;
                });
    }

}
