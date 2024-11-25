package com.example.foodisea.repository.custom;

import android.content.Context;

import com.example.foodisea.model.Repartidor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class PedidoTrackingRepository {
    private final FirebaseFirestore db;

    public PedidoTrackingRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
    }

    public ListenerRegistration listenToPedidoLocation(String pedidoId,
                                                       EventListener<DocumentSnapshot> listener) {
        return db.collection("pedidos")
                .document(pedidoId)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration listenToRepartidorLocation(String repartidorId,
                                                           EventListener<DocumentSnapshot> listener) {
        return db.collection("usuarios")
                .document(repartidorId)
                .addSnapshotListener(listener);
    }

    public Task<LatLng> getRepartidorLocation(String repartidorId) {
        return db.collection("usuarios")
                .document(repartidorId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("No se pudo obtener la ubicación del repartidor");
                    }
                    Repartidor repartidor = task.getResult().toObject(Repartidor.class);
                    if (repartidor == null) {
                        throw new Exception("Repartidor no encontrado");
                    }
                    return new LatLng(repartidor.getLatitud(), repartidor.getLongitud());
                });
    }
}
