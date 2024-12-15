package com.example.foodisea.repository;

import com.example.foodisea.model.Mensaje;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MensajeRepository {
    private final CollectionReference mensajeCollection;

    public MensajeRepository() {
        // Inicializa la referencia a la colección "Mensajes" en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mensajeCollection = db.collection("Mensajes");
    }

    // Crear un nuevo mensaje
    public Task<Void> createMensaje(String mensajeId, Mensaje mensaje) {
        return mensajeCollection.document(mensajeId).set(mensaje);
    }

    // Obtener un mensaje por su ID
    public Task<Mensaje> getMensajeById(String mensajeId) {
        return mensajeCollection.document(mensajeId).get()
                .continueWith(task -> task.getResult().toObject(Mensaje.class));
    }

    // Obtener todos los mensajes de un chat ordenados por timestamp
    public Query getMensajesByChatId(String chatId) {
        return mensajeCollection.whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    // Actualizar un mensaje
    public Task<Void> updateMensaje(String mensajeId, Mensaje mensaje) {
        return mensajeCollection.document(mensajeId).set(mensaje);
    }

    // Eliminar un mensaje
    public Task<Void> deleteMensaje(String mensajeId) {
        return mensajeCollection.document(mensajeId).delete();
    }
}
