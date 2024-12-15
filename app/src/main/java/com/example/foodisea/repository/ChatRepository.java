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
import com.google.firebase.firestore.WriteBatch;

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
    public Task<String> crearChat(String pedidoId, String restauranteId, String repartidorId, String clienteId) {
        String chatId = db.collection("chats").document().getId(); // Generar ID único para el chat.

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("pedidoId", pedidoId);
        chatData.put("restauranteId", restauranteId);
        chatData.put("repartidorId", repartidorId);
        chatData.put("clienteId", clienteId);
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
        // El mensaje ya tiene el timestamp en formato correcto
        db.collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void getChatById(String chatId, OnSuccessListener<Chat> onSuccess, OnFailureListener onFailure) {
        db.collection("chats")
                .document(chatId)
                .get()
                .addOnSuccessListener(document -> {
                    Chat chat = document.toObject(Chat.class);
                    if (chat != null) {
                        chat.setId(document.getId());
                    }
                    onSuccess.onSuccess(chat);
                })
                .addOnFailureListener(onFailure);
    }

    public void actualizarChat(Chat chat, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("ultimoMensaje", chat.getUltimoMensaje());
        updates.put("timestamp", chat.getTimestamp()); // Ya es un Timestamp
        updates.put("estadoPedido", chat.getEstadoPedido());

        db.collection("chats")
                .document(chat.getId())
                .update(updates)
                .addOnSuccessListener(onSuccess != null ? onSuccess : unused -> {})
                .addOnFailureListener(onFailure != null ? onFailure : e -> {});
    }

    public void getChatByPedidoId(String pedidoId, OnSuccessListener<Chat> onSuccess, OnFailureListener onFailure) {
        db.collection("chats")
                .whereEqualTo("pedidoId", pedidoId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Como solo debería haber un chat por pedido, tomamos el primero
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        Chat chat = document.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(document.getId());
                            onSuccess.onSuccess(chat);
                        } else {
                            onFailure.onFailure(new Exception("Error al convertir chat"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("No se encontró chat para este pedido"));
                    }
                })
                .addOnFailureListener(onFailure);
    }


}
