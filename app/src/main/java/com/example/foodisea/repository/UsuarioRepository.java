package com.example.foodisea.repository;

import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {
    private final FirebaseFirestore db;

    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Método genérico para obtener usuario por ID y tipo
    public Task<Usuario> getUsuarioById(String id) {
        return db.collection("usuarios")
                .document(id)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Usuario no encontrado");
                    }
                    return task.getResult().toObject(Usuario.class);
                });
    }

    // Método para cambiar estado de usuario (activo/inactivo)
    public Task<Void> cambiarEstadoUsuario(String userId, String nuevoEstado) {
        return db.collection("usuarios")
                .document(userId)
                .update("estado", nuevoEstado);
    }

    // Obtener todos los repartidores pendientes de aprobación
    public Task<List<Repartidor>> getRepartidoresPendientes() {
        return db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Repartidor")
                .whereEqualTo("estado", "Pendiente")
                .get()
                .continueWith(task -> {
                    List<Repartidor> repartidores = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        repartidores.add(doc.toObject(Repartidor.class));
                    }
                    return repartidores;
                });
    }
}
