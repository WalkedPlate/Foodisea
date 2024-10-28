package com.example.foodisea.repository;

import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Superadmin;
import com.example.foodisea.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repositorio que maneja todas las operaciones relacionadas con usuarios en Firebase
 * Incluye operaciones de autenticación y gestión de datos en Firestore
 */
public class UsuarioRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private static final String COLLECTION_USUARIOS = "usuarios";

    public UsuarioRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Realiza el inicio de sesión del usuario y obtiene sus datos completos
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @return Task<Usuario> con los datos del usuario si el login es exitoso
     */
    public Task<Usuario> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Obtener el UID del usuario autenticado
                    String uid = task.getResult().getUser().getUid();
                    // Buscar los datos completos en Firestore
                    return getUserData(uid);
                });
    }

    /**
     * Obtiene los datos del usuario desde Firestore
     * @param uid ID del usuario
     * @return Task<Usuario> con los datos del usuario
     */
    private Task<Usuario> getUserData(String uid) {
        return db.collection(COLLECTION_USUARIOS)
                .document(uid)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        throw new Exception("Usuario no encontrado en la base de datos");
                    }

                    DocumentSnapshot document = task.getResult();
                    String tipoUsuario = document.getString("tipoUsuario");
                    if (tipoUsuario == null) {
                        throw new Exception("Tipo de usuario no definido");
                    }

                    // Convertir a la clase específica según el tipo
                    Usuario usuario = null;
                    switch (tipoUsuario) {
                        case "AdministradorRestaurante":
                            usuario = document.toObject(AdministradorRestaurante.class);
                            break;
                        case "Repartidor":
                            usuario = document.toObject(Repartidor.class);
                            break;
                        case "Cliente":
                            usuario = document.toObject(Cliente.class);
                            break;
                        case "Superadmin":
                            usuario = document.toObject(Superadmin.class);
                            break;
                        default:
                            throw new Exception("Tipo de usuario no válido");
                    }

                    if (usuario == null) {
                        throw new Exception("Error al convertir datos del usuario");
                    }

                    usuario.setId(uid);
                    return usuario;
                });
    }

    /**
     * Envía un correo para restablecer la contraseña
     * @param email Correo del usuario
     * @return Task<Void>
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    /**
     * Verifica si hay un usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Actualiza el estado del usuario en Firestore
     * @param userId ID del usuario
     * @param nuevoEstado Nuevo estado a establecer
     * @return Task<Void>
     */
    public Task<Void> actualizarEstadoUsuario(String userId, String nuevoEstado) {
        return db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update("estado", nuevoEstado);
    }

    /**
     * Registra un nuevo usuario en Firebase Auth y Firestore
     * Soporta múltiples tipos de usuario (Cliente, Repartidor)
     * @param email Correo del usuario
     * @param password Contraseña del usuario
     * @param usuario Datos del usuario a registrar (Cliente o Repartidor)
     * @return Task<Usuario> con los datos del usuario registrado
     */
    public Task<Usuario> registerUser(String email, String password, Usuario usuario) {
        // El código existente ya maneja correctamente diferentes tipos de usuario
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();
                    return db.collection(COLLECTION_USUARIOS)
                            .document(uid)
                            .set(usuario)
                            .continueWith(firestoreTask -> usuario);
                });
    }

    /**
     * Obtiene el usuario por su ID
     * @param userId ID del usuario
     * @return Task<Usuario>
     */
    public Task<Usuario> getUserById(String userId) {
        return db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        throw new Exception("Usuario no encontrado");
                    }
                    return task.getResult().toObject(Usuario.class);
                });
    }
}
