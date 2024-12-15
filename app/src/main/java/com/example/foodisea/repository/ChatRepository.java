package com.example.foodisea.repository;

import android.util.Log;

import com.example.foodisea.model.Chat;
import com.example.foodisea.model.Mensaje;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Crear un chat entre el repartidor y el restaurante para un pedido específico.
     *
     * @param pedidoId      ID del pedido asociado al chat.
     * @param restauranteId ID del restaurante.
     * @param repartidorId  ID del repartidor.
     * @return Task con el ID del chat creado.
     */
    public Task<String> crearChat(String pedidoId, String restauranteId, String repartidorId) {
        String chatId = db.collection("chats").document().getId(); // Generar ID único para el chat.

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("pedidoId", pedidoId);
        chatData.put("restauranteId", restauranteId);
        chatData.put("repartidorId", repartidorId);
        chatData.put("estado", "activo");
        chatData.put("timestamp", FieldValue.serverTimestamp()); // Marca de tiempo del servidor.

        return db.collection("chats")
                .document(chatId)
                .set(chatData)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return chatId; // Devolver el ID del chat creado.
                    } else {
                        throw task.getException();
                    }
                });
    }

    public void getMensajes(String chatId, OnSuccessListener<List<Mensaje>> onSuccess, OnFailureListener onFailure) {
        db.collection("mensajes")
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error al obtener mensajes", error);
                        onFailure.onFailure(error);
                        return;
                    }
                    if (querySnapshot != null) {
                        Log.d("FirestoreDebug", "Mensajes obtenidos: " + querySnapshot.size());
                        List<Mensaje> mensajes = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Mensaje mensaje = document.toObject(Mensaje.class);
                            if (mensaje != null) {
                                mensaje.setId(document.getId());
                                mensajes.add(mensaje);
                            }
                        }
                        onSuccess.onSuccess(mensajes);
                    }
                });
    }

    public void enviarMensaje(Mensaje mensaje, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

}
