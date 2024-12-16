package com.example.foodisea.repository;

import android.content.Context;
import android.util.Log;

import com.example.foodisea.model.VerificacionEntrega;
import com.example.foodisea.notification.NotificationHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerificacionEntregaRepository {
    private final FirebaseFirestore db;
    private final CollectionReference verificacionCollection;
    private final Context context;
    private static final String COLLECTION_NAME = "verificaciones_entrega";

    public VerificacionEntregaRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.verificacionCollection = db.collection(COLLECTION_NAME);
        this.context = context;
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

    private Task<Void> manejarFinalizacionVerificacion(String verificacionId) {
        Log.d("VerificacionRepo", "Manejando finalización de verificación...");
        return verificacionCollection.document(verificacionId).get()
                .continueWithTask(docTask -> {
                    VerificacionEntrega verificacion = docTask.getResult().toObject(VerificacionEntrega.class);
                    if (verificacion != null) {
                        return FirebaseFirestore.getInstance()
                                .collection("pedidos")
                                .document(verificacion.getPedidoId())
                                .get()
                                .continueWithTask(pedidoTask -> {
                                    String repartidorId = pedidoTask.getResult().getString("repartidorId");

                                    List<Task<Void>> tasks = new ArrayList<>();

                                    tasks.add(FirebaseFirestore.getInstance()
                                            .collection("pedidos")
                                            .document(verificacion.getPedidoId())
                                            .update(
                                                    "estado", "Entregado",
                                                    "estadoVerificacion", "COMPLETADO"
                                            ));

                                    tasks.add(FirebaseFirestore.getInstance()
                                            .collection("usuarios")
                                            .document(repartidorId)
                                            .update("disposicion", "Disponible"));

                                    NotificationHelper notificationHelper = new NotificationHelper(context);
                                    notificationHelper.enviarNotificacionVerificacionCompletada(verificacion.getPedidoId());

                                    return Tasks.whenAll(tasks);
                                });
                    }
                    return Tasks.forResult(null);
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
                        return verificarConfirmacionesCompletas(id);
                    }
                    return Tasks.forException(task.getException());
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                        return manejarFinalizacionVerificacion(id);
                    }
                    return Tasks.forResult(null);
                });
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
                        return verificarConfirmacionesCompletas(id);
                    }
                    return Tasks.forException(task.getException());
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                        return manejarFinalizacionVerificacion(id);
                    }
                    return Tasks.forResult(null);
                });
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
