package com.example.foodisea.repository;

import android.util.Log;

import com.example.foodisea.model.CodigoQR;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CodigoQRRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "codigosQR";

    public CodigoQRRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Generar nuevo código QR
    public Task<DocumentReference> generarCodigoQR(CodigoQR codigoQR) {
        return db.collection(COLLECTION_NAME)
                .add(codigoQR);
    }

    // Validar código QR (versión actualizada para el sistema dual)
    public Task<Boolean> validarCodigoQR(String codigo, String verificacionId, String tipo) {
        Log.d("QRRepository", "=== INICIANDO VALIDACIÓN ===");
        Log.d("QRRepository", "Código a validar: " + codigo);
        Log.d("QRRepository", "VerificacionId: " + verificacionId);
        Log.d("QRRepository", "Tipo: " + tipo);

        return db.collection(COLLECTION_NAME)
                .whereEqualTo("codigo", codigo)
                .whereEqualTo("verificacionId", verificacionId)
                .whereEqualTo("tipo", tipo)
                .whereEqualTo("estado", "GENERADO")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        Log.d("QRRepository", "Documentos encontrados: " + snapshot.size());
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Log.d("QRRepository", "Documento encontrado: " + doc.getData());
                        }
                        return !snapshot.isEmpty();
                    } else {
                        Log.e("QRRepository", "Error en consulta: " + task.getException().getMessage());
                        throw task.getException();
                    }
                });
    }

    // Obtener código QR por código
    public Task<DocumentSnapshot> obtenerPorCodigo(String codigo) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("codigo", codigo)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0);
                    }
                    return null;
                });
    }

    // Marcar código QR como escaneado
    public Task<Void> marcarComoEscaneado(String id) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "ESCANEADO");
        updates.put("fechaEscaneo", new Date());

        return db.collection(COLLECTION_NAME)
                .document(id)
                .update(updates);
    }

    // Obtener códigos QR por verificación
    public Task<QuerySnapshot> obtenerPorVerificacionId(String verificacionId) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("verificacionId", verificacionId)
                .get();
    }

    // Obtener código QR específico de una verificación
    public Task<DocumentSnapshot> obtenerQRVerificacion(String verificacionId, String tipo) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("verificacionId", verificacionId)
                .whereEqualTo("tipo", tipo)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0);
                    }
                    return null;
                });
    }
}
