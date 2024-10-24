package com.example.foodisea.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class CodigoQRRepository {
    private final FirebaseFirestore db;

    public CodigoQRRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Generar nuevo código QR para pedido
    public Task<Void> generarCodigoQR(CodigoQRRepository codigoQR) {
        return db.collection("codigosQR")
                .document()
                .set(codigoQR);
    }

    // Validar código QR
    public Task<Boolean> validarCodigoQR(String codigo, String pedidoId) {
        return db.collection("codigosQR")
                .whereEqualTo("codigo", codigo)
                .whereEqualTo("pedidoId", pedidoId)
                .whereEqualTo("estado", "Generado")
                .get()
                .continueWith(task -> !task.getResult().isEmpty());
    }
}
